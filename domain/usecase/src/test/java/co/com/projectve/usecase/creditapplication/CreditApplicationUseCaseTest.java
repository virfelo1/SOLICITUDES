package co.com.projectve.usecase.creditapplication;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CreditApplicationRepository;
import co.com.projectve.model.creditapplication.gateways.NotificationService;
import co.com.projectve.model.creditapplication.gateways.AutomaticValidationService;
import co.com.projectve.usecase.creditapplication.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CreditApplicationUseCase - Caso de Uso")
class CreditApplicationUseCaseTest {

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AutomaticValidationService automaticValidationService;

    private CreditApplicationUseCase creditApplicationUseCase;

    @BeforeEach
    void setUp() {
        creditApplicationUseCase = new CreditApplicationUseCase(
                creditApplicationRepository,
                notificationService,
                automaticValidationService
        );
    }

    @Test
    @DisplayName("Debería ejecutar el caso de uso exitosamente y cambiar el estado a 'Pendiente de revision'")
    void shouldExecuteUseCaseSuccessfullyAndChangeStatus() {
        // Arrange
        CreditApplication inputCreditApplication = CreditApplication.builder()
                .idRequest(1)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .idLoanType((short) 1)
                .email("test@example.com")
                .build();

        CreditApplication savedCreditApplication = CreditApplication.builder()
                .idRequest(1)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .idLoanType((short) 1)
                .email("test@example.com")
                .idState((short) 1)
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(savedCreditApplication));
        when(automaticValidationService.processAutomaticValidation(any(CreditApplication.class)))
                .thenReturn(Mono.just(savedCreditApplication));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.execute(inputCreditApplication);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedCreditApplication)
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).saveRequest(any(CreditApplication.class));
        verify(automaticValidationService, times(1)).processAutomaticValidation(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debería mantener el ID original en la solicitud procesada")
    void shouldMaintainOriginalIdInProcessedRequest() {
        // Arrange
        CreditApplication inputCreditApplication = CreditApplication.builder()
                .idRequest(999)
                .documentType("CE")
                .documentNumber("987654321")
                .creditAmount(new BigDecimal("10000000.00"))
                .creditTime(24)
                .idLoanType((short) 2)
                .email("test999@example.com")
                .build();

        CreditApplication savedCreditApplication = CreditApplication.builder()
                .idRequest(999)
                .documentType("CE")
                .documentNumber("987654321")
                .creditAmount(new BigDecimal("10000000.00"))
                .creditTime(24)
                .idLoanType((short) 2)
                .email("test999@example.com")
                .idState((short) 1)
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(savedCreditApplication));
        when(automaticValidationService.processAutomaticValidation(any(CreditApplication.class)))
                .thenReturn(Mono.just(savedCreditApplication));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.execute(inputCreditApplication);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedApplication -> 
                    savedApplication.getIdRequest().equals(999) &&
                    savedApplication.getIdState().equals((short) 1))
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
                .idLoanType((short) 3)
                .email("test@example.com")
                .build();

        CreditApplication savedCreditApplication = CreditApplication.builder()
                .documentType("TI")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("20000000.00"))
                .creditTime(36)
                .idLoanType((short) 3)
                .email("test@example.com")
                .idState((short) 1)
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(savedCreditApplication));
        when(automaticValidationService.processAutomaticValidation(any(CreditApplication.class)))
                .thenReturn(Mono.just(savedCreditApplication));

        // Act
        creditApplicationUseCase.execute(inputCreditApplication).subscribe();

        // Assert
        verify(creditApplicationRepository, times(1)).saveRequest(argThat(application ->
                application.getIdState().equals((short) 1) &&
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
                .idLoanType((short) 1)
                .email("hipotecario@example.com")
                .build();

        CreditApplication vehiculoApp = CreditApplication.builder()
                .documentType("CC")
                .documentNumber("222222222")
                .creditAmount(new BigDecimal("30000000.00"))
                .creditTime(48)
                .idLoanType((short) 2)
                .email("vehiculo@example.com")
                .build();

        when(creditApplicationRepository.saveRequest(any(CreditApplication.class)))
                .thenReturn(Mono.just(hipotecarioApp))
                .thenReturn(Mono.just(vehiculoApp));
        when(automaticValidationService.processAutomaticValidation(any(CreditApplication.class)))
                .thenReturn(Mono.just(hipotecarioApp))
                .thenReturn(Mono.just(vehiculoApp));

        // Act & Assert
        StepVerifier.create(creditApplicationUseCase.execute(hipotecarioApp))
                .expectNextMatches(app -> app.getIdLoanType().equals((short) 1))
                .verifyComplete();

        StepVerifier.create(creditApplicationUseCase.execute(vehiculoApp))
                .expectNextMatches(app -> app.getIdLoanType().equals((short) 2))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener todas las solicitudes enriquecidas")
    void shouldGetAllEnrichedApplications() {
        // Arrange
        List<CreditApplication> expectedApplications = initDataToList();
        when(creditApplicationRepository.listAllEnriched())
                .thenReturn(Flux.fromIterable(expectedApplications));

        // Act
        Flux<CreditApplication> result = creditApplicationUseCase.listAllEnriched();

        // Assert
        StepVerifier.create(result)
                .expectNextCount(10)
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).listAllEnriched();
    }

    @Test
    @DisplayName("Debería buscar solicitud por email e idRequest")
    void shouldFindByEmailAndIdRequest() {
        // Arrange
        String email = "test@example.com";
        Integer idRequest = 12345;
        CreditApplication expectedApplication = CreditApplication.builder()
                .idRequest(idRequest)
                .email(email)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .idLoanType((short) 1)
                .idState((short) 1)
                .build();

        when(creditApplicationRepository.findByEmailAndIdRequest(email, idRequest))
                .thenReturn(Mono.just(expectedApplication));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.findByEmailAndIdRequest(email, idRequest);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedApplication)
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).findByEmailAndIdRequest(email, idRequest);
    }

    @Test
    @DisplayName("Debería actualizar estado y enviar notificación cuando es Aprobado")
    void shouldUpdateStateAndNotifyWhenApproved() {
        // Arrange
        String email = "test@example.com";
        Integer idRequest = 12345;
        String state = "Aprobado";
        
        CreditApplication foundApplication = CreditApplication.builder()
                .idRequest(idRequest)
                .email(email)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .idLoanType((short) 1)
                .idState((short) 1)
                .build();

        CreditApplication updatedApplication = CreditApplication.builder()
                .idRequest(idRequest)
                .email(email)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .idLoanType((short) 1)
                .idState((short) 2)
                .build();

        when(creditApplicationRepository.findByEmailAndIdRequest(email, idRequest))
                .thenReturn(Mono.just(foundApplication));
        when(creditApplicationRepository.updateState(any(CreditApplication.class)))
                .thenReturn(Mono.just(updatedApplication));
        when(notificationService.sendNotification(anyString()))
                .thenReturn(Mono.empty());

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.updateStateAndNotify(email, idRequest, state);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getIdState().equals((short) 2))
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).findByEmailAndIdRequest(email, idRequest);
        verify(creditApplicationRepository, times(1)).updateState(any(CreditApplication.class));
        verify(notificationService, times(1)).sendNotification(anyString());
    }

    @Test
    @DisplayName("Debería actualizar estado y enviar notificación cuando es Rechazado")
    void shouldUpdateStateAndNotifyWhenRejected() {
        // Arrange
        String email = "test@example.com";
        Integer idRequest = 12346;
        String state = "Rechazado";
        
        CreditApplication foundApplication = CreditApplication.builder()
                .idRequest(idRequest)
                .email(email)
                .documentType("CC")
                .documentNumber("987654321")
                .creditAmount(new BigDecimal("25000000.00"))
                .creditTime(36)
                .idLoanType((short) 2)
                .idState((short) 1)
                .build();

        CreditApplication updatedApplication = CreditApplication.builder()
                .idRequest(idRequest)
                .email(email)
                .documentType("CC")
                .documentNumber("987654321")
                .creditAmount(new BigDecimal("25000000.00"))
                .creditTime(36)
                .idLoanType((short) 2)
                .idState((short) 3)
                .build();

        when(creditApplicationRepository.findByEmailAndIdRequest(email, idRequest))
                .thenReturn(Mono.just(foundApplication));
        when(creditApplicationRepository.updateState(any(CreditApplication.class)))
                .thenReturn(Mono.just(updatedApplication));
        when(notificationService.sendNotification(anyString()))
                .thenReturn(Mono.empty());

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.updateStateAndNotify(email, idRequest, state);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getIdState().equals((short) 3))
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).findByEmailAndIdRequest(email, idRequest);
        verify(creditApplicationRepository, times(1)).updateState(any(CreditApplication.class));
        verify(notificationService, times(1)).sendNotification(anyString());
    }

    @Test
    @DisplayName("Debería actualizar estado sin enviar notificación para otros estados")
    void shouldUpdateStateWithoutNotificationForOtherStates() {
        // Arrange
        String email = "test@example.com";
        Integer idRequest = 12347;
        String state = "En Proceso";
        
        CreditApplication foundApplication = CreditApplication.builder()
                .idRequest(idRequest)
                .email(email)
                .documentType("TI")
                .documentNumber("111111111")
                .creditAmount(new BigDecimal("15000000.00"))
                .creditTime(24)
                .idLoanType((short) 3)
                .idState((short) 1)
                .build();

        CreditApplication updatedApplication = CreditApplication.builder()
                .idRequest(idRequest)
                .email(email)
                .documentType("TI")
                .documentNumber("111111111")
                .creditAmount(new BigDecimal("15000000.00"))
                .creditTime(24)
                .idLoanType((short) 3)
                .idState((short) 1)
                .build();

        when(creditApplicationRepository.findByEmailAndIdRequest(email, idRequest))
                .thenReturn(Mono.just(foundApplication));
        when(creditApplicationRepository.updateState(any(CreditApplication.class)))
                .thenReturn(Mono.just(updatedApplication));

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.updateStateAndNotify(email, idRequest, state);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getIdState().equals((short) 1))
                .verifyComplete();

        verify(creditApplicationRepository, times(1)).findByEmailAndIdRequest(email, idRequest);
        verify(creditApplicationRepository, times(1)).updateState(any(CreditApplication.class));
        verify(notificationService, never()).sendNotification(anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando no encuentra la solicitud")
    void shouldThrowExceptionWhenApplicationNotFound() {
        // Arrange
        String email = "notfound@example.com";
        Integer idRequest = 99999;
        String state = "Aprobado";

        when(creditApplicationRepository.findByEmailAndIdRequest(email, idRequest))
                .thenReturn(Mono.empty());

        // Act
        Mono<CreditApplication> result = creditApplicationUseCase.updateStateAndNotify(email, idRequest, state);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().contains("Solicitud con email '" + email + "' y idRequest '" + idRequest + "' no encontrada."))
                .verify();

        verify(creditApplicationRepository, times(1)).findByEmailAndIdRequest(email, idRequest);
        verify(creditApplicationRepository, never()).updateState(any(CreditApplication.class));
        verify(notificationService, never()).sendNotification(anyString());
    }

    private List<CreditApplication> initDataToList() {
        return IntStream.range(0, 10)
                .mapToObj(index -> CreditApplication.builder()
                        .idRequest(index + 1)
                        .email(String.format("test%d@example.com", index))
                        .documentType("CC")
                        .documentNumber(String.format("12345678%d", index))
                        .creditAmount(new BigDecimal(String.format("%d0000000.00", (index + 1) * 5)))
                        .creditTime((index + 1) * 12)
                        .idLoanType((short) ((index % 3) + 1))
                        .idState((short) 1)
                        .build())
                .collect(toList());
    }
}

