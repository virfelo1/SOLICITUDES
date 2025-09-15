package co.com.projectve.r2dbc.adapters;

import co.com.projectve.model.creditapplication.gateways.LoanTypeRepository;
import co.com.projectve.model.creditapplication.gateways.LoanTypeInfo;
import co.com.projectve.r2dbc.entity.LoanTypeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

@Repository
@Slf4j
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {
    
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    
    @Override
    public Mono<LoanTypeInfo> findLoanTypeById(Short loanTypeId) {
        log.trace("[findLoanTypeById] Buscando tipo de préstamo con ID: {}", loanTypeId);
        
        return r2dbcEntityTemplate
                .select(LoanTypeEntity.class)
                .matching(Query.query(where("id_loan_type").is(loanTypeId)))
                .one()
                .map(this::mapToLoanTypeInfo)
                .doOnNext(loanType -> log.debug("Tipo de préstamo encontrado: {} - Validación automática: {}", 
                           loanType.nameLoanType(), loanType.autoValidation()))
                .doOnError(error -> log.error("Error buscando tipo de préstamo: {}", error.getMessage()));
    }
    
    private LoanTypeInfo mapToLoanTypeInfo(LoanTypeEntity entity) {
        return new LoanTypeInfo(
            entity.getIdLoanType(),
            entity.getNameLoanType(),
            entity.getAutoValidation(),
            entity.getMaxAmount(),
            entity.getMinAmount(),
            entity.getInterestRate()
        );
    }
}
