package co.com.projectve.r2dbc.adapters;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceAdapter implements TransactionService {
    
    private final DatabaseClient databaseClient;
    
    @Override
    @Transactional
    public Mono<CreditApplication> updateStateAtomically(CreditApplication creditApplication, String newState) {
        log.trace("[updateStateAtomically] Iniciando actualización atómica de estado para solicitud ID: {}", 
                 creditApplication.getIdRequest());
        
        short stateId = mapStateToId(newState);
        
        String sql = """
            UPDATE users_info 
            SET id_state = :stateId
            WHERE id_request = :idRequest
            """;
        
        return databaseClient
                .sql(sql)
                .bind("stateId", stateId)
                .bind("idRequest", creditApplication.getIdRequest())
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> {
                    if (rowsUpdated > 0) {
                        log.info("Estado actualizado exitosamente para solicitud ID: {} a estado: {}", 
                               creditApplication.getIdRequest(), newState);
                        creditApplication.setIdState(stateId);
                        return Mono.just(creditApplication);
                    } else {
                        log.error("No se pudo actualizar el estado para solicitud ID: {}", 
                                creditApplication.getIdRequest());
                        return Mono.error(new RuntimeException("No se pudo actualizar el estado de la solicitud"));
                    }
                })
                .doOnError(error -> log.error("Error en actualización atómica: {}", error.getMessage(), error));
    }
    
    @Override
    @Transactional
    public Mono<CreditApplication> updateStateWithCapacityResult(CreditApplication creditApplication, String capacityResult) {
        log.trace("[updateStateWithCapacityResult] Actualizando estado basado en resultado de capacidad: {}", capacityResult);
        
        // El estado ya viene actualizado en el CreditApplication desde la Lambda
        short stateId = creditApplication.getIdState();
        
        String sql = """
            UPDATE users_info 
            SET id_state = :stateId
            WHERE id_request = :idRequest
            """;
        
        return databaseClient
                .sql(sql)
                .bind("stateId", stateId)
                .bind("idRequest", creditApplication.getIdRequest())
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> {
                    if (rowsUpdated > 0) {
                        log.info("Estado actualizado con resultado de capacidad para solicitud ID: {} - Estado: {}", 
                               creditApplication.getIdRequest(), stateId);
                        return Mono.just(creditApplication);
                    } else {
                        log.error("No se pudo actualizar el estado con resultado de capacidad para solicitud ID: {}", 
                                creditApplication.getIdRequest());
                        return Mono.error(new RuntimeException("No se pudo actualizar el estado de la solicitud con resultado de capacidad"));
                    }
                })
                .doOnError(error -> log.error("Error en actualización con resultado de capacidad: {}", error.getMessage(), error));
    }
    
    private short mapStateToId(String state) {
        return switch (state) {
            case "Aprobado" -> (short) 2;
            case "Rechazado" -> (short) 3;
            case "Revision Manual" -> (short) 4;
            default -> (short) 1;
        };
    }
}
