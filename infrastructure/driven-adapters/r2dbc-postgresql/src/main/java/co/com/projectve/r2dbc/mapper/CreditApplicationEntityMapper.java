package co.com.projectve.r2dbc.mapper;

import co.com.projectve.r2dbc.dto.*;
import co.com.projectve.r2dbc.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CreditApplicationEntityMapper {

    CreditApplicationEntityMapper INSTANCE = Mappers.getMapper(CreditApplicationEntityMapper.class);

    // ---- States ----
    @Mapping(source = "idState", target = "idState")
    @Mapping(source = "nameState", target = "nameState")
    @Mapping(source = "description", target = "description")
    StateDTO toStateDTO(StatesEntity entity);

    // ---- Loan Types ----
    @Mapping(source = "idLoanType", target = "idLoanType")
    @Mapping(source = "nameLoanType", target = "nameLoanType")
    @Mapping(source = "minAmount", target = "minAmount")
    @Mapping(source = "maxAmount", target = "maxAmount")
    @Mapping(source = "interestRate", target = "interestRate")
    @Mapping(source = "autoValidation", target = "autoValidation")
    LoanTypeDTO toLoanTypeDTO(LoanTypeEntity entity);

    // ---- Credit Application ----
    @Mapping(source = "entity.idRequest", target = "idRequest")
    @Mapping(source = "entity.documentType", target = "documentType")
    @Mapping(source = "entity.documentNumber", target = "documentNumber")
    @Mapping(source = "entity.creditAmount", target = "creditAmount")
    @Mapping(source = "entity.creditTime", target = "creditTime")
    @Mapping(source = "entity.email", target = "email")
    @Mapping(target = "state", expression = "java(toStateDTO(state))")
    @Mapping(target = "loanType", expression = "java(toLoanTypeDTO(loanType))")
    CreditApplicationResponseDTO toResponseDTO(
            CreditApplicationEntity entity,
            StatesEntity state,
            LoanTypeEntity loanType
    );
}
