package co.com.projectve.model.creditapplication;

import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreditApplication {
    private BigInteger id; //utilizar uno mas pequeño como integer
    private String documentType;
    private Long documentNumber; // aqui va string
    private BigDecimal creditAmount; // float o double mas que necesario
    private Integer creditTime;
    private String typeCredit;
    private String creditStatus;
}
