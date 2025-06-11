package com.Fresh_harvest.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {

    private String accessToken;
    private String tokenType="Bearer";
    private Long userId;
    private String username;
    private String email;
    private String role;
}
