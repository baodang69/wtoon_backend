package com.example.wtoon.mapper;

import com.example.wtoon.dto.request.UserRegister;
import com.example.wtoon.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "email", ignore = true)
    User toEntity(UserRegister dto);
}
