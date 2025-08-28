package co.com.projectve.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "DTO para guardar una nueva solicitud de crédito")
public record CreditApplicationDTO(
        @Schema(description = "Tipo de documento del solicitante", example = "CC")
        @NotBlank(message = "El tipo de documento es obligatorio") //reemplaza las validaciones en el dominio
        String documentType,

        @Schema(description = "Número de documento del solicitante", example = "1000200300")
        @NotNull(message = "El número de documento es obligatorio")
        Long documentNumber,

        @Schema(description = "Monto solicitado del crédito", example = "50000000.00")
        @NotNull(message = "El monto solicitado es obligatorio")
        BigDecimal creditAmount,

        @Schema(description = "Plazo del crédito en meses", example = "60")
        @NotNull(message = "El plazo del crédito es obligatorio")
        Integer creditTime,

        @Schema(description = "Tipo de crédito (Hipotecario, Vehiculo, Libre Inversion, Educacion)", example = "Hipotecario")
        @NotBlank(message = "El tipo de crédito es obligatorio")
        String typeCredit

) {}