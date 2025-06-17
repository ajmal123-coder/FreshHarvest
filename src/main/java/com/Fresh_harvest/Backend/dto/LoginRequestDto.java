package com.Fresh_harvest.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {

    @NotBlank(message = "Please provide Username or Email")
    private String usernameOrEmail;

    @NotBlank(message = "Please provide password")
    private String password;
}
