package com.Fresh_harvest.Backend.controller;


import com.Fresh_harvest.Backend.dto.JwtAuthResponse;
import com.Fresh_harvest.Backend.dto.LoginRequestDto;
import com.Fresh_harvest.Backend.dto.RegisterRequestDto;
import com.Fresh_harvest.Backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User Registration and login APIs")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new User")
    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> registerUser(@Valid @RequestBody RegisterRequestDto registrationDTO){
        JwtAuthResponse response = authService.registerUser(registrationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Login a User")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> loginUser(@Valid @RequestBody LoginRequestDto loginDTO){
        JwtAuthResponse response = authService.loginUser(loginDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

