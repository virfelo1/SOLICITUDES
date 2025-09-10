package co.com.projectve.api;

import co.com.projectve.api.dto.CreditApplicationDTO;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.r2dbc.dto.CreditApplicationListViewDTO;
import co.com.projectve.shared.dto.PageResponse;
import co.com.projectve.shared.dto.CreditApplicationEnrichedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "Solicitudes", description = "Endpoints para la gestión de solicitudes de crédito")
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "saveRequestApi",
                    operation = @Operation(
                            summary = "Guarda una nueva solicitud de crédito",
                            description = "Recibe los datos de una solicitud y la persiste en el sistema.",
                            operationId = "saveRequestApi",
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(implementation = CreditApplicationDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Solicitud guardada exitosamente",
                                            content = @Content(schema = @Schema(implementation = CreditApplication.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error de validación",
                                            content = @Content(schema = @Schema(implementation = String.class, example = "{\"error\":\"El tipo de documento es obligatorio.\"}"))
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listRequest",
                    operation = @Operation(
                            summary = "Lista todas las solicitudes de crédito",
                            description = "Retorna el listado paginado con información enriquecida (estado y tipo de préstamo).",
                            operationId = "listRequest",
                            parameters = {
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente",
                                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitud"), handler::saveRequestApi)
                .andRoute(GET("/api/v1/solicitud"), handler::listRequest); //este es el endpoint para el listado de solicitudes
        // .andRoute(POST("/api/usecase/otherpath"), handler::listenPOSTUseCase)
    }
}
