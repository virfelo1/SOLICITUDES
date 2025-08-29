package co.com.projectve.model.creditapplication;

import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreditApplication {
    private Integer id;
    private String documentType;
    private String documentNumber;
    private BigDecimal creditAmount; // float o double mas que necesario
    private Integer creditTime;
    private String typeCredit;
    private String creditStatus;
}
