package co.com.projectve.r2dbc;

import co.com.projectve.r2dbc.entity.InfoUserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.math.BigInteger;

// TODO: This file is just an example, you should delete or modify it
public interface MyReactiveRepository extends ReactiveCrudRepository<InfoUserEntity, BigInteger>, ReactiveQueryByExampleExecutor<InfoUserEntity> {

}
