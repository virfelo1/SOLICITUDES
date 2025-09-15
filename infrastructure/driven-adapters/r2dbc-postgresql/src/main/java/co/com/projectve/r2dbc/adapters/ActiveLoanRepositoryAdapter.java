package co.com.projectve.r2dbc.adapters;

import co.com.projectve.model.creditapplication.gateways.ActiveLoanRepository;
import co.com.projectve.model.creditapplication.ActiveLoan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ActiveLoanRepositoryAdapter implements ActiveLoanRepository {
    
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    
    @Override
    public Flux<ActiveLoan> findActiveLoansByEmail(String email) {
        log.trace("[findActiveLoansByEmail] Buscando préstamos activos para email: {}", email);
        
        // Consultar préstamos con estado "Aprobado" (ID = 2)
        // Solo enviar datos básicos, la Lambda externa calculará la cuota mensual
        String sql = """
            SELECT
                ca.id_request,
                ca.credit_amount,
                ca.credit_time,
                lt.interest_rate
            FROM users_info ca
            INNER JOIN loan_type lt ON ca.id_loan_type = lt.id_loan_type
            WHERE ca.email = :email
            AND ca.id_state = 2  -- Estado "Aprobado"
            """;
        
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql(sql)
                .bind("email", email)
                .map((row, meta) -> {
                    Integer idRequest = row.get("id_request", Integer.class);
                    BigDecimal creditAmount = row.get("credit_amount", BigDecimal.class);
                    Integer creditTime = row.get("credit_time", Integer.class);
                    Double interestRate = row.get("interest_rate", Double.class);
                    
                    log.debug("[findActiveLoansByEmail] Préstamo encontrado - ID: {}, Monto: {}, Plazo: {}, Tasa: {}", 
                            idRequest, creditAmount, creditTime, interestRate);
                    
                // Calcular monthlyRequestAmount usando la misma lógica que MyReactiveRepositoryAdapter
                BigDecimal monthlyRequestAmount = calculateMonthlyRequestAmount(creditAmount, creditTime, interestRate);
                
                return new ActiveLoan(
                        idRequest,
                        creditAmount,
                        creditTime,
                        interestRate,
                        monthlyRequestAmount
                );
                })
                .all()
                .doOnNext(loan -> log.trace("[findActiveLoansByEmail] Préstamo activo: {}", loan))
                .doOnComplete(() -> log.info("[findActiveLoansByEmail] Búsqueda completada para email: {}", email))
                            .doOnError(error -> log.error("[findActiveLoansByEmail] Error buscando préstamos activos: {}", error.getMessage(), error));
    }

    /**
     * Calcula el monto mensual de la solicitud usando la misma lógica que MyReactiveRepositoryAdapter
     * @param creditAmount Monto del crédito
     * @param creditTime Tiempo del crédito en meses
     * @param interestRate Tasa de interés anual
     * @return Monto mensual calculado
     */
    private BigDecimal calculateMonthlyRequestAmount(BigDecimal creditAmount, Integer creditTime, Double interestRate) {
        if (creditAmount == null || creditTime == null || creditTime <= 0) {
            return BigDecimal.ZERO;
        }

        double principal = creditAmount.doubleValue();
        int periods = creditTime;
        double annualRate = interestRate != null ? interestRate : 0.0;
        double monthlyRequestAmount = 0.0;

        // Convertir tasa anual a decimal
        annualRate = annualRate / 100.0;

        if (annualRate > 0.0 && periods > 0) {
            // Fórmula de amortización: (principal * monthlyRate) / (1 - (1 + monthlyRate)^(-periods))
            double monthlyRate = annualRate / 12.0;
            monthlyRequestAmount = (principal * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -periods));
        } else if (periods > 0) {
            // Si no hay interés, dividir el principal entre los períodos
            monthlyRequestAmount = principal / periods;
        }

        log.debug("[calculateMonthlyRequestAmount] Calculado - Principal: {}, Períodos: {}, Tasa: {}%, Monto mensual: {}", 
                principal, periods, interestRate, monthlyRequestAmount);

        return BigDecimal.valueOf(monthlyRequestAmount);
    }
}
