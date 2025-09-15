package co.com.projectve.model.creditapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityCalculationMessage {
    
    // Datos de la nueva solicitud
    private Integer idRequest;
    private String documentType;
    private String documentNumber;
    private BigDecimal creditAmount;
    private Integer creditTime;
    private String email;
    private Short idState;
    private Short idLoanType;
    
    // Datos para el cálculo de capacidad
    private Double interestRate;
    private BigDecimal baseSalary;
    private BigDecimal maxBorrowingCapacity;
    
    // Préstamos activos del solicitante (sin calcular cuotas)
    private List<ActiveLoan> activeLoans;
}
