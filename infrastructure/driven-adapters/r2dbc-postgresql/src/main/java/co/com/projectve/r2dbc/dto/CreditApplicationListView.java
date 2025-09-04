package co.com.projectve.r2dbc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditApplicationListView {
    private Integer idRequest;
    private String documentType;
    private String documentNumber;
    private BigDecimal creditAmount;
    private Integer creditTime;
    private String email;
    private String nameState;
    private String nameLoan;
}


