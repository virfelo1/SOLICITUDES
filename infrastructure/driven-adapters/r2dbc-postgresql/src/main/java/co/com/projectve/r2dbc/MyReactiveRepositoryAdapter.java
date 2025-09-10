package co.com.projectve.r2dbc;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import co.com.projectve.r2dbc.mapper.CreditApplicationEntityMapper;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import co.com.projectve.r2dbc.clients.AuthClient;
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
import co.com.projectve.shared.dto.PageResponse;
import co.com.projectve.shared.dto.PageableInfo;
import co.com.projectve.shared.dto.SortInfo;
import reactor.util.function.Tuples;
import reactor.util.function.Tuple2; // Importacion necesaria para Tuple2

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditApplication,
        CreditApplicationEntity,
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
    public Flux<CreditApplication> listAllEnriched() {
        return null;
    }*/

    // Nuevo método para obtener un Flux filtrado y enriquecido
    private Flux<CreditApplicationEnrichedDTO> getFilteredEnrichedDTO(String nameState, String nameLoan) {
        Flux<CreditApplication> baseFlux = super.findAll();

        return Mono.zip(
                authClient.listAllUsersFromContextAsMap(),
                databaseClient.sql("SELECT id_state, name_state FROM states").map((row, meta) -> Tuples.of(((Number) row.get("id_state")).shortValue(), (String) row.get("name_state"))).all().collectMap(Tuple2::getT1, Tuple2::getT2),
                databaseClient.sql("SELECT id_loan_type, name_loan FROM loan_type").map((row, meta) -> Tuples.of(((Number) row.get("id_loan_type")).shortValue(), (String) row.get("name_loan"))).all().collectMap(Tuple2::getT1, Tuple2::getT2),
                databaseClient.sql("SELECT id_loan_type, interest_rate FROM loan_type").map((row, meta) -> Tuples.of(((Number) row.get("id_loan_type")).shortValue(), ((Number) row.get("interest_rate")).doubleValue())).all().collectMap(Tuple2::getT1, Tuple2::getT2)
        ).flatMapMany(tuple -> {
            Map<String, UserInfoDTO> usersMap = tuple.getT1();
            Map<Short, String> statesMap = tuple.getT2();
            Map<Short, String> loansMap = tuple.getT3();
            Map<Short, Double> ratesMap = tuple.getT4();

            return baseFlux
                    .filter(creditApp -> {
                        String currentState = statesMap.get(creditApp.getIdState());
                        String currentLoan = loansMap.get(creditApp.getIdLoanType());
                        boolean stateMatches = nameState == null || (currentState != null && currentState.equalsIgnoreCase(nameState));
                        boolean loanMatches = nameLoan == null || (currentLoan != null && currentLoan.equalsIgnoreCase(nameLoan));
                        return stateMatches && loanMatches;
                    })
                    .map(creditApp -> {
                        String normalizedEmail = creditApp.getEmail() == null ? "" : creditApp.getEmail().trim().toLowerCase();
                        UserInfoDTO user = usersMap.get(normalizedEmail);
                        if (user == null) {
                            logger.trace("Usuario no encontrado para email en solicitudes: {}", normalizedEmail);
                        }
                        String stateName = statesMap.get(creditApp.getIdState());
                        String loanName = loansMap.get(creditApp.getIdLoanType());
                        Double interestRate = ratesMap.get(creditApp.getIdLoanType());
                        double principal = creditApp.getCreditAmount() != null ? creditApp.getCreditAmount().doubleValue() : 0d;
                        int periods = creditApp.getCreditTime() != null ? creditApp.getCreditTime() : 0;
                        double annualRate = interestRate != null ? interestRate : 0d;
                        double monthlyRequestAmount = 0d;

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
                                stateName,
                                loanName,
                                interestRate,
                                user != null ? user.getFirstName() : null,
                                user != null ? user.getBaseSalary() : null,
                                monthlyRequestAmount
                        );
                    });
        });
    }

    public Mono<PageResponse<CreditApplicationEnrichedDTO>> listAllEnrichedDTOPage(int page, int size, String nameState, String nameLoan) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;

        Flux<CreditApplicationEnrichedDTO> full = getFilteredEnrichedDTO(nameState, nameLoan);

        Mono<Long> totalMono = full.count();

        Mono<java.util.List<CreditApplicationEnrichedDTO>> contentMono = full
                .skip((long) safePage * safeSize)
                .take(safeSize)
                .collectList();

        return Mono.zip(contentMono, totalMono)
                .map(tuple -> {
                    java.util.List<CreditApplicationEnrichedDTO> content = tuple.getT1();
                    long total = tuple.getT2();
                    int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) total / (double) safeSize);
                    boolean isFirst = safePage == 0;
                    boolean isLast = totalPages == 0 ? true : (safePage >= totalPages - 1);

                    PageableInfo pageable = PageableInfo.builder()
                            .pageNumber(safePage)
                            .pageSize(safeSize)
                            .offset((long) safePage * safeSize)
                            .paged(true)
                            .unpaged(false)
                            .build();

                    SortInfo sort = SortInfo.builder()
                            .empty(true)
                            .sorted(false)
                            .unsorted(true)
                            .build();

                    return PageResponse.<CreditApplicationEnrichedDTO>builder()
                            .content(content)
                            .pageable(pageable)
                            .last(isLast)
                            .totalPages(totalPages)
                            .totalElements(total)
                            .size(safeSize)
                            .number(safePage)
                            .sort(sort)
                            .first(isFirst)
                            .numberOfElements(content.size())
                            .empty(content.isEmpty())
                            .build();
                });
    }

    @PostConstruct
    public void testLog() {
        logger.info("Log4j2 funcionando correctamente en consola");
        logger.trace("MyReactiveRepositoryAdapter inicializado y listo para operaciones de persistencia");
    }
}