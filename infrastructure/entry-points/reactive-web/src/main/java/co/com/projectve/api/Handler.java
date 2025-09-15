package co.com.projectve.api;

import co.com.projectve.api.dto.CreditApplicationDTO;
import co.com.projectve.api.dto.UpdateStateDTO;
import co.com.projectve.api.mapper.CreditApplicationDTOMapper;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.r2dbc.MyReactiveRepositoryAdapter;
import co.com.projectve.r2dbc.dto.CreditApplicationListViewDTO;
import co.com.projectve.shared.dto.PageResponse;
import co.com.projectve.shared.dto.UserInfoDTO;
import co.com.projectve.usecase.creditapplication.CreditApplicationUseCase;
import co.com.projectve.usecase.capacity.CapacityCalculationUseCase;
import co.com.projectve.model.creditapplication.gateways.EnrichedCapacityCalculationService;
import co.com.projectve.api.dto.CapacityTestResponseDTO;
import co.com.projectve.api.mapper.CapacityTestMapper;
import co.com.projectve.model.creditapplication.gateways.CapacityCalculationGateway;
import co.com.projectve.model.creditapplication.gateways.ActiveLoanRepository;
import co.com.projectve.shared.clients.AuthClient;
import co.com.projectve.shared.dto.UserInfoDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
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
    private final CapacityCalculationUseCase capacityCalculationUseCase;
    private final EnrichedCapacityCalculationService enrichedCapacityCalculationService;
    private final CapacityCalculationGateway capacityCalculationGateway;
    private final ActiveLoanRepository activeLoanRepository;
    private final CapacityTestMapper capacityTestMapper;
    private final AuthClient authClient;

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
        logger.info("Iniciando listado de solicitudes de credito");
        int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        String nameState = serverRequest.queryParam("nameState").orElse(null);
        String nameLoan = serverRequest.queryParam("nameLoan").orElse(null);

        var pageMono = myReactiveRepositoryAdapter.listAllEnrichedDTOPage(page, size, nameState, nameLoan)
                .doFirst(() -> logger.trace("[listRequest] Preparando flujo de datos paginados"))
                .doOnSubscribe(s -> logger.debug("Suscrito al flujo de listado paginado de solicitudes"))
                .doOnError(err -> logger.error("Error durante el listado de solicitudes: {}", err.getMessage(), err))
                .doFinally(signal -> logger.trace("[listRequest] Flujo finalizado con señal: {}", signal));

        logger.trace("[listRequest] Enviando respuesta 200 OK");
        return ServerResponse.ok().body(pageMono, PageResponse.class);
    }

    @Operation(
            summary = "Actualiza el estado de una solicitud",
            description = "Permite a un 'Asesor' cambiar el estado de una solicitud a 'Aprobado' o 'Rechazado'.",
            tags = {"Solicitudes"},
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = UpdateStateDTO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado de solicitud actualizado exitosamente",
                            content = @Content(schema = @Schema(implementation = CreditApplication.class))),
                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada o solicitud no encontrada",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"Solicitud con email 'juan.garcia@email.com' no encontrada.\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public Mono<ServerResponse> updateState(ServerRequest serverRequest){
        logger.trace("[updateState] Recibida solicitud PUT /api/v1/solicitud");

        return serverRequest.bodyToMono(UpdateStateDTO.class)
                .flatMap(dto -> {
                    logger.trace("[updateState] Iniciando validación de DTO");
                    Set<ConstraintViolation<UpdateStateDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        logger.error("[updateState] DTO inválido: {} violaciones", violations.size());
                        return Mono.error(new ConstraintViolationException(violations));
                    }

                    logger.trace("[updateState] DTO válido. Llamando a la lógica en el caso de uso para actualizar estado y notificar.");
                    return creditApplicationUseCase.updateStateAndNotify(dto.email(), dto.idRequest(), dto.state());
                })
                .flatMap(response -> {
                    logger.trace("[updateState] Enviando respuesta 200 OK con la solicitud actualizada.");
                    return ServerResponse.ok().bodyValue(response);
                });
    }

    @Operation(
            summary = "Calcula la capacidad de endeudamiento",
            description = "Encola una solicitud para evaluación de capacidad de endeudamiento por Lambda externa.",
            tags = {"Solicitudes"},
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = CreditApplicationDTO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud encolada exitosamente",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"message\":\"Solicitud encolada exitosamente\"}"))),
                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"El tipo de documento es obligatorio.\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public Mono<ServerResponse> BorrowingCapacity(ServerRequest serverRequest){
        logger.trace("[BorrowingCapacity] Recibida solicitud POST /api/v1/calcular-capacidad");
        
        return serverRequest.bodyToMono(CreditApplicationDTO.class)
                .flatMap(dto -> {
                    logger.trace("[BorrowingCapacity] Iniciando validación de DTO");
                    Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        logger.error("[BorrowingCapacity] DTO inválido: {} violaciones", violations.size());
                        return Mono.error(new ConstraintViolationException(violations));
                    }

                    logger.trace("[BorrowingCapacity] DTO válido. Mapeando a modelo de dominio");
                    CreditApplication creditApplication = creditApplicationDTOMapper.toModel(dto);
                    
                    logger.debug("[BorrowingCapacity] Ejecutando caso de uso para documento: {} {}", 
                               creditApplication.getDocumentType(), creditApplication.getDocumentNumber());
                    
                    return enrichedCapacityCalculationService.enqueueEnrichedCapacityCalculation(creditApplication)
                            .then(Mono.fromCallable(() -> {
                                Map<String, String> response = new java.util.HashMap<>();
                                response.put("message", "Solicitud de cálculo de capacidad encolada exitosamente. Recibirá el resultado por correo electrónico.");
                                response.put("idRequest", creditApplication.getIdRequest().toString());
                                return response;
                            }));
                })
                .doOnSuccess(response -> logger.info("[BorrowingCapacity] Solicitud encolada exitosamente. ID: {}", 
                           response.get("idRequest")))
                .doOnError(error -> logger.error("[BorrowingCapacity] Error encolando solicitud: {}", error.getMessage(), error))
                            .flatMap(response -> {
                                logger.trace("[BorrowingCapacity] Enviando respuesta 200 OK");
                                return ServerResponse.ok().bodyValue(response);
                            });
                }

    @Operation(
            summary = "Prueba el cálculo de capacidad de endeudamiento",
            description = "Endpoint para probar el cálculo de deuda mensual actual y capacidad de endeudamiento usando el salario base del usuario.",
            tags = {"Pruebas"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cálculo exitoso",
                            content = @Content(schema = @Schema(implementation = CapacityTestResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Email requerido",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })
    public Mono<ServerResponse> testCapacityCalculation(ServerRequest serverRequest) {
        logger.trace("[testCapacityCalculation] Recibida solicitud GET /api/v1/test-capacity");
        
        // Obtener email de los query parameters
        return serverRequest.queryParam("email")
                .map(email -> {
                    logger.info("[testCapacityCalculation] Calculando capacidad para email: {}", email);
                    
                    return authClient.listAllUsersFromContextAsMap()
                            .flatMap(usersMap -> {
                                UserInfoDTO user = usersMap.get(email);
                                if (user == null || user.getBaseSalary() == null) {
                                    logger.warn("[testCapacityCalculation] Usuario no encontrado o sin salario base: {}", email);
                                    return Mono.error(new RuntimeException("Usuario no encontrado o sin salario base"));
                                }
                                
                                logger.debug("[testCapacityCalculation] Usuario encontrado - Salario base: {}", user.getBaseSalary());
                                
                                return capacityCalculationGateway.calculateBorrowingCapacity(email, user.getBaseSalary())
                                        .zipWith(activeLoanRepository.findActiveLoansByEmail(email).collectList())
                                        .map(tuple -> {
                                            var result = tuple.getT1();
                                            var activeLoans = tuple.getT2();
                                            
                                            logger.info("[testCapacityCalculation] Cálculo completado - Decisión: {}, Capacidad: {}, Deuda: {}", 
                                                      result.decision(), result.availableCapacity(), result.currentMonthlyDebt());
                                            
                                            return capacityTestMapper.toResponseDTO(email, user.getBaseSalary(), result, activeLoans);
                                        });
                            });
                })
                .orElse(Mono.error(new RuntimeException("Email es requerido como query parameter")))
                .doOnSuccess(response -> logger.info("[testCapacityCalculation] Cálculo exitoso para email: {}", 
                           response.getEmail()))
                .doOnError(error -> logger.error("[testCapacityCalculation] Error en cálculo: {}", error.getMessage(), error))
                .flatMap(response -> {
                    logger.trace("[testCapacityCalculation] Enviando respuesta 200 OK");
                    return ServerResponse.ok().bodyValue(response);
                });
    }
}

