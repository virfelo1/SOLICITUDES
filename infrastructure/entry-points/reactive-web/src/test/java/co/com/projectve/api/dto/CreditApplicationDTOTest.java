package co.com.projectve.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Set;

@DisplayName("Tests para CreditApplicationDTO - DTO de API")
class CreditApplicationDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debería crear un DTO válido con todos los campos correctos")
    void shouldCreateValidDTOWithAllFields() {
        // Arrange & Act
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                new BigDecimal("50000000.00"),
                60,
                "Hipotecario"
        );

        // Assert
        assertEquals("CC", dto.documentType());
        assertEquals("123456789", dto.documentNumber());
        assertEquals(new BigDecimal("50000000.00"), dto.creditAmount());
        assertEquals(60, dto.creditTime());
        assertEquals("Hipotecario", dto.typeCredit());
    }

    @Test
    @DisplayName("Debería validar correctamente un DTO con datos válidos")
    void shouldValidateCorrectlyWithValidData() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CE",
                "987654321",
                new BigDecimal("10000000.00"),
                24,
                "Vehiculo"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty(), "No debería haber violaciones de validación");
    }

    @Test
    @DisplayName("Debería rechazar tipo de documento inválido")
    void shouldRejectInvalidDocumentType() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "INVALID",
                "123456789",
                new BigDecimal("50000000.00"),
                60,
                "Hipotecario"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("documentType")));
    }

    @Test
    @DisplayName("Debería rechazar tipo de crédito inválido")
    void shouldRejectInvalidCreditType() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                new BigDecimal("50000000.00"),
                60,
                "CreditoPersonal"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("typeCredit")));
    }

    @Test
    @DisplayName("Debería rechazar plazo de crédito menor al mínimo")
    void shouldRejectCreditTimeBelowMinimum() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                new BigDecimal("50000000.00"),
                0,
                "Hipotecario"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("creditTime")));
    }

    @Test
    @DisplayName("Debería rechazar plazo de crédito mayor al máximo")
    void shouldRejectCreditTimeAboveMaximum() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                new BigDecimal("50000000.00"),
                361,
                "Hipotecario"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("creditTime")));
    }

    @Test
    @DisplayName("Debería rechazar documento tipo vacío")
    void shouldRejectEmptyDocumentType() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "",
                "123456789",
                new BigDecimal("50000000.00"),
                60,
                "Hipotecario"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("documentType")));
    }

    @Test
    @DisplayName("Debería rechazar número de documento vacío")
    void shouldRejectEmptyDocumentNumber() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "",
                new BigDecimal("50000000.00"),
                60,
                "Hipotecario"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("documentNumber")));
    }

    @Test
    @DisplayName("Debería rechazar monto de crédito nulo")
    void shouldRejectNullCreditAmount() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                null,
                60,
                "Hipotecario"
        );

        // Act
        Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("creditAmount")));
    }

    @Test
    @DisplayName("Debería aceptar todos los tipos de documento válidos")
    void shouldAcceptAllValidDocumentTypes() {
        // Arrange
        String[] validDocumentTypes = {"CC", "CE", "TI", "PP", "NIT"};

        for (String docType : validDocumentTypes) {
            // Act
            CreditApplicationDTO dto = new CreditApplicationDTO(
                    docType,
                    "123456789",
                    new BigDecimal("50000000.00"),
                    60,
                    "Hipotecario"
            );

            Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty(), 
                    "El tipo de documento " + docType + " debería ser válido");
        }
    }

    @Test
    @DisplayName("Debería aceptar todos los tipos de crédito válidos")
    void shouldAcceptAllValidCreditTypes() {
        // Arrange
        String[] validCreditTypes = {"Hipotecario", "Vehiculo", "Libre Inversion", "Educacion"};

        for (String creditType : validCreditTypes) {
            // Act
            CreditApplicationDTO dto = new CreditApplicationDTO(
                    "CC",
                    "123456789",
                    new BigDecimal("50000000.00"),
                    60,
                    creditType
            );

            Set<ConstraintViolation<CreditApplicationDTO>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty(), 
                    "El tipo de crédito " + creditType + " debería ser válido");
        }
    }
}

