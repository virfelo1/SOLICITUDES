package co.com.projectve.shared.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.com.projectve.shared.dto.UserListDTO;

@Component
public class AuthClient {

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(AuthClient.class);

    public AuthClient(@Value("${auth.service.base-url}") String authBaseUrl) {
        logger.info("Inicializando AuthClient con URL base: {}", authBaseUrl);
        this.webClient = WebClient.builder()
                .baseUrl(authBaseUrl)
                .build();
    }

    /**
     * Obtiene una lista completa de usuarios del microservicio de autenticación,
     * incluyendo el token JWT en el encabezado de la solicitud.
     * @param jwtToken El token JWT para la autorización.
     * @return Flux que emite una lista de objetos UserListDTO.
     */
    public Flux<UserListDTO> listAllUsers(String jwtToken) {
        logger.debug("Llamando a Auth Service para obtener todos los usuarios con token JWT.");
        return webClient.get()
                .uri("/api/v1/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken) // Añade el token aquí
                .retrieve()
                .bodyToFlux(UserListDTO.class)
                .doOnComplete(() -> logger.debug("Respuesta de Auth Service recibida para listado de usuarios."))
                .doOnError(e -> logger.error("Error al obtener el listado de usuarios de auth service: {}", e.getMessage()))
                .onErrorResume(e -> Flux.empty());
    }
}


