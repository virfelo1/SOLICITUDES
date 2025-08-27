package co.com.projectve.model.infouser;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Tipo de documento del solicitante", example = "CC")
    private String documentType;

    @Schema(description = "Número de documento del solicitante", example = "1000200300")
    private Long documentNumber;

    @Schema(description = "Monto solicitado del crédito", example = "50000000.00")
    private BigDecimal creditAmount;

    @Schema(description = "Plazo del crédito en meses", example = "60")
    private Integer creditTime;

    @Schema(description = "Tipo de crédito", example = "Hipotecario/Vehiculo/Libre Inversion/Educacion")
    private String typeCredit;

    @Schema(description = "Estado actual de la solicitud", example = "Pendiente de revision")
    private String creditStatus;
}
