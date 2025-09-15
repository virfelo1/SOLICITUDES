package co.com.projectve.model.creditapplication.mapper;

import co.com.projectve.model.creditapplication.ActiveLoan;
import co.com.projectve.model.creditapplication.CapacityCalculationMessage;
import co.com.projectve.model.creditapplication.CreditApplication;
import co.com.projectve.model.creditapplication.CapacityCalculationResult;
import co.com.projectve.model.creditapplication.gateways.LoanTypeInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CapacityCalculationMessageMapper {
    
    public CapacityCalculationMessage toMessage(
            CreditApplication creditApplication, 
            LoanTypeInfo loanTypeInfo, 
            BigDecimal baseSalary, 
            CapacityCalculationResult capacityResult,
            List<ActiveLoan> activeLoans) {
        
        return CapacityCalculationMessage.builder()
                .idRequest(creditApplication.getIdRequest())
                .documentType(creditApplication.getDocumentType())
                .documentNumber(creditApplication.getDocumentNumber())
                .creditAmount(creditApplication.getCreditAmount())
                .creditTime(creditApplication.getCreditTime())
                .email(creditApplication.getEmail())
                .idState(creditApplication.getIdState())
                .idLoanType(creditApplication.getIdLoanType())
                .interestRate(loanTypeInfo != null ? loanTypeInfo.interestRate() : null)
                .baseSalary(baseSalary)
                .maxBorrowingCapacity(capacityResult != null ? capacityResult.maxBorrowingCapacity() : BigDecimal.ZERO)
                .activeLoans(activeLoans)
                .build();
    }
}
