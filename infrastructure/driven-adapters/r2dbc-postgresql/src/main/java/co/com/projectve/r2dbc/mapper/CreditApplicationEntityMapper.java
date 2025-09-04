package co.com.projectve.r2dbc.mapper;

import co.com.projectve.r2dbc.dto.CreditApplicationListViewDTO;
import co.com.projectve.r2dbc.entity.CreditApplicationEntity;
import co.com.projectve.r2dbc.entity.LoanTypeEntity;
import co.com.projectve.r2dbc.entity.StatesEntity;

public interface CreditApplicationEntityMapper {

    default CreditApplicationListViewDTO toListView(
            CreditApplicationEntity entity,
            StatesEntity state,
            LoanTypeEntity loanType
    ) {
        CreditApplicationListViewDTO dto = new CreditApplicationListViewDTO();
        dto.setIdRequest(entity.getIdRequest());
        dto.setDocumentType(entity.getDocumentType());
        dto.setDocumentNumber(entity.getDocumentNumber());
        dto.setCreditAmount(entity.getCreditAmount());
        dto.setCreditTime(entity.getCreditTime());
        dto.setEmail(entity.getEmail());
        dto.setNameState(state != null ? state.getNameState() : null);
        dto.setNameLoan(loanType != null ? loanType.getNameLoanType() : null);
        return dto;
    }
}
