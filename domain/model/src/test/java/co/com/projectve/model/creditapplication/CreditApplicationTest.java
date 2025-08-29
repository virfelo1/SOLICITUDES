package co.com.projectve.model.creditapplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@DisplayName("Tests para CreditApplication - Model:Dominio")
class CreditApplicationTest {

    @Test
    @DisplayName("Se crea una instancia de CreditApplication con todos los campos")
    void shouldCreateCreditApplicationWithAllFields() {
        // Arrange
        Integer id = 1;
        String documentType = "CC";
        String documentNumber = "123456789";
        BigDecimal creditAmount = new BigDecimal("50000000.00");
        Integer creditTime = 60;
        String typeCredit = "Hipotecario";
        String creditStatus = "Pendiente";

        // Act
        CreditApplication creditApplication = CreditApplication.builder()
                .id(id)
                .documentType(documentType)
                .documentNumber(documentNumber)
                .creditAmount(creditAmount)
                .creditTime(creditTime)
                .typeCredit(typeCredit)
                .creditStatus(creditStatus)
                .build();

        // Assert
        assertNotNull(creditApplication);
        assertEquals(id, creditApplication.getId());
        assertEquals(documentType, creditApplication.getDocumentType());
        assertEquals(documentNumber, creditApplication.getDocumentNumber());
        assertEquals(creditAmount, creditApplication.getCreditAmount());
        assertEquals(creditTime, creditApplication.getCreditTime());
        assertEquals(typeCredit, creditApplication.getTypeCredit());
        assertEquals(creditStatus, creditApplication.getCreditStatus());
    }

    @Test
    @DisplayName("Se crea una instancia de CreditApplication usando constructor por defecto")
    void shouldCreateCreditApplicationWithDefaultConstructor() {
        // Act
        CreditApplication creditApplication = new CreditApplication();

        // Assert
        assertNotNull(creditApplication);
        assertNull(creditApplication.getId());
        assertNull(creditApplication.getDocumentType());
        assertNull(creditApplication.getDocumentNumber());
        assertNull(creditApplication.getCreditAmount());
        assertNull(creditApplication.getCreditTime());
        assertNull(creditApplication.getTypeCredit());
        assertNull(creditApplication.getCreditStatus());
    }

    @Test
    @DisplayName("Debería permitir modificar campos usando setters")
    void shouldAllowModifyingFieldsWithSetters() {
        // Arrange
        CreditApplication creditApplication = new CreditApplication();
        String newDocumentType = "CE";
        String newDocumentNumber = "987654321";

        // Act
        creditApplication.setDocumentType(newDocumentType);
        creditApplication.setDocumentNumber(newDocumentNumber);

        // Assert
        assertEquals(newDocumentType, creditApplication.getDocumentType());
        assertEquals(newDocumentNumber, creditApplication.getDocumentNumber());
    }

    @Test
    @DisplayName("Debería crear una copia usando toBuilder")
    void shouldCreateCopyUsingToBuilder() {
        // Arrange
        CreditApplication original = CreditApplication.builder()
                .id(1)
                .documentType("CC")
                .documentNumber("123456789")
                .creditAmount(new BigDecimal("10000000.00"))
                .creditTime(24)
                .typeCredit("Vehiculo")
                .creditStatus("Aprobado")
                .build();

        // Act
        CreditApplication copy = original.toBuilder()
                .id(2)
                .creditStatus("Rechazado")
                .build();

        // Assert
        assertNotEquals(original.getId(), copy.getId());
        assertEquals(original.getDocumentType(), copy.getDocumentType());
        assertEquals(original.getDocumentNumber(), copy.getDocumentNumber());
        assertEquals(original.getCreditAmount(), copy.getCreditAmount());
        assertEquals(original.getCreditTime(), copy.getCreditTime());
        assertEquals(original.getTypeCredit(), copy.getTypeCredit());
        assertNotEquals(original.getCreditStatus(), copy.getCreditStatus());
    }
}

