package co.com.projectve.usecase.infouser;

import co.com.projectve.model.infouser.InfoUser;
import co.com.projectve.model.infouser.gateways.InfoUserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class InfoUserUseCase {
    private final InfoUserRepository infoUserRepository;

    private final List<String> CREDIT_TYPES = Arrays.asList("Hipotecario", "Vehiculo", "Libre Inversion", "Educacion");

    public Mono<InfoUser> execute(InfoUser infoUser) {
        return validateInfoUser(infoUser)
                .flatMap(validatedUser -> {
                    validatedUser.setCreditStatus("Pendiente de revision");
                    return infoUserRepository.saveRequest(validatedUser);
                });
    }

    private Mono<InfoUser> validateInfoUser(InfoUser infoUser) {
        return Mono.just(infoUser)
                .filter(iu -> iu.getDocumentType() != null && !iu.getDocumentType().isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El tipo de documento es obligatorio.")))
                .filter(iu -> iu.getDocumentNumber() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El número de documento es obligatorio.")))
                .filter(iu -> iu.getCreditAmount() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El monto del crédito es obligatorio.")))
                .filter(iu -> iu.getCreditTime() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El plazo del crédito es obligatorio.")))
                .filter(iu -> iu.getTypeCredit() != null && !iu.getTypeCredit().isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El tipo de crédito es obligatorio.")))
                .filter(iu -> CREDIT_TYPES.contains(iu.getTypeCredit()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El tipo de préstamo no existe.")));
    }
}