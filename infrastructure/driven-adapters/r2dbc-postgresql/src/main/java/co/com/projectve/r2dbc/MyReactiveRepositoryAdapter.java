package co.com.projectve.r2dbc;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import co.com.projectve.r2dbc.dto.CreditApplicationListViewDTO;
import co.com.projectve.r2dbc.mapper.CreditApplicationEntityMapper;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import jakarta.annotation.PostConstruct;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


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
    private final CreditApplicationEntityMapper listViewMapper = new CreditApplicationEntityMapper() {};
    private static final Logger logger = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator, DatabaseClient databaseClient) {
        super(repository, mapper, entity -> mapper.map(entity, CreditApplication.class));
        this.transactionalOperator = transactionalOperator;
        this.databaseClient = databaseClient;
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

    public Flux<CreditApplicationListViewDTO> listRequestview() {
        String sql = "SELECT ui.id_request, ui.document_type, ui.document_number, ui.credit_amount, ui.credit_time, ui.email, " +
                "s.name_state AS name_state, lt.name_loan AS name_loan " +
                "FROM users_info ui " +
                "JOIN states s ON s.id_state = ui.id_state " +
                "JOIN loan_type lt ON lt.id_loan_type = ui.id_loan_type";

        return databaseClient.sql(sql)
                .map(row -> {
                    CreditApplicationListViewDTO dto = new CreditApplicationListViewDTO();
                    dto.setIdRequest(row.get("id_request", Integer.class));
                    dto.setDocumentType(row.get("document_type", String.class));
                    dto.setDocumentNumber(row.get("document_number", String.class));
                    dto.setCreditAmount(row.get("credit_amount", java.math.BigDecimal.class));
                    dto.setCreditTime(row.get("credit_time", Integer.class));
                    dto.setEmail(row.get("email", String.class));
                    dto.setNameState(row.get("name_state", String.class));
                    dto.setNameLoan(row.get("name_loan", String.class));
                    return dto;
                })
                .all();
    }

    @PostConstruct
    public void testLog() {
        logger.info("Log4j2 funcionando correctamente en consola");
        logger.trace("MyReactiveRepositoryAdapter inicializado y listo para operaciones de persistencia");
    }
}