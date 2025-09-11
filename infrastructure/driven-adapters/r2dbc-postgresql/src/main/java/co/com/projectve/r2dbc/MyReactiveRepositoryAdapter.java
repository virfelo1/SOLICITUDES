package co.com.projectve.r2dbc;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import co.com.projectve.r2dbc.mapper.CreditApplicationEntityMapper;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import co.com.projectve.shared.clients.AuthClient;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import co.com.projectve.shared.dto.CreditApplicationEnrichedDTO;
import co.com.projectve.shared.dto.UserInfoDTO;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;


@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditApplication/* change for domain model */,
        CreditApplicationEntity/* change for adapter model */,
        Integer,
        MyReactiveRepository
        >
        implements CreditApplicationRepository {
    private final TransactionalOperator transactionalOperator;
    private final DatabaseClient databaseClient;
    private final AuthClient authClient;
    private final CreditApplicationEntityMapper listViewMapper = new CreditApplicationEntityMapper() {};
    private static final Logger logger = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator, DatabaseClient databaseClient, AuthClient authClient) {
        super(repository, mapper, entity -> mapper.map(entity, CreditApplication.class));
        this.transactionalOperator = transactionalOperator;
        this.databaseClient = databaseClient;
        this.authClient = authClient;
        logger.trace("MyReactiveRepositoryAdapter inicializado con repository: {}, mapper: {}, transactionalOperator: {}",
                repository.getClass().getSimpleName(), mapper.getClass().getSimpleName(), transactionalOperator.getClass().getSimpleName());
    }

    @Override
    public Mono<CreditApplication> saveRequest(CreditApplication creditApplication) {
        logger.trace("Iniciando solicitud de guardado para CreditApplication. ID: {}, TipoDocumento: {}, NumeroDocumento: {}",
                creditApplication.getIdRequest(), creditApplication.getDocumentType(), creditApplication.getDocumentNumber());

        logger.debug("Datos completos de la solicitud a persistir: {}", creditApplication);

        return super.save(creditApplication)
                .doOnSubscribe(subscription -> {
                    logger.trace("Operación de guardado suscrita. Iniciando persistencia en base de datos");
                })
                .doOnNext(savedCreditApplication -> {
                    logger.info("CreditApplication guardado exitosamente con ID: {}", savedCreditApplication.getIdRequest());
                    logger.debug("Datos de la entidad guardada: {}", savedCreditApplication);
                })
                .doOnError(error -> {
                    logger.error("No se pudo guardar CreditApplication debido a: {}", error.getMessage(), error);
                    logger.trace("Detalles del error de persistencia: tipo={}, causa={}",
                            error.getClass().getSimpleName(), error.getCause() != null ? error.getCause().getMessage() : "N/A");
                })
                .doFinally(signalType -> {
                    logger.trace("Solicitud de guardado completada. Señal: {}, ID de la solicitud: {}",
                            signalType, creditApplication.getIdRequest());
                });
    }

    /*@Override
    public Flux<CreditApplication> listRequest() {
        return super.findAll();
    }*/

    @Override
    public Flux<CreditApplication> listAllEnriched() {
        Flux<CreditApplication> creditApplicationsFlux = super.findAll();

        Mono<Map<String, UserInfoDTO>> usersMapMono = authClient.listAllUsersFromContextAsMap();

        return usersMapMono.flatMapMany(usersMap ->
                creditApplicationsFlux.map(creditApp -> {
                    UserInfoDTO user = usersMap.get(creditApp.getEmail());
                    if (user == null) {
                        return creditApp;
                    }
                    // Si el handler espera CreditApplicationEnrichedDTO, ajustar en capa superior.
                    return new CreditApplication(
                            creditApp.getIdRequest(),
                            creditApp.getDocumentType(),
                            creditApp.getDocumentNumber(),
                            creditApp.getCreditAmount(),
                            creditApp.getCreditTime(),
                            creditApp.getEmail(),
                            creditApp.getIdState(),
                            creditApp.getIdLoanType()
                    );
                })
        );
    }

    public Flux<CreditApplicationEnrichedDTO> listAllEnrichedDTO() {
        Flux<CreditApplication> creditApplicationsFlux = super.findAll();

        Mono<Map<String, UserInfoDTO>> usersMapMono = authClient.listAllUsersFromContextAsMap();

        Mono<Map<Short, String>> statesMapMono = databaseClient.sql("SELECT id_state, name_state FROM states")
                .map((row, meta) -> Tuples.of(((Number) row.get("id_state")).shortValue(), (String) row.get("name_state")))
                .all()
                .collectMap(t -> t.getT1(), t -> t.getT2());

        Mono<Map<Short, String>> loanTypeNameMapMono = databaseClient.sql("SELECT id_loan_type, name_loan FROM loan_type")
                .map((row, meta) -> Tuples.of(((Number) row.get("id_loan_type")).shortValue(), (String) row.get("name_loan")))
                .all()
                .collectMap(t -> t.getT1(), t -> t.getT2());

        Mono<Map<Short, Double>> loanTypeRateMapMono = databaseClient.sql("SELECT id_loan_type, interest_rate FROM loan_type")
                .map((row, meta) -> Tuples.of(((Number) row.get("id_loan_type")).shortValue(), ((Number) row.get("interest_rate")).doubleValue()))
                .all()
                .collectMap(t -> t.getT1(), t -> t.getT2());

        Mono<Tuple4<Map<String, UserInfoDTO>, Map<Short, String>, Map<Short, String>, Map<Short, Double>>> combined =
                Mono.zip(usersMapMono, statesMapMono, loanTypeNameMapMono, loanTypeRateMapMono);

        return combined.flatMapMany(tuple -> {
            Map<String, UserInfoDTO> usersMap = tuple.getT1();
            Map<Short, String> statesMap = tuple.getT2();
            Map<Short, String> loansMap = tuple.getT3();
            Map<Short, Double> ratesMap = tuple.getT4();

            return creditApplicationsFlux.map(creditApp -> {
                String normalizedEmail = creditApp.getEmail() == null ? "" : creditApp.getEmail().trim().toLowerCase();
                UserInfoDTO user = usersMap.get(normalizedEmail);
                if (user == null) {
                    logger.trace("Usuario no encontrado para email en solicitudes: {}", normalizedEmail);
                }
                String nameState = statesMap.get(creditApp.getIdState());
                String nameLoan = loansMap.get(creditApp.getIdLoanType());
                Double interestRate = ratesMap.get(creditApp.getIdLoanType());
                double principal = creditApp.getCreditAmount() != null ? creditApp.getCreditAmount().doubleValue() : 0d;
                int periods = creditApp.getCreditTime() != null ? creditApp.getCreditTime() : 0;
                double annualRate = interestRate != null ? interestRate : 0d;
                double monthlyRequestAmount = 0d;

// Convertir la tasa anual que viene en porcentaje (ej: 12.0) a decimal (0.12)
                annualRate = annualRate / 100.0;

                if (annualRate > 0d && periods > 0) {
                    double monthlyRate = annualRate / 12.0;
                    monthlyRequestAmount = (principal * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -periods));
                } else if (periods > 0) {
                    monthlyRequestAmount = principal / periods;
                }

                return new CreditApplicationEnrichedDTO(
                        creditApp.getIdRequest(),
                        creditApp.getDocumentType(),
                        creditApp.getDocumentNumber(),
                        creditApp.getCreditAmount(),
                        creditApp.getCreditTime(),
                        creditApp.getEmail(),
                        nameState,
                        nameLoan,
                        interestRate,
                        user != null ? user.getFirstName() : null,
                        user != null ? user.getBaseSalary() : null,
                        monthlyRequestAmount
                );
            });
        });
    }

    @PostConstruct
    public void testLog() {
        logger.info("Log4j2 funcionando correctamente en consola");
        logger.trace("MyReactiveRepositoryAdapter inicializado y listo para operaciones de persistencia");
    }
}
