package co.com.projectve.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO de respuesta para el cálculo de capacidad de endeudamiento")
public class CapacityTestResponseDTO {
    
    @Schema(description = "Email del solicitante")
    private String email;
    
    @Schema(description = "Salario base del solicitante")
    private BigDecimal baseSalary;
    
    @Schema(description = "Capacidad máxima de endeudamiento (35% del salario)")
    private BigDecimal maxBorrowingCapacity;
    
    @Schema(description = "Deuda mensual actual")
    private BigDecimal currentMonthlyDebt;
    
    @Schema(description = "Capacidad disponible")
    private BigDecimal availableCapacity;
    
    @Schema(description = "Lista de préstamos activos")
    private List<ActiveLoanDTO> activeLoans;
    
    @Schema(description = "Decisión del cálculo")
    private String decision;
    
    @Schema(description = "Razón de la decisión")
    private String reason;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "DTO para préstamos activos")
    public static class ActiveLoanDTO {
        @Schema(description = "ID del préstamo")
        private Integer loanId;
        
        @Schema(description = "Monto principal")
        private BigDecimal principalAmount;
        
        @Schema(description = "Tasa de interés")
        private BigDecimal interestRate;
        
        @Schema(description = "Meses restantes")
        private Integer remainingMonths;
        
        @Schema(description = "Cuota mensual")
        private BigDecimal monthlyPayment;
    }
}
