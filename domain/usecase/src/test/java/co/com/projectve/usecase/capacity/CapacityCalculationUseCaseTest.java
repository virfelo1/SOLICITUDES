package co.com.projectve.usecase.capacity;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.gateways.CapacityCalculationService;
import co.com.projectve.model.creditapplication.gateways.LoanTypeInfo;
import co.com.projectve.model.creditapplication.gateways.LoanTypeRepository;
import co.com.projectve.usecase.capacity.exception.CapacityCalculationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CapacityCalculationUseCase - Caso de Uso de Cálculo de Capacidad")
class CapacityCalculationUseCaseTest {

    @Mock
    private CapacityCalculationService capacityCalculationService;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    private CapacityCalculationUseCase capacityCalculationUseCase;

    @BeforeEach
    void setUp() {
        capacityCalculationUseCase = new CapacityCalculationUseCase(
                capacityCalculationService,
                loanTypeRepository
        );
    }

    @Test
    @DisplayName("Debería encolar cálculo de capacidad exitosamente")
    void shouldEnqueueCapacityCalculationSuccessfully() {
        // Arrange
        CreditApplication creditApplication = CreditApplication.builder()
                .idRequest(12345)
                .email("test@example.com")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .idLoanType((short) 1)
                .build();

        LoanTypeInfo loanTypeInfo = new LoanTypeInfo(
                (short) 1,
                "Hipotecario",
                true,
                new BigDecimal("100000000.00"),
                new BigDecimal("10000000.00"),
                8.5
        );

        when(loanTypeRepository.findLoanTypeById(any(Short.class)))
                .thenReturn(Mono.just(loanTypeInfo));
        when(capacityCalculationService.enqueueCapacityCalculation(any(CreditApplication.class)))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = capacityCalculationUseCase.enqueueCapacityCalculation(creditApplication);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(loanTypeRepository, times(1)).findLoanTypeById((short) 1);
        verify(capacityCalculationService, times(1)).enqueueCapacityCalculation(creditApplication);
    }

    @Test
    @DisplayName("Debería manejar error cuando no se encuentra el tipo de préstamo")
    void shouldHandleErrorWhenLoanTypeNotFound() {
        // Arrange
        CreditApplication creditApplication = CreditApplication.builder()
                .idRequest(12346)
                .email("test2@example.com")
                .creditAmount(new BigDecimal("25000000.00"))
                .creditTime(36)
                .idLoanType((short) 999)
                .build();

        when(loanTypeRepository.findLoanTypeById(any(Short.class)))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = capacityCalculationUseCase.enqueueCapacityCalculation(creditApplication);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CapacityCalculationException &&
                        throwable.getMessage().equals("Error encolando cálculo de capacidad"))
                .verify();

        verify(loanTypeRepository, times(1)).findLoanTypeById((short) 999);
        verify(capacityCalculationService, never()).enqueueCapacityCalculation(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debería manejar error cuando falla el servicio de encolamiento")
    void shouldHandleErrorWhenEnqueueServiceFails() {
        // Arrange
        CreditApplication creditApplication = CreditApplication.builder()
                .idRequest(12347)
                .email("test3@example.com")
                .creditAmount(new BigDecimal("75000000.00"))
                .creditTime(48)
                .idLoanType((short) 2)
                .build();

        LoanTypeInfo loanTypeInfo = new LoanTypeInfo(
                (short) 2,
                "Vehicular",
                true,
                new BigDecimal("80000000.00"),
                new BigDecimal("5000000.00"),
                12.0
        );

        when(loanTypeRepository.findLoanTypeById(any(Short.class)))
                .thenReturn(Mono.just(loanTypeInfo));
        when(capacityCalculationService.enqueueCapacityCalculation(any(CreditApplication.class)))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Act
        Mono<Void> result = capacityCalculationUseCase.enqueueCapacityCalculation(creditApplication);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CapacityCalculationException &&
                        throwable.getMessage().equals("Error encolando cálculo de capacidad"))
                .verify();

        verify(loanTypeRepository, times(1)).findLoanTypeById((short) 2);
        verify(capacityCalculationService, times(1)).enqueueCapacityCalculation(creditApplication);
    }

    @Test
    @DisplayName("Debería procesar diferentes tipos de préstamo correctamente")
    void shouldProcessDifferentLoanTypesCorrectly() {
        // Arrange
        CreditApplication hipotecarioApp = CreditApplication.builder()
                .idRequest(12348)
                .email("hipotecario@example.com")
                .creditAmount(new BigDecimal("100000000.00"))
                .creditTime(120)
                .idLoanType((short) 1)
                .build();

        CreditApplication vehicularApp = CreditApplication.builder()
                .idRequest(12349)
                .email("vehicular@example.com")
                .creditAmount(new BigDecimal("40000000.00"))
                .creditTime(60)
                .idLoanType((short) 2)
                .build();

        LoanTypeInfo hipotecarioInfo = new LoanTypeInfo(
                (short) 1,
                "Hipotecario",
                true,
                new BigDecimal("200000000.00"),
                new BigDecimal("20000000.00"),
                7.5
        );

        LoanTypeInfo vehicularInfo = new LoanTypeInfo(
                (short) 2,
                "Vehicular",
                false,
                new BigDecimal("80000000.00"),
                new BigDecimal("10000000.00"),
                11.5
        );

        when(loanTypeRepository.findLoanTypeById((short) 1))
                .thenReturn(Mono.just(hipotecarioInfo));
        when(loanTypeRepository.findLoanTypeById((short) 2))
                .thenReturn(Mono.just(vehicularInfo));
        when(capacityCalculationService.enqueueCapacityCalculation(any(CreditApplication.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(capacityCalculationUseCase.enqueueCapacityCalculation(hipotecarioApp))
                .verifyComplete();

        StepVerifier.create(capacityCalculationUseCase.enqueueCapacityCalculation(vehicularApp))
                .verifyComplete();

        verify(loanTypeRepository, times(1)).findLoanTypeById((short) 1);
        verify(loanTypeRepository, times(1)).findLoanTypeById((short) 2);
        verify(capacityCalculationService, times(2)).enqueueCapacityCalculation(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debería manejar solicitudes con diferentes montos y plazos")
    void shouldHandleDifferentAmountsAndTerms() {
        // Arrange
        CreditApplication smallAmountApp = CreditApplication.builder()
                .idRequest(12350)
                .email("small@example.com")
                .creditAmount(new BigDecimal("10000000.00"))
                .creditTime(12)
                .idLoanType((short) 3)
                .build();

        CreditApplication largeAmountApp = CreditApplication.builder()
                .idRequest(12351)
                .email("large@example.com")
                .creditAmount(new BigDecimal("150000000.00"))
                .creditTime(180)
                .idLoanType((short) 1)
                .build();

        LoanTypeInfo libreInversionInfo = new LoanTypeInfo(
                (short) 3,
                "Libre Inversión",
                true,
                new BigDecimal("50000000.00"),
                new BigDecimal("1000000.00"),
                15.0
        );

        LoanTypeInfo hipotecarioInfo = new LoanTypeInfo(
                (short) 1,
                "Hipotecario",
                true,
                new BigDecimal("200000000.00"),
                new BigDecimal("20000000.00"),
                7.5
        );

        when(loanTypeRepository.findLoanTypeById((short) 3))
                .thenReturn(Mono.just(libreInversionInfo));
        when(loanTypeRepository.findLoanTypeById((short) 1))
                .thenReturn(Mono.just(hipotecarioInfo));
        when(capacityCalculationService.enqueueCapacityCalculation(any(CreditApplication.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(capacityCalculationUseCase.enqueueCapacityCalculation(smallAmountApp))
                .verifyComplete();

        StepVerifier.create(capacityCalculationUseCase.enqueueCapacityCalculation(largeAmountApp))
                .verifyComplete();

        verify(capacityCalculationService, times(2)).enqueueCapacityCalculation(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debería generar requestId único para cada solicitud")
    void shouldGenerateUniqueRequestIdForEachRequest() {
        // Arrange
        CreditApplication creditApplication1 = CreditApplication.builder()
                .idRequest(12352)
                .email("request1@example.com")
                .creditAmount(new BigDecimal("30000000.00"))
                .creditTime(24)
                .idLoanType((short) 2)
                .build();

        CreditApplication creditApplication2 = CreditApplication.builder()
                .idRequest(12353)
                .email("request2@example.com")
                .creditAmount(new BigDecimal("45000000.00"))
                .creditTime(36)
                .idLoanType((short) 2)
                .build();

        LoanTypeInfo loanTypeInfo = new LoanTypeInfo(
                (short) 2,
                "Vehicular",
                true,
                new BigDecimal("80000000.00"),
                new BigDecimal("5000000.00"),
                12.0
        );

        when(loanTypeRepository.findLoanTypeById(any(Short.class)))
                .thenReturn(Mono.just(loanTypeInfo));
        when(capacityCalculationService.enqueueCapacityCalculation(any(CreditApplication.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(capacityCalculationUseCase.enqueueCapacityCalculation(creditApplication1))
                .verifyComplete();

        StepVerifier.create(capacityCalculationUseCase.enqueueCapacityCalculation(creditApplication2))
                .verifyComplete();

        verify(capacityCalculationService, times(2)).enqueueCapacityCalculation(any(CreditApplication.class));
    }
}
