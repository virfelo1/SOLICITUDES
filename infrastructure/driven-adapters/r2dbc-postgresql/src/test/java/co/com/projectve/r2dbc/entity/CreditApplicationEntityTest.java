package co.com.projectve.r2dbc.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@DisplayName("Tests para CreditApplicationEntity - Entidad de Base de Datos")
class CreditApplicationEntityTest {

    @Test
    @DisplayName("Debería crear una entidad CreditApplicationEntity con todos los campos")
    void shouldCreateCreditApplicationEntityWithAllFields() {
        // Arrange
        Integer id = 1;
        String documentType = "CC";
        String documentNumber = "123456789";
        BigDecimal creditAmount = new BigDecimal("50000000.00");
        Integer creditTime = 60;
        String typeCredit = "Hipotecario";
        String creditStatus = "Pendiente";

        // Act
        CreditApplicationEntity entity = new CreditApplicationEntity();
        entity.setId(id);
        entity.setDocumentType(documentType);
        entity.setDocumentNumber(documentNumber);
        entity.setCreditAmount(creditAmount);
        entity.setCreditTime(creditTime);
        entity.setTypeCredit(typeCredit);
        entity.setCreditStatus(creditStatus);

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(documentType, entity.getDocumentType());
        assertEquals(documentNumber, entity.getDocumentNumber());
        assertEquals(creditAmount, entity.getCreditAmount());
        assertEquals(creditTime, entity.getCreditTime());
        assertEquals(typeCredit, entity.getTypeCredit());
        assertEquals(creditStatus, entity.getCreditStatus());
    }

    @Test
    @DisplayName("Debería crear una entidad vacía y permitir asignar valores")
    void shouldCreateEmptyEntityAndAllowSettingValues() {
        // Act
        CreditApplicationEntity entity = new CreditApplicationEntity();

        // Assert - Inicialmente todos los campos son null
        assertNull(entity.getId());
        assertNull(entity.getDocumentType());
        assertNull(entity.getDocumentNumber());
        assertNull(entity.getCreditAmount());
        assertNull(entity.getCreditTime());
        assertNull(entity.getTypeCredit());
        assertNull(entity.getCreditStatus());

        // Act - Asignar valores
        entity.setId(999);
        entity.setDocumentType("CE");
        entity.setDocumentNumber("987654321");
        entity.setCreditAmount(new BigDecimal("10000000.00"));
        entity.setCreditTime(24);
        entity.setTypeCredit("Vehiculo");
        entity.setCreditStatus("Aprobado");

        // Assert - Verificar que se asignaron correctamente
        assertEquals(999, entity.getId());
        assertEquals("CE", entity.getDocumentType());
        assertEquals("987654321", entity.getDocumentNumber());
        assertEquals(new BigDecimal("10000000.00"), entity.getCreditAmount());
        assertEquals(24, entity.getCreditTime());
        assertEquals("Vehiculo", entity.getTypeCredit());
        assertEquals("Aprobado", entity.getCreditStatus());
    }

    @Test
    @DisplayName("Debería manejar diferentes tipos de documentos")
    void shouldHandleDifferentDocumentTypes() {
        // Arrange
        CreditApplicationEntity entity = new CreditApplicationEntity();
        String[] documentTypes = {"CC", "CE", "TI", "PP", "NIT"};

        // Act & Assert
        for (String docType : documentTypes) {
            entity.setDocumentType(docType);
            assertEquals(docType, entity.getDocumentType());
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes tipos de crédito")
    void shouldHandleDifferentCreditTypes() {
        // Arrange
        CreditApplicationEntity entity = new CreditApplicationEntity();
        String[] creditTypes = {"Hipotecario", "Vehiculo", "Libre Inversion", "Educacion"};

        // Act & Assert
        for (String creditType : creditTypes) {
            entity.setTypeCredit(creditType);
            assertEquals(creditType, entity.getTypeCredit());
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes montos de crédito")
    void shouldHandleDifferentCreditAmounts() {
        // Arrange
        CreditApplicationEntity entity = new CreditApplicationEntity();
        BigDecimal[] amounts = {
            new BigDecimal("1000000.00"),
            new BigDecimal("50000000.00"),
            new BigDecimal("100000000.00"),
            new BigDecimal("0.01")
        };

        // Act & Assert
        for (BigDecimal amount : amounts) {
            entity.setCreditAmount(amount);
            assertEquals(amount, entity.getCreditAmount());
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes plazos de crédito")
    void shouldHandleDifferentCreditTimes() {
        // Arrange
        CreditApplicationEntity entity = new CreditApplicationEntity();
        Integer[] times = {1, 12, 60, 120, 360};

        // Act & Assert
        for (Integer time : times) {
            entity.setCreditTime(time);
            assertEquals(time, entity.getCreditTime());
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes estados de crédito")
    void shouldHandleDifferentCreditStatuses() {
        // Arrange
        CreditApplicationEntity entity = new CreditApplicationEntity();
        String[] statuses = {"Nuevo", "Pendiente de revision", "En revision", "Aprobado", "Rechazado"};

        // Act & Assert
        for (String status : statuses) {
            entity.setCreditStatus(status);
            assertEquals(status, entity.getCreditStatus());
        }
    }
}

