package co.com.projectve.r2dbc;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import jakarta.annotation.PostConstruct;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditApplication/* change for domain model */,
        CreditApplicationEntity/* change for adapter model */,
        BigInteger,
        MyReactiveRepository
        >
        implements CreditApplicationRepository {
    private final TransactionalOperator transactionalOperator;
    private static final Logger logger = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, CreditApplication.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<CreditApplication> saveRequest(CreditApplication creditApplication) {
        logger.trace("Iniciando solicitud de guardado para: {}", creditApplication); // Log the start of the process
        return super.save(creditApplication)
                .doOnSuccess(savedCreditApplication -> logger.info("CreditApplication guardado exitosamente con ID: {}", savedCreditApplication.getId())) // Log on successful save
                .doOnError(error -> logger.error("No se pudo guardar CreditApplication debido a: {}", error.getMessage(), error)) // Log errors
                .doFinally(signalType -> logger.trace("Solicitud de guardado completada: {}", signalType)); // Log when the process is finished
    }

    @PostConstruct
    public void testLog() {
        logger.info("Log4j2 Funcionan correctamente en consola");
    }
}