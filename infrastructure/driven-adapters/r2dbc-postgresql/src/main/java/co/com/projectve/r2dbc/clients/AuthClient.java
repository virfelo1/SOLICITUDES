package co.com.projectve.r2dbc.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import co.com.projectve.shared.dto.UserInfoDTO;

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
    public Flux<UserInfoDTO> listAllUsers(String jwtToken) {
        logger.debug("Llamando a Auth Service para obtener todos los usuarios con token JWT.");
        return webClient.get()
                .uri("/api/v1/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken) // Añade el token aquí
                .retrieve()
                .bodyToFlux(UserInfoDTO.class)
                .doOnComplete(() -> logger.debug("Respuesta de Auth Service recibida para listado de usuarios."))
                .doOnError(e -> logger.error("Error al obtener el listado de usuarios de auth service: {}", e.getMessage()))
                .onErrorResume(e -> Flux.empty());
    }

    private Mono<String> resolveBearerToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    if (auth == null) return "";
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        return jwtAuth.getToken().getTokenValue();
                    }
                    Object principal = auth.getPrincipal();
                    if (principal instanceof Jwt jwt) {
                        return jwt.getTokenValue();
                    }
                    return "";
                })
                .defaultIfEmpty("");
    }

    public Flux<UserInfoDTO> listAllUsersFromContext() {
        return resolveBearerToken()
                .defaultIfEmpty("")
                .filter(token -> !token.isBlank())
                .flatMapMany(this::listAllUsers);
    }

    public Mono<Map<String, UserInfoDTO>> listAllUsersFromContextAsMap() {
        return listAllUsersFromContext()
                .collect(Collectors.toMap(
                        u -> u.getEmail() == null ? "" : u.getEmail().trim().toLowerCase(),
                        Function.identity(),
                        (a, b) -> a
                ));
    }
}


