package co.com.projectve.usecase.creditapplication;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Tests para CreditApplicationUseCase - Caso de Uso")
class CreditApplicationUseCaseTest {

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    private CreditApplicationUseCase creditApplicationUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        creditApplicationUseCase = new CreditApplicationUseCase(creditApplicationRepository);
    }

    @Test
    @DisplayName("Debería ejecutar el caso de uso exitosamente y cambiar el estado a 'Pendiente de revision'")
    void shouldExecuteUseCaseSuccessfullyAndChangeStatus() {
        // Arrange
        CreditApplication inputCreditApplication = CreditApplication.builder()
                .id(1)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .typeCredit("Hipotecario")
                .creditStatus("Nuevo")
                .build();

        CreditApplication expectedCreditApplication = CreditApplication.builder()
                .id(1)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .typeCredit("Hipotecario")
                .creditStatus("Pendiente de revision")
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(expectedCreditApplication));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.execute(inputCreditApplication);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedCreditApplication)
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).saveRequest(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debería mantener el ID original en la solicitud procesada")
    void shouldMaintainOriginalIdInProcessedRequest() {
        // Arrange
        CreditApplication inputCreditApplication = CreditApplication.builder()
                .id(999)
                .documentType("CE")
                .documentNumber("987654321")
                .creditAmount(new BigDecimal("10000000.00"))
                .creditTime(24)
                .typeCredit("Vehiculo")
                .creditStatus("Nuevo")
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(inputCreditApplication));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.execute(inputCreditApplication);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedApplication -> 
                    savedApplication.getId().equals(999) &&
                    savedApplication.getCreditStatus().equals("Pendiente de revision"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería llamar al repositorio con la solicitud modificada")
    void shouldCallRepositoryWithModifiedRequest() {
        // Arrange
        CreditApplication inputCreditApplication = CreditApplication.builder()
                .documentType("TI")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("20000000.00"))
                .creditTime(36)
                .typeCredit("Libre Inversion")
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(inputCreditApplication));

        // Act
        creditApplicationUseCase.execute(inputCreditApplication).subscribe();

        // Assert
        verify(creditApplicationRepository, times(1)).saveRequest(argThat(application ->
                application.getCreditStatus().equals("Pendiente de revision") &&
                application.getDocumentType().equals("TI") &&
                application.getDocumentNumber().equals("123456789")
        ));
    }

    @Test
    @DisplayName("Debería manejar solicitudes con diferentes tipos de crédito")
    void shouldHandleDifferentCreditTypes() {
        // Arrange
        CreditApplication hipotecarioApp = CreditApplication.builder()
                .documentType("CC")
                .documentNumber("111111111")
                .creditAmount(new BigDecimal("100000000.00"))
                .creditTime(120)
                .typeCredit("Hipotecario")
                .build();

        CreditApplication vehiculoApp = CreditApplication.builder()
                .documentType("CC")
                .documentNumber("222222222")
                .creditAmount(new BigDecimal("30000000.00"))
                .creditTime(48)
                .typeCredit("Vehiculo")
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(hipotecarioApp))
                .thenReturn(Mono.just(vehiculoApp));

        // Act & Assert
        StepVerifier.create(creditApplicationUseCase.execute(hipotecarioApp))
                .expectNextMatches(app -> app.getTypeCredit().equals("Hipotecario"))
                .verifyComplete();

        StepVerifier.create(creditApplicationUseCase.execute(vehiculoApp))
                .expectNextMatches(app -> app.getTypeCredit().equals("Vehiculo"))
                .verifyComplete();
    }
}

