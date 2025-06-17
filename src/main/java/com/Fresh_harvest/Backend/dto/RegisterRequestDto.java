package com.Fresh_harvest.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {

    @NotBlank(message = "username cannot be empty")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6)
    private String password;

    @NotBlank(message = "Email Cannot be empty")
    @Email(message = "Invalid Email format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Role cannot be empty")
    private String role;
}
