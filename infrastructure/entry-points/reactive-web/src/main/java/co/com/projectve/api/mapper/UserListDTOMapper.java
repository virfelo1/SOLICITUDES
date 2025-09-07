package co.com.projectve.api.mapper;

import co.com.projectve.api.dto.AuthUserResponseDTO;
import co.com.projectve.api.dto.UserListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserListDTOMapper {

    /**
     * Mapea de AuthUserResponseDTO a UserListDTO.
     * Mapstruct se encarga de los campos con el mismo nombre.
     * No es necesario ignorar campos ya que no existen en el DTO de destino.
     */
    UserListDTO toUserListDTO(AuthUserResponseDTO authUserResponseDTO);
}