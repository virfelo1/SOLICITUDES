package co.com.projectve.api.mapper;

import co.com.projectve.api.dto.CapacityTestResponseDTO;
import co.com.projectve.model.creditapplication.ActiveLoan;
import co.com.projectve.model.creditapplication.CapacityCalculationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CapacityTestMapper {
    
    public CapacityTestResponseDTO toResponseDTO(
            String email, 
            java.math.BigDecimal baseSalary, 
            CapacityCalculationResult result, 
            List<ActiveLoan> activeLoans) {
        
        List<CapacityTestResponseDTO.ActiveLoanDTO> activeLoanDTOs = activeLoans.stream()
                .map(this::toActiveLoanDTO)
                .toList();
        
        return CapacityTestResponseDTO.builder()
                .email(email)
                .baseSalary(baseSalary)
                .maxBorrowingCapacity(result.maxBorrowingCapacity())
                .currentMonthlyDebt(result.currentMonthlyDebt())
                .availableCapacity(result.availableCapacity())
                .activeLoans(activeLoanDTOs)
                .decision(result.decision())
                .reason(result.reason())
                .build();
    }
    
    private CapacityTestResponseDTO.ActiveLoanDTO toActiveLoanDTO(ActiveLoan activeLoan) {
        return CapacityTestResponseDTO.ActiveLoanDTO.builder()
                .loanId(activeLoan.idRequest())
                .principalAmount(activeLoan.creditAmount())
                .interestRate(activeLoan.interestRate() != null ? java.math.BigDecimal.valueOf(activeLoan.interestRate()) : null)
                .remainingMonths(activeLoan.creditTime())
                .monthlyPayment(activeLoan.monthlyRequestAmount())
                .build();
    }
}
