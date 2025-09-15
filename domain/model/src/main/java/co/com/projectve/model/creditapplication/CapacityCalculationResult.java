package co.com.projectve.model.creditapplication;

import java.math.BigDecimal;

public record CapacityCalculationResult(
    BigDecimal maxBorrowingCapacity,    // 35% del salario
    BigDecimal currentMonthlyDebt,      // Suma de cuotas actuales
    BigDecimal availableCapacity,       // Capacidad disponible
    BigDecimal newLoanPayment,          // Cuota del nuevo préstamo
    String decision,                    // "APROBADO", "RECHAZADO", "REVISION_MANUAL"
    String reason                       // Razón de la decisión
) {}
