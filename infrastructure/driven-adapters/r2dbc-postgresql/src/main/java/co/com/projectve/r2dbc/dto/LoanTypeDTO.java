package co.com.projectve.r2dbc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanTypeDTO {
    private Integer idLoanType;
    private String nameLoanType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private double interestRate;
    private Boolean autoValidation;
}
