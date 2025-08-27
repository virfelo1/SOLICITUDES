package co.com.projectve.r2dbc.config;

import co.com.projectve.r2dbc.MyReactiveRepositoryAdapter;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableR2dbcRepositories(basePackages = "co.com.projectve.r2dbc")


public class R2dbcConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);
    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }


    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

    @PostConstruct
    public void testLog() {
        logger.info("Se esta cargando la configuracion de R2dbc");
    }
}





