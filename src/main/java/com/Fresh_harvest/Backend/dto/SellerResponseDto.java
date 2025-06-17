package com.Fresh_harvest.Backend.dto;

import com.Fresh_harvest.Backend.model.SellerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDto {

    private Long id;
    private String sellerName;
    private String address;
    private String phoneNumber;
    private String bankAccountDetails;
    private SellerStatus status;
    private boolean isBlocked;

    private Long userId;
    private String username;
    private String email;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}