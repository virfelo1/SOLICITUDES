package co.com.projectve.model.infouser;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class InfoUser {
    private String id;
    private String documentType;
    private String documentNumber;
    private BigDecimal creditAmount;
    private String creditTime;
    private String typeCredit;
    private String creditStatus;
}
