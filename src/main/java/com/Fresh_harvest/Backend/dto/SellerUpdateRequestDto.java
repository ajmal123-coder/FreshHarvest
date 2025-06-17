package com.Fresh_harvest.Backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerUpdateRequestDto {

    @Size(min = 3, max = 100, message = "Seller name must be between 3 and 100 characters if provided")
    private String sellerName; // Can be updated by admin or seller

    @Size(max = 255, message = "Address cannot exceed 255 characters if provided")
    private String address;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format if provided")
    private String phoneNumber;

    @Size(max = 255, message = "Bank account details cannot exceed 255 characters if provided")
    private String bankAccountDetails;

}