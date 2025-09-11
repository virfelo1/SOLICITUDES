package co.com.projectve.api;

import co.com.projectve.api.dto.CreditApplicationDTO;
import co.com.projectve.api.mapper.CreditApplicationDTOMapper;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.r2dbc.MyReactiveRepositoryAdapter;
import co.com.projectve.r2dbc.dto.CreditApplicationListViewDTO;
import co.com.projectve.shared.dto.UserInfoDTO;
import co.com.projectve.usecase.creditapplication.CreditApplicationUseCase;
import co.com.projectve.shared.dto.CreditApplicationEnrichedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final MyReactiveRepositoryAdapter myReactiveRepositoryAdapter;
    private final Validator validator; // Inyectamos el validador de Bean Validation

    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

    //private final CreditApplicationUseCase creditApplicationUseCase;
    @Operation(summary = "Guarda una nueva solicitud de crédito",
            description = "Recibe los datos de una solicitud y la persiste en el sistema con el estado 'Pendiente de revision'.",
            tags = {"Solicitudes"},
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = CreditApplicationDTO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud de crédito guardada exitosamente",
                            content = @Content(schema = @Schema(implementation = CreditApplicationDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de la solicitud",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"El tipo de documento es obligatorio.\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })

    public Mono<ServerResponse> saveRequestApi(ServerRequest serverRequest) {
        logger.trace("[saveRequestApi] Recibida solicitud POST /api/v1/solicitud");
        return serverRequest.bodyToMono(CreditApplicationDTO.class)
                .flatMap(dto -> {
                    logger.trace("[saveRequestApi] Iniciando validación de DTO");
                    // Validación del DTO usando Bean Validation
                    Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        logger.trace("[saveRequestApi] DTO inválido: {} violaciones", violations.size());
                        throw new ConstraintViolationException(violations);
                    }

                    // Mapeo y ejecución del caso de uso
                    logger.trace("[saveRequestApi] Mapeando DTO a modelo de dominio");
                    CreditApplication model = creditApplicationDTOMapper.toModel(dto);
                    logger.trace("[saveRequestApi] Ejecutando caso de uso - documentNumber={}", model.getDocumentNumber());
                    return creditApplicationUseCase.execute(model);
                })
                .doOnSuccess(r -> logger.trace("[saveRequestApi] Caso de uso completado exitosamente"))
                .doOnError(e -> logger.trace("[saveRequestApi] Error en caso de uso: {}", e.getMessage()))
                .flatMap(response -> {
                    logger.trace("[saveRequestApi] Enviando respuesta 200 OK");
                    return ServerResponse.ok().bodyValue(response);
                });
    }

    //public Mono<ServerResponse> listRequest(ServerRequest serverRequest){ //aqui esta el metodo para capturar la informacion que va al listado
    //    return ServerResponse.ok().body(creditApplicationUseCase.listRequest(), CreditApplication.class);
    //}
    @Operation(
            summary = "Lista todas las solicitudes de crédito",
            description = "Retorna el listado con información enriquecida (estado y tipo de préstamo).",
            tags = {"Solicitudes"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente",
                            content = @Content(schema = @Schema(implementation = CreditApplicationListViewDTO.class))
                    ),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public Mono<ServerResponse> listRequest(ServerRequest serverRequest){ //aqui esta el metodo para capturar la informacion que va al listado
        logger.trace("[listRequest] Recibida solicitud GET /api/v1/solicitud");
        logger.info("Iniciando listado de solicitudes de crédito");
        var list = myReactiveRepositoryAdapter.listAllEnrichedDTO()
                .doFirst(() -> logger.trace("[listRequest] Preparando flujo de datos"))
                .doOnSubscribe(s -> logger.debug("Suscrito al flujo de listado de solicitudes"))
                .doOnNext(item -> logger.debug("Solicitud listada: {}", item))
                .doOnError(err -> logger.error("Error durante el listado de solicitudes: {}", err.getMessage(), err))
                .doOnComplete(() -> logger.info("Listado de solicitudes completado"))
                .doFinally(signal -> logger.trace("[listRequest] Flujo finalizado con señal: {}", signal));

        logger.trace("[listRequest] Enviando respuesta 200 OK");
        return ServerResponse.ok().body(list, UserInfoDTO.class);
    }


}

