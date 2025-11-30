package com.example.usermanagement.application.mapper;

import com.example.usermanagement.application.dto.user.UserRequestDTO;
import com.example.usermanagement.application.dto.user.UserResponseDTO;
import com.example.usermanagement.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct Mapper for User entity <-> DTO conversions.
 *
 * Part of the APPLICATION LAYER - handles data transformation.
 *
 * MapStruct generates the implementation at compile time.
 * componentModel = "spring" makes it a Spring bean.
 *
 * Mapping Methods:
 * - toEntity: DTO → Entity (for create operations)
 * - toResponseDTO: Entity → DTO (for responses)
 * - toResponseDTOList: List<Entity> → List<DTO>
 * - updateEntityFromDTO: Updates existing entity from DTO
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convert UserRequestDTO to User entity.
     * Used when creating a new user.
     */
    User toEntity(UserRequestDTO dto);

    /**
     * Convert User entity to UserResponseDTO.
     * Used when returning user data to client.
     */
    UserResponseDTO toResponseDTO(User entity);

    /**
     * Convert list of User entities to list of UserResponseDTOs.
     */
    List<UserResponseDTO> toResponseDTOList(List<User> entities);

    /**
     * Update existing User entity with data from DTO.
     * Used for update operations.
     *
     * @param dto The source DTO with new values
     * @param entity The target entity to update
     */
    void updateEntityFromDTO(UserRequestDTO dto, @MappingTarget User entity);
}
