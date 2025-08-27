package co.com.projectve.r2dbc;

import co.com.projectve.model.infouser.InfoUser;
import co.com.projectve.model.infouser.gateways.InfoUserRepository;
import co.com.projectve.r2dbc.entity.InfoUserEntity;
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
        InfoUser/* change for domain model */,
        InfoUserEntity/* change for adapter model */,
        BigInteger,
        MyReactiveRepository
        >
        implements InfoUserRepository{
    private final TransactionalOperator transactionalOperator;
    private static final Logger logger = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, InfoUser.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<InfoUser> saveRequest(InfoUser infoUser) {
        logger.trace("Iniciando solicitud de guardado para: {}", infoUser); // Log the start of the process
        return super.save(infoUser)
                .doOnSuccess(savedInfoUser -> logger.info("InfoUser guardado exitosamente con ID: {}", savedInfoUser.getId())) // Log on successful save
                .doOnError(error -> logger.error("No se pudo guardar InfoUser debido a: {}", error.getMessage(), error)) // Log errors
                .doFinally(signalType -> logger.trace("Solicitud de guardado completada: {}", signalType)); // Log when the process is finished
    }

    @PostConstruct
    public void testLog() {
        logger.info("Log4j2 Funcionan correctamente en consola");
    }
}