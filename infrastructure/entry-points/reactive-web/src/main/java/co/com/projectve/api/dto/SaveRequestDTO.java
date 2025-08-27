package co.com.projectve.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "DTO para guardar una nueva solicitud de crédito")
public record SaveRequestDTO(
        @Schema(description = "Tipo de documento del solicitante", example = "CC")
        String documentType,
        @Schema(description = "Número de documento del solicitante", example = "1000200300")
        Long documentNumber,
        @Schema(description = "Monto solicitado del crédito", example = "50000000.00")
        BigDecimal creditAmount,
        @Schema(description = "Plazo del crédito en meses", example = "60")
        Integer creditTime,
        @Schema(description = "Tipo de crédito (Hipotecario, Vehiculo, Libre Inversion, Educacion)", example = "Hipotecario")
        String typeCredit
) {}