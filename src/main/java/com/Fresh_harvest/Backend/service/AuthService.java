package com.Fresh_harvest.Backend.service;

import com.Fresh_harvest.Backend.config.CustomUserDetails;
import com.Fresh_harvest.Backend.config.JwtUtil;
import com.Fresh_harvest.Backend.dto.JwtAuthResponse;
import com.Fresh_harvest.Backend.dto.LoginRequestDto;
import com.Fresh_harvest.Backend.dto.RegisterRequestDto;
import com.Fresh_harvest.Backend.exception.InvalidCredentialsException;
import com.Fresh_harvest.Backend.exception.UserAlreadyExistsException;
import com.Fresh_harvest.Backend.model.ERole;
import com.Fresh_harvest.Backend.model.Role;
import com.Fresh_harvest.Backend.model.User;
import com.Fresh_harvest.Backend.repository.RoleRepository;
import com.Fresh_harvest.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public JwtAuthResponse registerUser(RegisterRequestDto registrationDTO){
        if (userRepository.existsByUsername(registrationDTO.getUsername())){
            throw new UserAlreadyExistsException("Username "+registrationDTO.getUsername()+" is already taken");
        }
        if (userRepository.existsByEmail(registrationDTO.getEmail())){
            throw new UserAlreadyExistsException("Email "+registrationDTO.getEmail()+" is already registered");
        }

        Role defaultRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                .orElseGet(() -> {
                    Role newRole = new Role(ERole.ROLE_CUSTOMER);
                    return roleRepository.save(newRole);
                });

        User user = User.builder()
                .username(registrationDTO.getUsername())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .email(registrationDTO.getEmail())
                .roles(Collections.singleton(defaultRole))
                .build();

        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        Authentication authentication =new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtil.generateToken(authentication);

        return buildJwtAuthResponse(savedUser, jwt);
    }


    public JwtAuthResponse loginUser(LoginRequestDto loginDTO){

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsernameOrEmail(),
                            loginDTO.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtil.generateToken(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseGet(()-> userRepository.findByEmail(loginDTO.getUsernameOrEmail())
                            .orElseThrow(()->new InvalidCredentialsException("User not Found")));

            return buildJwtAuthResponse(user,jwt);

        }

    private JwtAuthResponse buildJwtAuthResponse(User user, String jwt){
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        return new JwtAuthResponse(jwt, "Bearer", user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
