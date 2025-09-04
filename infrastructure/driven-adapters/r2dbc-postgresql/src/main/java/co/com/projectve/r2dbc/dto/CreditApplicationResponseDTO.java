package co.com.projectve.r2dbc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditApplicationResponseDTO {
    private Integer idRequest;
    private String documentType;
    private String documentNumber;
    private BigDecimal creditAmount;
    private Integer creditTime;
    private String email;

    private StateDTO state;
    private LoanTypeDTO loanType;
}
