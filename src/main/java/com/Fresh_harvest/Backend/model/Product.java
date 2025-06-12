package com.Fresh_harvest.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @Size(max = 700, message = "Image URL can't exceed 700 characters")
    private String imageUrl;
}

