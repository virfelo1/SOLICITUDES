package co.com.projectve.api;

import co.com.projectve.api.dto.CreditApplicationDTO;
import co.com.projectve.api.mapper.CreditApplicationDTOMapper;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.usecase.creditapplication.CreditApplicationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Handler {
//private  final UseCase useCase;
//private  final UseCase2 useCase2;
    private final CreditApplicationUseCase creditApplicationUseCase;
    private final CreditApplicationDTOMapper creditApplicationDTOMapper;
    private final Validator validator; // Inyectamos el validador de Bean Validation

    //private final CreditApplicationUseCase creditApplicationUseCase;
    @Operation(summary = "Guarda una nueva solicitud de crédito",
            description = "Recibe los datos de una solicitud y la persiste en el sistema con el estado 'Pendiente de revision'.",
            tags = {"Solicitudes"},
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = CreditApplicationDTO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud de crédito guardada exitosamente",
                            content = @Content(schema = @Schema(implementation = CreditApplication.class))),
                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de la solicitud",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"El tipo de documento es obligatorio.\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })

    public Mono<ServerResponse> saveRequestApi(ServerRequest request) {
        return request.bodyToMono(CreditApplicationDTO.class)
                .flatMap(dto -> {
                    // Validación del DTO usando Bean Validation
                    Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        throw new ConstraintViolationException(violations);
                    }

                    // Mapeo y ejecución del caso de uso
                    CreditApplication model = creditApplicationDTOMapper.toModel(dto);
                    return creditApplicationUseCase.execute(model);
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}

