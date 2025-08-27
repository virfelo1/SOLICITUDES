package co.com.projectve.api;

import co.com.projectve.api.dto.SaveRequestDTO;
import co.com.projectve.api.mapper.InfoUserDTOMapper;
//import co.com.projectve.model.infouser.InfoUser;
import co.com.projectve.model.infouser.InfoUser;
import co.com.projectve.usecase.infouser.InfoUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class Handler {
//private  final UseCase useCase;
//private  final UseCase2 useCase2;
    private final InfoUserUseCase infoUserUseCase;
    private final InfoUserDTOMapper infoUserDTOMapper;

    //private final InfoUserUseCase infoUserUseCase;
    @Operation(summary = "Guarda una nueva solicitud de crédito",
            description = "Recibe los datos de una solicitud y la persiste en el sistema con el estado 'Pendiente de revision'.",
            tags = {"Solicitudes"},
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = SaveRequestDTO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud de crédito guardada exitosamente",
                            content = @Content(schema = @Schema(implementation = InfoUser.class))),
                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de la solicitud",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"El tipo de documento es obligatorio.\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })

    public Mono<ServerResponse> saveRequestApi(ServerRequest request) {
        return request.bodyToMono(SaveRequestDTO.class)
                .map(infoUserDTOMapper::toModel)
                .flatMap(infoUserUseCase::execute)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
                //.onErrorResume(e -> ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }
}
