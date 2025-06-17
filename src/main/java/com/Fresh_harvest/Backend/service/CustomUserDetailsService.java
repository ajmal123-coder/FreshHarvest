package com.Fresh_harvest.Backend.service;

import com.Fresh_harvest.Backend.config.CustomUserDetails;
import com.Fresh_harvest.Backend.model.User;
import com.Fresh_harvest.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(()->userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(()->new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail )));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        logger.info("Loading user: {} with authorities: {}",usernameOrEmail,customUserDetails.getAuthorities());

        return customUserDetails;
    }
}
