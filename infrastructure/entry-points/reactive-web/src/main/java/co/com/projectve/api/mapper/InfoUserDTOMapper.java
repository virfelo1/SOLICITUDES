package co.com.projectve.api.mapper;

import co.com.projectve.api.dto.SaveRequestDTO;
import co.com.projectve.model.infouser.InfoUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InfoUserDTOMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creditStatus", ignore = true)
    InfoUser toModel (SaveRequestDTO saveRequestDTO);
}
