package co.com.projectve.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "DTO para guardar una nueva solicitud de crédito")
public record CreditApplicationDTO(
        @Schema(description = "Tipo de documento del solicitante", example = "CC")
        @Pattern(regexp = "^(CC|CE|TI|PP|NIT)$", message = "El tipo de documento debe ser CC, CE, TI, PP o NIT")
        @NotBlank(message = "El tipo de documento es obligatorio") //reemplaza las validaciones en el dominio
        String documentType,

        @Schema(description = "Número de documento del solicitante", example = "1000200300")
        @NotBlank(message = "El número de documento es obligatorio")
        String documentNumber,

        @Schema(description = "Monto solicitado del crédito", example = "50000000.00")
        @NotNull(message = "El monto solicitado es obligatorio")
        BigDecimal creditAmount,

        @Schema(description = "Plazo del crédito en meses", example = "60")
        @NotNull(message = "El plazo del crédito es obligatorio")
        @Min(value = 1, message = "El plazo mínimo es 1 mes")
        @Max(value = 360, message = "El plazo máximo es 360 meses (30 años)")
        Integer creditTime,

        @Schema(description = "Tipo de crédito (Hipotecario, Vehiculo, Libre Inversion, Educacion)", example = "Hipotecario")
        @NotBlank(message = "El tipo de crédito es obligatorio")
        @Pattern(regexp = "^(Hipotecario|Vehiculo|Libre Inversion|Educacion)$", message = "El tipo de crédito debe ser Hipotecario, Vehiculo, Libre Inversion o Educacion")
        String typeCredit

) {}