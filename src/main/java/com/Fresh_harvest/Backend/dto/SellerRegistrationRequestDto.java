package com.Fresh_harvest.Backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SellerRegistrationRequestDto {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;


    @NotBlank(message = "Seller name cannot be empty")
    @Size(min = 3, max = 100, message = "Seller name must be between 3 and 100 characters")
    private String sellerName;

    @NotBlank(message = "Address cannot be empty")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;


    @NotBlank(message = "Bank account details cannot be empty")
    @Size(max = 255, message = "Bank account details cannot exceed 255 characters")
    private String bankAccountDetails;
}
