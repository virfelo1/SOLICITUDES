package co.com.projectve.api.mapper;

import co.com.projectve.api.dto.CreditApplicationDTO;
import co.com.projectve.model.creditapplication.CreditApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditApplicationDTOMapper {
    @Mapping(target = "idRequest", ignore = true)
    @Mapping(target = "idState", ignore = true)
    CreditApplication toModel (CreditApplicationDTO creditApplicationDTO);
}
