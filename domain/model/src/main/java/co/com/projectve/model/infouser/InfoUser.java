package co.com.projectve.model.infouser;
import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class InfoUser {
    private BigInteger id;
    private String documentType;
    private Long documentNumber;
    private BigDecimal creditAmount;
    private Integer creditTime;
    private String typeCredit;
    private String creditStatus;
}
