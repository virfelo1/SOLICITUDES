# Corrección de Arquitectura: Delegación de Cálculos a Lambda Externa

## 🎯 **Objetivo**

Corregir la implementación para que la **Lambda externa** sea responsable de todos los cálculos de capacidad de endeudamiento, incluyendo:
- Cálculo de cuota mensual del nuevo préstamo
- Cálculo de deuda mensual actual
- Cálculo de capacidad disponible
- Toma de decisión final

## 🔧 **Cambios Realizados**

### **1. Corrección de Error SQL**
**Problema**: Tabla `credit_applications` no existe
**Solución**: Cambiar a `credit_application`

```sql
-- ❌ ANTES
FROM credit_applications ca

-- ✅ DESPUÉS  
FROM credit_application ca
```

### **2. Simplificación del Modelo ActiveLoan**
**Antes**:
```java
public record ActiveLoan(
    Integer loanId,
    String email,
    BigDecimal principalAmount,
    BigDecimal interestRate,
    Integer remainingMonths,
    BigDecimal monthlyPayment
) {}
```

**Después**:
```java
public record ActiveLoan(
    Integer idRequest,
    BigDecimal creditAmount,
    Integer creditTime,
    Double interestRate,
    BigDecimal monthlyPayment  // null - será calculado por Lambda
) {}
```

### **3. Actualización de CapacityCalculationMessage**
**Antes**: Incluía cálculos pre-computados
```java
private BigDecimal currentMonthlyDebt;
private BigDecimal availableCapacity;
```

**Después**: Solo datos básicos + préstamos activos
```java
private BigDecimal maxBorrowingCapacity;
private List<ActiveLoan> activeLoans;  // Sin cuotas calculadas
```

### **4. Simplificación de CapacityCalculationAdapter**
**Antes**: Calculaba deuda mensual actual
```java
BigDecimal currentMonthlyDebt = activeLoans.stream()
    .map(ActiveLoan::monthlyPayment)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

**Después**: Solo capacidad máxima básica
```java
BigDecimal maxBorrowingCapacity = baseSalary.multiply(new BigDecimal("0.35"));
// La Lambda externa hará todos los cálculos complejos
```

### **5. Actualización de ActiveLoanRepositoryAdapter**
**Antes**: Calculaba cuota mensual en SQL
```sql
CASE 
    WHEN lt.interest_rate > 0 AND ca.credit_time > 0 THEN
        (ca.credit_amount * (lt.interest_rate / 100.0 / 12.0)) / 
        (1 - POWER(1 + (lt.interest_rate / 100.0 / 12.0), -ca.credit_time))
    ELSE ca.credit_amount / ca.credit_time
END as monthly_payment
```

**Después**: Solo datos básicos
```sql
SELECT 
    ca.id_request,
    ca.credit_amount,
    ca.credit_time,
    lt.interest_rate
FROM credit_application ca
-- Sin cálculo de cuota mensual
```

### **6. Actualización de CapacityCalculationMessageMapper**
**Antes**: Usaba cálculos pre-computados
```java
.currentMonthlyDebt(capacityResult.currentMonthlyDebt())
.availableCapacity(capacityResult.availableCapacity())
```

**Después**: Incluye préstamos activos
```java
.maxBorrowingCapacity(capacityResult.maxBorrowingCapacity())
.activeLoans(activeLoans)  // Lista de préstamos sin cuotas
```

## 📋 **Estructura del Mensaje SQS**

### **Datos Enviados a la Lambda Externa**:
```json
{
  "idRequest": 37,
  "documentType": "CC",
  "documentNumber": "123456789",
  "creditAmount": 1000000,
  "creditTime": 24,
  "email": "librecarbon@gmail.com",
  "idState": 1,
  "idLoanType": 1,
  "interestRate": 12.5,
  "baseSalary": 3000000,
  "maxBorrowingCapacity": 1050000,
  "activeLoans": [
    {
      "idRequest": 25,
      "creditAmount": 500000,
      "creditTime": 12,
      "interestRate": 10.0,
      "monthlyPayment": null
    }
  ]
}
```

## 🎯 **Responsabilidades de la Lambda Externa**

### **Cálculos que debe realizar**:
1. **Cuota mensual del nuevo préstamo**:
   ```java
   monthlyPayment = (principal * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -periods))
   ```

2. **Deuda mensual actual**:
   ```java
   currentDebt = sum(activeLoans.map(loan -> calculateMonthlyPayment(loan)))
   ```

3. **Capacidad disponible**:
   ```java
   availableCapacity = maxBorrowingCapacity - currentDebt
   ```

4. **Decisión final**:
   ```java
   if (newLoanPayment <= availableCapacity) {
       if (creditAmount > baseSalary * 5) {
           decision = "REVISION_MANUAL"
       } else {
           decision = "APROBADO"
       }
   } else {
       decision = "RECHAZADO"
   }
   ```

## ✅ **Beneficios de la Corrección**

1. **Separación de responsabilidades**: Microservicio solo prepara datos, Lambda hace cálculos
2. **Escalabilidad**: Lambda puede manejar cálculos complejos sin afectar el microservicio
3. **Mantenibilidad**: Lógica de negocio centralizada en la Lambda
4. **Flexibilidad**: Fácil modificación de fórmulas sin tocar el microservicio
5. **Performance**: Microservicio más rápido al no hacer cálculos pesados

## 🧪 **Próximos Pasos**

1. **Probar la aplicación** - Verificar que se ejecute sin errores
2. **Probar validación automática** - Crear solicitudes con `auto_validation = true`
3. **Verificar mensaje SQS** - Confirmar que se envían los datos correctos
4. **Implementar Lambda externa** - Crear la Lambda que reciba y procese el mensaje

La corrección está **completa y lista para pruebas** 🎉
