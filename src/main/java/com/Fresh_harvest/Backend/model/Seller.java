package com.Fresh_harvest.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "id", unique = true, nullable = false)
    @NotNull(message = "Seller must have user")
    private User user;

    @NotBlank(message = "Seller Name Cant be empty")
    @Size(max = 255, message = "Shop name cant be exceed 255 characters")
    @Column(unique = true, nullable = false)
    private String sellerName;

    @NotBlank(message = "Please Provide Your Address")
    @Size(max = 500, message = "Maximum characters allowed are 500")
    private String address;

    @NotBlank(message = "Phone Number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    @Size(max = 15, message = "Phone number can't Exceed 15 characters")
    private String phoneNumber;

//    Encrypt this - don't forget
    @Size(max = 255, message = "Bank account details can't exceed 255 characters")
    private String bankAccountDetails;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Cannot be null")
    private SellerStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Product> products;

    @Column(nullable = false)
    private boolean isBlocked = false;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.status == null){
            this.status = SellerStatus.PENDING;
        }
    }
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
