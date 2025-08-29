package co.com.projectve.r2dbc;

import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Tests para MyReactiveRepositoryAdapter - Adaptador de Repositorio Reactivo")
class MyReactiveRepositoryAdapterTest {

    @Mock
    private MyReactiveRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    private MyReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar el ObjectMapper mock para el mapeo
        when(objectMapper.map(any(CreditApplicationEntity.class), eq(CreditApplication.class)))
                .thenAnswer(invocation -> {
                    CreditApplicationEntity entity = invocation.getArgument(0);
                    return CreditApplication.builder()
                            .id(entity.getId())
                            .documentType(entity.getDocumentType())
                            .documentNumber(entity.getDocumentNumber())
                            .creditAmount(entity.getCreditAmount())
                            .creditTime(entity.getCreditTime())
                            .typeCredit(entity.getTypeCredit())
                            .creditStatus(entity.getCreditStatus())
                            .build();
                });

        adapter = new MyReactiveRepositoryAdapter(repository, objectMapper, transactionalOperator);
    }

    @Test
    @DisplayName("Debería guardar una solicitud de crédito exitosamente")
    void shouldSaveCreditApplicationSuccessfully() {
        // Arrange
        CreditApplication creditApplication = CreditApplication.builder()
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .typeCredit("Hipotecario")
                .creditStatus("Pendiente de revision")
                .build();

        CreditApplicationEntity entity = new CreditApplicationEntity();
        entity.setDocumentType("CC");
        entity.setDocumentNumber("123456789");
        entity.setCreditAmount(new BigDecimal("50000000.00"));
        entity.setCreditTime(60);
        entity.setTypeCredit("Hipotecario");
        entity.setCreditStatus("Pendiente de revision");

        when(repository.save(any(CreditApplicationEntity.class)))
                .thenReturn(Mono.just(entity));

        // Act
        Mono<CreditApplication> result = adapter.saveRequest(creditApplication);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedApplication -> 
                    savedApplication.getDocumentType().equals("CC") &&
                    savedApplication.getDocumentNumber().equals("123456789") &&
                    savedApplication.getCreditAmount().equals(new BigDecimal("50000000.00")) &&
                    savedApplication.getCreditTime().equals(60) &&
                    savedApplication.getTypeCredit().equals("Hipotecario") &&
                    savedApplication.getCreditStatus().equals("Pendiente de revision"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar diferentes tipos de documento en el guardado")
    void shouldHandleDifferentDocumentTypesInSave() {
        // Arrange
        String[] documentTypes = {"CC", "CE", "TI", "PP", "NIT"};

        for (String docType : documentTypes) {
            CreditApplication creditApplication = CreditApplication.builder()
                    .documentType(docType)
                    .documentNumber("123456789")
                    .creditAmount(new BigDecimal("10000000.00"))
                    .creditTime(24)
                    .typeCredit("Vehiculo")
                    .creditStatus("Pendiente de revision")
                    .build();

            CreditApplicationEntity entity = new CreditApplicationEntity();
            entity.setDocumentType(docType);
            entity.setDocumentNumber("123456789");
            entity.setCreditAmount(new BigDecimal("10000000.00"));
            entity.setCreditTime(24);
            entity.setTypeCredit("Vehiculo");
            entity.setCreditStatus("Pendiente de revision");

            when(repository.save(any(CreditApplicationEntity.class)))
                    .thenReturn(Mono.just(entity));

            // Act
            Mono<CreditApplication> result = adapter.saveRequest(creditApplication);

            // Assert
            StepVerifier.create(result)
                    .expectNextMatches(savedApplication -> 
                        savedApplication.getDocumentType().equals(docType))
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes tipos de crédito en el guardado")
    void shouldHandleDifferentCreditTypesInSave() {
        // Arrange
        String[] creditTypes = {"Hipotecario", "Vehiculo", "Libre Inversion", "Educacion"};

        for (String creditType : creditTypes) {
            CreditApplication creditApplication = CreditApplication.builder()
                    .documentType("CC")
                    .documentNumber("123456789")
                    .creditAmount(new BigDecimal("20000000.00"))
                    .creditTime(36)
                    .typeCredit(creditType)
                    .creditStatus("Pendiente de revision")
                    .build();

            CreditApplicationEntity entity = new CreditApplicationEntity();
            entity.setDocumentType("CC");
            entity.setDocumentNumber("123456789");
            entity.setCreditAmount(new BigDecimal("20000000.00"));
            entity.setCreditTime(36);
            entity.setTypeCredit(creditType);
            entity.setCreditStatus("Pendiente de revision");

            when(repository.save(any(CreditApplicationEntity.class)))
                    .thenReturn(Mono.just(entity));

            // Act
            Mono<CreditApplication> result = adapter.saveRequest(creditApplication);

            // Assert
            StepVerifier.create(result)
                    .expectNextMatches(savedApplication -> 
                        savedApplication.getTypeCredit().equals(creditType))
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes montos de crédito en el guardado")
    void shouldHandleDifferentCreditAmountsInSave() {
        // Arrange
        BigDecimal[] amounts = {
            new BigDecimal("1000000.00"),
            new BigDecimal("50000000.00"),
            new BigDecimal("100000000.00")
        };

        for (BigDecimal amount : amounts) {
            CreditApplication creditApplication = CreditApplication.builder()
                    .documentType("CC")
                    .documentNumber("123456789")
                    .creditAmount(amount)
                    .creditTime(60)
                    .typeCredit("Hipotecario")
                    .creditStatus("Pendiente de revision")
                    .build();

            CreditApplicationEntity entity = new CreditApplicationEntity();
            entity.setDocumentType("CC");
            entity.setDocumentNumber("123456789");
            entity.setCreditAmount(amount);
            entity.setCreditTime(60);
            entity.setTypeCredit("Hipotecario");
            entity.setCreditStatus("Pendiente de revision");

            when(repository.save(any(CreditApplicationEntity.class)))
                    .thenReturn(Mono.just(entity));

            // Act
            Mono<CreditApplication> result = adapter.saveRequest(creditApplication);

            // Assert
            StepVerifier.create(result)
                    .expectNextMatches(savedApplication -> 
                        savedApplication.getCreditAmount().equals(amount))
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes plazos de crédito en el guardado")
    void shouldHandleDifferentCreditTimesInSave() {
        // Arrange
        Integer[] times = {1, 12, 60, 120, 360};

        for (Integer time : times) {
            CreditApplication creditApplication = CreditApplication.builder()
                    .documentType("CC")
                    .documentNumber("123456789")
                    .creditAmount(new BigDecimal("30000000.00"))
                    .creditTime(time)
                    .typeCredit("Libre Inversion")
                    .creditStatus("Pendiente de revision")
                    .build();

            CreditApplicationEntity entity = new CreditApplicationEntity();
            entity.setDocumentType("CC");
            entity.setDocumentNumber("123456789");
            entity.setCreditAmount(new BigDecimal("30000000.00"));
            entity.setCreditTime(time);
            entity.setTypeCredit("Libre Inversion");
            entity.setCreditStatus("Pendiente de revision");

            when(repository.save(any(CreditApplicationEntity.class)))
                    .thenReturn(Mono.just(entity));

            // Act
            Mono<CreditApplication> result = adapter.saveRequest(creditApplication);

            // Assert
            StepVerifier.create(result)
                    .expectNextMatches(savedApplication -> 
                        savedApplication.getCreditTime().equals(time))
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Debería mantener el estado del crédito en el guardado")
    void shouldMaintainCreditStatusInSave() {
        // Arrange
        CreditApplication creditApplication = CreditApplication.builder()
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("50000000.00"))
                .creditTime(60)
                .typeCredit("Hipotecario")
                .creditStatus("Pendiente de revision")
                .build();

        CreditApplicationEntity entity = new CreditApplicationEntity();
        entity.setDocumentType("CC");
        entity.setDocumentNumber("123456789");
        entity.setCreditAmount(new BigDecimal("50000000.00"));
        entity.setCreditTime(60);
        entity.setTypeCredit("Hipotecario");
        entity.setCreditStatus("Pendiente de revision");

        when(repository.save(any(CreditApplicationEntity.class)))
                .thenReturn(Mono.just(entity));

        // Act
        Mono<CreditApplication> result = adapter.saveRequest(creditApplication);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedApplication -> 
                    savedApplication.getCreditStatus().equals("Pendiente de revision"))
                .verifyComplete();
    }
}
