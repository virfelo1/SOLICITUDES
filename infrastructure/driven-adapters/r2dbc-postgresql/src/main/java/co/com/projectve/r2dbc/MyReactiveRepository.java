package co.com.projectve.r2dbc;

import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


// TODO: This file is just an example, you should delete or modify it
public interface MyReactiveRepository extends ReactiveCrudRepository<CreditApplicationEntity, Integer>, ReactiveQueryByExampleExecutor<CreditApplicationEntity> {

}
