package co.com.projectve.model.creditapplication;

import java.math.BigDecimal;

public record ActiveLoan(
    Integer idRequest,
    BigDecimal creditAmount,
    Integer creditTime,
    Double interestRate,
    BigDecimal monthlyRequestAmount
) {}
