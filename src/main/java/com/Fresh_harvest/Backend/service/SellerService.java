package com.Fresh_harvest.Backend.service;

import com.Fresh_harvest.Backend.dto.SellerRegistrationRequestDto;
import com.Fresh_harvest.Backend.dto.SellerUpdateRequestDto;
import com.Fresh_harvest.Backend.exception.ResourceNotFoundException;
import com.Fresh_harvest.Backend.exception.UnauthorizedAccessException;
import com.Fresh_harvest.Backend.exception.UserAlreadyExistsException;
import com.Fresh_harvest.Backend.exception.ValidationException;
import com.Fresh_harvest.Backend.model.Role;
import com.Fresh_harvest.Backend.model.ERole;
import com.Fresh_harvest.Backend.model.Seller;
import com.Fresh_harvest.Backend.model.SellerStatus;
import com.Fresh_harvest.Backend.model.User;
import com.Fresh_harvest.Backend.repository.RoleRepository;
import com.Fresh_harvest.Backend.repository.SellerRepository;
import com.Fresh_harvest.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    @Transactional
    public Seller registerSeller(SellerRegistrationRequestDto registrationDto){

        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()){
            throw new UserAlreadyExistsException("Username '" + registrationDto.getUsername() + "' already exists.");
        }

        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email '" + registrationDto.getEmail() + "' is already registered.");
        }

        if (registrationDto.getPassword() == null || registrationDto.getPassword().isEmpty()){
            throw new ValidationException("Password cannot be empty for registration.");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role sellerRole = roleRepository.findByName(ERole.ROLE_SELLER)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Seller Role is not found."));
        roles.add(sellerRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        if (sellerRepository.findBySellerName(registrationDto.getSellerName()).isPresent()) {
            throw new UserAlreadyExistsException("Seller name '" + registrationDto.getSellerName() + "' already exists.");
        }

        Seller seller = new Seller();
        seller.setSellerName(registrationDto.getSellerName());
        seller.setAddress(registrationDto.getAddress());
        seller.setPhoneNumber(registrationDto.getPhoneNumber());
        seller.setBankAccountDetails(registrationDto.getBankAccountDetails());
        seller.setStatus(SellerStatus.PENDING);

        seller.setUser(savedUser);
        return sellerRepository.save(seller);
    }

    @Transactional(readOnly = true)
    public Seller getSellerById(Long sellerId){
        return sellerRepository.findById(sellerId)
                .orElseThrow(()-> new ResourceNotFoundException("Seller not found with ID: " + sellerId));
    }

    @Transactional(readOnly = true)
    public Seller getSellerByUSerId(Long userId){
        return sellerRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Seller not found for User ID: " + userId));
    }

    @Transactional(readOnly = true)
    public Page<Seller> getAllSellers(Pageable pageable){
        return sellerRepository.findAll(pageable);
    }

    @Transactional
    public Seller updateSellerProfile(Long sellerId, SellerUpdateRequestDto updateDto, Long requestingUserId){ // Accepts DTO
        Seller existingSeller = sellerRepository.findById(sellerId)
                .orElseThrow(()->new ResourceNotFoundException("Seller not found with ID: " + sellerId));

        if (!existingSeller.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this seller's profile.");
        }

        Optional.ofNullable(updateDto.getSellerName()).ifPresent(name -> {
            if (!existingSeller.getSellerName().equals(name) && sellerRepository.findBySellerName(name).isPresent()) {
                throw new UserAlreadyExistsException("Seller name '" + name + "' already exists.");
            }
            existingSeller.setSellerName(name);
        });
        Optional.ofNullable(updateDto.getAddress()).ifPresent(existingSeller::setAddress);
        Optional.ofNullable(updateDto.getPhoneNumber()).ifPresent(phone -> {

            if (!phone.matches("^\\+?[0-9. ()-]{7,25}$")) {
                throw new ValidationException("Invalid phone number format.");
            }
            existingSeller.setPhoneNumber(phone);
        });
        Optional.ofNullable(updateDto.getBankAccountDetails()).ifPresent(existingSeller::setBankAccountDetails);

        return sellerRepository.save(existingSeller);
    }

    @Transactional
    public Seller updateSellerStatus(Long sellerId, SellerStatus newStatus){
        Seller existingSeller = sellerRepository.findById(sellerId)
                .orElseThrow(()-> new ResourceNotFoundException("Seller not found with ID: " + sellerId));
        existingSeller.setStatus(newStatus);
        return sellerRepository.save(existingSeller);
    }

    @Transactional
    public void deleteSeller(Long sellerId){ // Fixed typo from deleSeller
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(()->new ResourceNotFoundException("Seller not found with ID: " + sellerId));

        userRepository.delete(seller.getUser());
        sellerRepository.delete(seller);
    }
}