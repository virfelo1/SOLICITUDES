package co.com.projectve.api;

import co.com.projectve.api.dto.CreditApplicationDTO;
import co.com.projectve.api.dto.UpdateStateDTO;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.r2dbc.dto.CreditApplicationListViewDTO;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
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
                            description = "Retorna el listado con información enriquecida (estado y tipo de préstamo).",
                            operationId = "listRequest",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente",
                                            content = @Content(schema = @Schema(implementation = CreditApplicationListViewDTO.class))
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "updateState",
                    operation = @Operation(
                            summary = "Actualiza el estado de una solicitud",
                            description = "Permite a un 'Asesor' cambiar el estado de una solicitud a 'Aprobado' o 'Rechazado'.",
                            operationId = "updateState",
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(implementation = UpdateStateDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Estado de solicitud actualizado exitosamente",
                                            content = @Content(schema = @Schema(implementation = CreditApplication.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada o solicitud no encontrada",
                                            content = @Content(schema = @Schema(implementation = String.class, example = "{\"error\":\"Solicitud con email 'librecarbon@gmail.com' no encontrada.\"}"))
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/test-capacity",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "testCapacityCalculation",
                    operation = @Operation(
                            summary = "Prueba el cálculo de capacidad de endeudamiento",
                            description = "Endpoint para probar el cálculo de deuda mensual actual y capacidad de endeudamiento usando el salario base del usuario.",
                            operationId = "testCapacityCalculation",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Cálculo exitoso",
                                            content = @Content(schema = @Schema(implementation = String.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Email requerido",
                                            content = @Content(schema = @Schema(implementation = String.class))
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitud"), handler::saveRequestApi)
                .andRoute(GET("/api/v1/solicitud"), handler::listRequest) //este es el endpoint para el listado de solicitudes
                .andRoute(PUT("/api/v1/solicitud"), handler::updateState)
                .andRoute(POST( "/api/v1/calcular-capacidad"), handler::BorrowingCapacity)
                .andRoute(GET("/api/v1/test-capacity"), handler::testCapacityCalculation);
        // .andRoute(POST("/api/usecase/otherpath"), handler::listenPOSTUseCase)
    }
}
