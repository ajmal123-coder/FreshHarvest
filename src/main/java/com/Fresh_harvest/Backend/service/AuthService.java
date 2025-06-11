package com.Fresh_harvest.Backend.service;

import com.Fresh_harvest.Backend.config.JwtUtil;
import com.Fresh_harvest.Backend.dto.JwtAuthResponse;
import com.Fresh_harvest.Backend.dto.UserLoginDTO;
import com.Fresh_harvest.Backend.dto.UserRegistrationDTO;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleResult;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public JwtAuthResponse registerUser(UserRegistrationDTO registrationDTO){
        if (userRepository.existsByUsername(registrationDTO.getUsername())){
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(registrationDTO.getEmail())){
            throw new UserAlreadyExistsException("Email is already registered");
        }

        Role userRole = roleRepository.findByName(ERole.valueOf("ROLE_"+registrationDTO.getRole().toUpperCase()))
                .orElseGet(()->{
                    Role newRole = new Role(ERole.valueOf("ROLE_"+registrationDTO.getRole().toUpperCase()));
                    return roleRepository.save(newRole);
                });

        User user = User.builder()
                .username(registrationDTO.getUsername())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .email(registrationDTO.getEmail())
                .roles(Collections.singleton(userRole))
                .build();

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registrationDTO.getUsername(),
                        registrationDTO.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtil.generateToken(authentication);

        return buildJwtAuthResponse(savedUser, jwt);
    }
    public JwtAuthResponse loginUser(UserLoginDTO loginDTO){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsernameOrEmail(),
                            loginDTO.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtil.generateToken(authentication);

            User user = userRepository.findByUsername(loginDTO.getUsernameOrEmail())
                    .orElseGet(()-> userRepository.findByEmail(loginDTO.getUsernameOrEmail())
                            .orElseThrow(()->new InvalidCredentialsException("User not Found")));

            return buildJwtAuthResponse(user,jwt);

        }catch (AuthenticationException a){
            throw new InvalidCredentialsException("Invalid username/email or password");
        }
    }
    private JwtAuthResponse buildJwtAuthResponse(User user, String jwt){
        String primaryRole = user.getRoles().stream()
                .map(role -> role.getName().name().replace("ROLE","").toLowerCase())
                .findFirst()
                .orElse("customer");
        return new JwtAuthResponse(jwt, "Bearer", user.getId(), user.getUsername(), user.getEmail(), primaryRole);
    }
}
