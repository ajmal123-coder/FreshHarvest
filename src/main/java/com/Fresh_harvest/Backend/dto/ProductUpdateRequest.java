package com.Fresh_harvest.Backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class ProductUpdateRequest {

    @Schema(description = "Updated name of the product", example = "Organic Honey 500g")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    @Schema(description = "Updated description of the product", example = "Pure organic honey, locally sourced from wild beehives.")
    @Size(max = 1000, message = "Product description cannot exceed 1000 characters")
    private String description;

    @Schema(description = "Updated price of the product", example = "15.99")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    private BigDecimal price;

    @Schema(description = "Updated stock quantity of the product", example = "200")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Schema(description = "Updated availability status of the product", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Updated ID of the product's category", example = "102")
    @Min(value = 1, message = "Category ID must be positive")
    private Long categoryId;

    @Schema(description = "Updated image URL of the product. Set to null or empty string to remove existing image.", example = "http://example.com/new-honey-image.jpg")
    private String imageUrl;

}