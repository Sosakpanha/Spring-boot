package com.example.usermanagement.mapper;

import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct Mapper for User Entity <-> DTO conversions.
 *
 * @Mapper: Marks this interface as a MapStruct mapper
 * - componentModel = "spring": Makes mapper a Spring bean for injection
 *
 * MapStruct generates implementation at compile time:
 * - Type-safe (compile-time errors if types don't match)
 * - Fast (no reflection, just plain method calls)
 * - Generated code in target/generated-sources/annotations
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convert UserRequestDTO to User entity.
     * Used when creating or updating a user.
     *
     * @param dto the request DTO
     * @return User entity
     */
    User toEntity(UserRequestDTO dto);

    /**
     * Convert User entity to UserResponseDTO.
     * Used when returning user data in API responses.
     *
     * @param entity the User entity
     * @return UserResponseDTO
     */
    UserResponseDTO toResponseDTO(User entity);

    /**
     * Convert list of User entities to list of UserResponseDTOs.
     *
     * @param entities list of User entities
     * @return list of UserResponseDTOs
     */
    List<UserResponseDTO> toResponseDTOList(List<User> entities);

    /**
     * Update existing User entity from DTO.
     * Used for partial updates without creating new entity.
     *
     * @param dto the source DTO with updated values
     * @param entity the target entity to update
     */
    void updateEntityFromDTO(UserRequestDTO dto, @MappingTarget User entity);
}
