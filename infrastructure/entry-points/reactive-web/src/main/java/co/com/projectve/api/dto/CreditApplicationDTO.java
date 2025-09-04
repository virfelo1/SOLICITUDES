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

        @Schema(description = "Correo electrónico único del usuario", example = "juan.garcia@email.com", requiredMode = Schema.RequiredMode.REQUIRED, format = "email")
        @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
                message = "El correo no es valido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotNull(message = "El tipo de crédito es obligatorio")
        @Min(value = 1, message = "El tipo de crédito debe ser 1, 2, 3 o 4")
        @Max(value = 4, message = "El tipo de crédito debe ser 1, 2, 3 o 4")
        Short idLoanType

) {}