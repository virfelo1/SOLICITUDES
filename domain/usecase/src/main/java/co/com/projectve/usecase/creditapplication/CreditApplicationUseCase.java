package co.com.projectve.usecase.creditapplication;


import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.usecase.creditapplication.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class CreditApplicationUseCase {
    private final CreditApplicationRepository creditApplicationRepository;

    private final List<String> CREDIT_TYPES = Arrays.asList("Hipotecario", "Vehiculo", "Libre Inversion", "Educacion");

    public Mono<CreditApplication> execute(CreditApplication creditApplication) {
        return validateCreditApplication(creditApplication)
                .flatMap(validatedUser -> {
                    validatedUser.setCreditStatus("Pendiente de revision");
                    return creditApplicationRepository.saveRequest(validatedUser);
                });
    }

    private Mono<CreditApplication> validateCreditApplication(CreditApplication creditApplication) { //no hacerlo en la logica de negocio hacerlo en entry-point
        return Mono.just(creditApplication)
                //.filter(iu -> iu.getDocumentType() != null && !iu.getDocumentType().isBlank())
                //.switchIfEmpty(Mono.error(new BusinessException("El tipo de documento es obligatorio.")))
                //.filter(iu -> iu.getDocumentNumber() != null)
                //.switchIfEmpty(Mono.error(new BusinessException("El número de documento es obligatorio.")))
                //.filter(iu -> iu.getCreditAmount() != null)
                //.switchIfEmpty(Mono.error(new BusinessException("El monto del crédito es obligatorio.")))
                //.filter(iu -> iu.getCreditTime() != null)
                //.switchIfEmpty(Mono.error(new BusinessException("El plazo del crédito es obligatorio.")))
                //.filter(iu -> iu.getTypeCredit() != null && !iu.getTypeCredit().isBlank())
                //.switchIfEmpty(Mono.error(new BusinessException("El tipo de crédito es obligatorio.")))
                .filter(iu -> CREDIT_TYPES.contains(iu.getTypeCredit()))
                .switchIfEmpty(Mono.error(new BusinessException("El tipo de préstamo no existe.")));
    }
}