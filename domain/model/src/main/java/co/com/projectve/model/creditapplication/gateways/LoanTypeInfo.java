package co.com.projectve.model.creditapplication.gateways;

// Record público para la información del tipo de préstamo
 public record LoanTypeInfo(
    Short idLoanType,
    String nameLoanType,
    Boolean autoValidation,
    java.math.BigDecimal maxAmount,
    java.math.BigDecimal minAmount,
    Double interestRate
) {}
