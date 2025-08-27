package co.com.projectve.api;

import co.com.projectve.api.dto.SaveRequestDTO;
import co.com.projectve.api.mapper.InfoUserDTOMapper;
//import co.com.projectve.model.infouser.InfoUser;
import co.com.projectve.usecase.infouser.InfoUserUseCase;
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

    public Mono<ServerResponse> saveRequestApi(ServerRequest request) {
        return request.bodyToMono(SaveRequestDTO.class)
                .map(infoUserDTOMapper::toModel) // Use the mapper to convert DTO to Model
                .flatMap(infoUserUseCase::execute)
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }
}
