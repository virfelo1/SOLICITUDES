package co.com.projectve.r2dbc;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import co.com.projectve.r2dbc.dto.CreditApplicationListViewDTO;
import co.com.projectve.r2dbc.mapper.CreditApplicationEntityMapper;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import co.com.projectve.shared.clients.AuthClient;
import co.com.projectve.shared.dto.CreditApplicationResponseDTO;
import co.com.projectve.shared.dto.UserListDTO;
import jakarta.annotation.PostConstruct;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


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

    @Override
    public Flux<CreditApplication> listRequest() {
        return super.findAll();
    }

    @Override
    public Flux<CreditApplication> listAllEnriched() {
        // Se obtiene el token JWT del contexto de seguridad reactivo
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                        return ((Jwt) authentication.getPrincipal()).getTokenValue();
                    }
                    return "";
                })
                .flatMapMany(jwtToken -> {
                    logger.info("JWT extraído del contexto de seguridad: {}", jwtToken);

                    // Se obtienen las solicitudes de crédito de la base de datos local
                    Flux<CreditApplication> creditApplicationsFlux = super.findAll();

                    // Se obtiene el mapa de usuarios del microservicio de autenticación
                    Mono<Map<String, UserListDTO>> usersMapMono = authClient.listAllUsers(jwtToken)
                            .collect(Collectors.toMap(UserListDTO::getEmail, Function.identity()));

                    // Se combinan los dos flujos para enriquecer los datos
                    return usersMapMono.flatMapMany(usersMap ->
                            creditApplicationsFlux.map(creditApp -> {
                                UserListDTO userDetails = usersMap.get(creditApp.getEmail());
                                if (userDetails != null) {
                                    // Se crea un nuevo objeto CreditApplication con los datos enriquecidos
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
                                }
                                return creditApp; // Se devuelve el objeto original si no se encuentran detalles del usuario
                            })
                    );
                });
    }

    @PostConstruct
    public void testLog() {
        logger.info("Log4j2 funcionando correctamente en consola");
        logger.trace("MyReactiveRepositoryAdapter inicializado y listo para operaciones de persistencia");
    }
}
