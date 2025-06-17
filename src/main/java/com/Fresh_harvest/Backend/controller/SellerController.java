package com.Fresh_harvest.Backend.controller;

import com.Fresh_harvest.Backend.dto.SellerRegistrationRequestDto;
import com.Fresh_harvest.Backend.dto.SellerResponseDto;
import com.Fresh_harvest.Backend.dto.SellerUpdateRequestDto;
import com.Fresh_harvest.Backend.exception.UnauthorizedAccessException;
import com.Fresh_harvest.Backend.model.Seller;
import com.Fresh_harvest.Backend.model.SellerStatus;
import com.Fresh_harvest.Backend.config.CustomUserDetails;
import com.Fresh_harvest.Backend.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sellers", description = "Seller management APIs for registration, profile, and admin actions")
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    // --- Public/Registration Endpoints ---

    @Operation(summary = "Register a new seller account",
            description = "Allows a new user to register as a seller, creating both a user account and a seller profile. Publicly accessible.")
    @PostMapping("/register")
    public ResponseEntity<SellerResponseDto> registerSeller(@Valid @RequestBody SellerRegistrationRequestDto registrationDto) {
        Seller registeredSeller = sellerService.registerSeller(registrationDto);
        return new ResponseEntity<>(convertToSellerResponseDto(registeredSeller), HttpStatus.CREATED);
    }

    @Operation(summary = "Get seller by ID",
            description = "Retrieves details of a specific seller by their ID. Accessible to all users.")
    @GetMapping("/{id}")
    public ResponseEntity<SellerResponseDto> getSellerById(@PathVariable Long id) {
        Seller seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(convertToSellerResponseDto(seller));
    }

    @Operation(summary = "Get all sellers (paginated)",
            description = "Retrieves a paginated list of all registered sellers. Accessible to all users.")
    @GetMapping
    public ResponseEntity<Page<SellerResponseDto>> getAllSellers(Pageable pageable) {
        Page<Seller> sellers = sellerService.getAllSellers(pageable);
        return ResponseEntity.ok(sellers.map(this::convertToSellerResponseDto));
    }

    // --- Seller-Specific Endpoints (Requires ROLE_SELLER) ---

    @Operation(summary = "Get current authenticated seller's profile",
            description = "Retrieves the profile of the currently logged-in seller. Requires SELLER role.")
    @GetMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerResponseDto> getCurrentSellerProfile() {
        Long currentUserId = getCurrentAuthenticatedUserId();
        Seller seller = sellerService.getSellerByUSerId(currentUserId);
        return ResponseEntity.ok(convertToSellerResponseDto(seller));
    }

    @Operation(summary = "Update current authenticated seller's profile",
            description = "Allows the currently logged-in seller to update their own profile details. Requires SELLER role.")
    @PutMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerResponseDto> updateCurrentSellerProfile(@Valid @RequestBody SellerUpdateRequestDto updateDto) {
        Long currentUserId = getCurrentAuthenticatedUserId();
        Seller updatedSeller = sellerService.updateSellerProfile(
                sellerService.getSellerByUSerId(currentUserId).getId(), // Get current seller's ID
                updateDto,
                currentUserId
        );
        return ResponseEntity.ok(convertToSellerResponseDto(updatedSeller));
    }

    // --- Admin-Specific Endpoints (Requires ROLE_ADMIN) ---

    @Operation(summary = "Admin: Update seller status by ID",
            description = "Allows an administrator to change the status of any seller (e.g., PENDING, APPROVED, BLOCKED). Requires ADMIN role.")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerResponseDto> updateSellerStatus(@PathVariable Long id,
                                                                @RequestParam SellerStatus status) {
        Seller updatedSeller = sellerService.updateSellerStatus(id, status);
        return ResponseEntity.ok(convertToSellerResponseDto(updatedSeller));
    }

    @Operation(summary = "Admin: Delete seller by ID",
            description = "Allows an administrator to delete a seller account and their associated user account. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeller(@PathVariable Long id) {
        sellerService.deleteSeller(id);
    }

    // --- Helper Methods ---

    private SellerResponseDto convertToSellerResponseDto(Seller seller) {
        SellerResponseDto dto = new SellerResponseDto();
        dto.setId(seller.getId());
        dto.setSellerName(seller.getSellerName());
        dto.setAddress(seller.getAddress());
        dto.setPhoneNumber(seller.getPhoneNumber());
        dto.setBankAccountDetails(seller.getBankAccountDetails());
        dto.setStatus(seller.getStatus());
        dto.setBlocked(seller.isBlocked()); // Assuming isBlocked field on Seller model

        // Populate User details from the associated User entity
        if (seller.getUser() != null) {
            dto.setUserId(seller.getUser().getId());
            dto.setUsername(seller.getUser().getUsername());
            dto.setEmail(seller.getUser().getEmail());
        }

        dto.setCreatedAt(seller.getCreatedAt());
        dto.setUpdatedAt(seller.getUpdatedAt());
        return dto;
    }

    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("User not authenticated.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        } else {
            throw new UnauthorizedAccessException("Could not retrieve user ID from authentication principal. Unexpected principal type.");
        }
    }
}