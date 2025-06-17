package com.Fresh_harvest.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "product name cannot be empty")
    @Size(max = 255, message = "product name cant exceed 255 characters")
    private String name;

    @NotBlank(message = "product description cannot be empty")
    @Size(max = 1000, message = "product name cant exceed 1000 characters")
    private String description;

    @NotNull(message = "price cannot be null")
    @DecimalMin(value = "0.01",message = "price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Please Provide Stock Quantity")
    @Min(value = 0,message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Size(max = 700, message = "Image URL can't exceed 700 characters")
    private String imageUrl;


    @NotNull(message = "Cant empty")
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    @NotNull(message = "Product must associated with a seller")
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Product must have category")
    private Category category;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}

