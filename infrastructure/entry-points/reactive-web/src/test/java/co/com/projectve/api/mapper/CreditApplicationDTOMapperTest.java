package co.com.projectve.api.mapper;

import co.com.projectve.api.dto.CreditApplicationDTO;
import co.com.projectve.model.creditapplication.CreditApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@DisplayName("Tests para CreditApplicationDTOMapper - Mapeador DTO a Modelo")
class CreditApplicationDTOMapperTest {

    private final CreditApplicationDTOMapper mapper = Mappers.getMapper(CreditApplicationDTOMapper.class);

    @Test
    @DisplayName("Debería mapear correctamente un DTO a un modelo de dominio")
    void shouldMapDTOToDomainModelCorrectly() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                new BigDecimal("50000000.00"),
                60,
                "Hipotecario"
        );

        // Act
        CreditApplication result = mapper.toModel(dto);

        // Assert
        assertNotNull(result);
        assertNull(result.getId(), "El ID debería ser null ya que se ignora en el mapeo");
        assertEquals("CC", result.getDocumentType());
        assertEquals("123456789", result.getDocumentNumber());
        assertEquals(new BigDecimal("50000000.00"), result.getCreditAmount());
        assertEquals(60, result.getCreditTime());
        assertEquals("Hipotecario", result.getTypeCredit());
        assertNull(result.getCreditStatus(), "El estado del crédito debería ser null ya que se ignora en el mapeo");
    }

    @Test
    @DisplayName("Debería mapear correctamente diferentes tipos de documento")
    void shouldMapDifferentDocumentTypesCorrectly() {
        // Arrange
        String[] documentTypes = {"CC", "CE", "TI", "PP", "NIT"};

        for (String docType : documentTypes) {
            CreditApplicationDTO dto = new CreditApplicationDTO(
                    docType,
                    "123456789",
                    new BigDecimal("10000000.00"),
                    24,
                    "Vehiculo"
            );

            // Act
            CreditApplication result = mapper.toModel(dto);

            // Assert
            assertEquals(docType, result.getDocumentType(), 
                    "El tipo de documento " + docType + " debería mapearse correctamente");
        }
    }

    @Test
    @DisplayName("Debería mapear correctamente diferentes tipos de crédito")
    void shouldMapDifferentCreditTypesCorrectly() {
        // Arrange
        String[] creditTypes = {"Hipotecario", "Vehiculo", "Libre Inversion", "Educacion"};

        for (String creditType : creditTypes) {
            CreditApplicationDTO dto = new CreditApplicationDTO(
                    "CC",
                    "123456789",
                    new BigDecimal("20000000.00"),
                    36,
                    creditType
            );

            // Act
            CreditApplication result = mapper.toModel(dto);

            // Assert
            assertEquals(creditType, result.getTypeCredit(), 
                    "El tipo de crédito " + creditType + " debería mapearse correctamente");
        }
    }

    @Test
    @DisplayName("Debería mapear correctamente diferentes montos de crédito")
    void shouldMapDifferentCreditAmountsCorrectly() {
        // Arrange
        BigDecimal[] amounts = {
            new BigDecimal("1000000.00"),
            new BigDecimal("50000000.00"),
            new BigDecimal("100000000.00"),
            new BigDecimal("0.01")
        };

        for (BigDecimal amount : amounts) {
            CreditApplicationDTO dto = new CreditApplicationDTO(
                    "CC",
                    "123456789",
                    amount,
                    60,
                    "Hipotecario"
            );

            // Act
            CreditApplication result = mapper.toModel(dto);

            // Assert
            assertEquals(amount, result.getCreditAmount(), 
                    "El monto " + amount + " debería mapearse correctamente");
        }
    }

    @Test
    @DisplayName("Debería mapear correctamente diferentes plazos de crédito")
    void shouldMapDifferentCreditTimesCorrectly() {
        // Arrange
        Integer[] times = {1, 12, 60, 120, 360};

        for (Integer time : times) {
            CreditApplicationDTO dto = new CreditApplicationDTO(
                    "CC",
                    "123456789",
                    new BigDecimal("30000000.00"),
                    time,
                    "Libre Inversion"
            );

            // Act
            CreditApplication result = mapper.toModel(dto);

            // Assert
            assertEquals(time, result.getCreditTime(), 
                    "El plazo " + time + " debería mapearse correctamente");
        }
    }

    @Test
    @DisplayName("Debería mapear correctamente diferentes números de documento")
    void shouldMapDifferentDocumentNumbersCorrectly() {
        // Arrange
        String[] documentNumbers = {"123456789", "987654321", "111111111", "999999999"};

        for (String docNumber : documentNumbers) {
            CreditApplicationDTO dto = new CreditApplicationDTO(
                    "CE",
                    docNumber,
                    new BigDecimal("15000000.00"),
                    48,
                    "Educacion"
            );

            // Act
            CreditApplication result = mapper.toModel(dto);

            // Assert
            assertEquals(docNumber, result.getDocumentNumber(), 
                    "El número de documento " + docNumber + " debería mapearse correctamente");
        }
    }

    @Test
    @DisplayName("Debería ignorar el campo ID en el mapeo")
    void shouldIgnoreIdFieldInMapping() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                new BigDecimal("50000000.00"),
                60,
                "Hipotecario"
        );

        // Act
        CreditApplication result = mapper.toModel(dto);

        // Assert
        assertNull(result.getId(), "El ID debería ser null ya que se ignora en el mapeo");
    }

    @Test
    @DisplayName("Debería ignorar el campo creditStatus en el mapeo")
    void shouldIgnoreCreditStatusFieldInMapping() {
        // Arrange
        CreditApplicationDTO dto = new CreditApplicationDTO(
                "CC",
                "123456789",
                new BigDecimal("50000000.00"),
                60,
                "Hipotecario"
        );

        // Act
        CreditApplication result = mapper.toModel(dto);

        // Assert
        assertNull(result.getCreditStatus(), "El estado del crédito debería ser null ya que se ignora en el mapeo");
    }
}

