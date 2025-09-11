package co.com.projectve.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        logger.info("Inicializando SecurityWebFilterChain para OAuth2 Resource Server (JWT)");
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {})
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitud").hasAnyRole("ADMIN", "ASESOR")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        logger.info("Creando ReactiveJwtDecoder con clave HMAC de longitud: {} bytes", jwtSecret != null ? jwtSecret.getBytes().length : -1);
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String subject = jwt.getSubject();
            List<String> roles = jwt.getClaimAsStringList("roles");
            logger.info("JWT recibido - subject: {}", subject);
            logger.debug("JWT claims: {}", jwt.getClaims());
            if (roles == null || roles.isEmpty()) {
                logger.warn("JWT sin roles o lista vacía");
                return List.of();
            }
            List<GrantedAuthority> granted = roles.stream()
                    .map(role -> {
                        String normalized = "ROLE_" + role.toUpperCase();
                        logger.info("Asignando autoridad desde rol JWT: {} -> {}", role, normalized);
                        return new SimpleGrantedAuthority(normalized);
                    })
                    .collect(Collectors.toList());
            logger.info("Autoridades extraídas del JWT: {}", granted);
            return granted;
        });
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}