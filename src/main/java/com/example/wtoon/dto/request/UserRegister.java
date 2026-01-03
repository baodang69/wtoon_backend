package com.example.wtoon.dto.request;

import lombok.Data;

@Data
public class UserRegister {
    private String username;
    private String password;
    private String repeatPassword;
}
