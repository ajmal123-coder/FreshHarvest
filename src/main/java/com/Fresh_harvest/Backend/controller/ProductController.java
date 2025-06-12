package com.Fresh_harvest.Backend.controller;

import com.Fresh_harvest.Backend.dto.ProductMultipartRequest;
import com.Fresh_harvest.Backend.model.Product;
import com.Fresh_harvest.Backend.service.CloudinaryService;
import com.Fresh_harvest.Backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Products", description = "Product management APIs")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;
    @Autowired
    public ProductController(ProductService productService, CloudinaryService cloudinaryService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Create a product with optional image file",
            description = "Creates a new product by uploading product JSON and optional image file",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = ProductMultipartRequest.class)
                    )
            )
    )
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") @Valid String productJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            Product product = objectMapper.readValue(productJson, Product.class);

            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(file);
                product.setImageUrl(imageUrl);
            }

            Product createdProduct = productService.createProduct(product);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Update a product with optional new image file",
            description = "Updates product details and optionally replaces the product image",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = ProductMultipartRequest.class)
                    )
            )
    )
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") @Valid String productJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            Product productDetails = objectMapper.readValue(productJson, Product.class);

            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(file);
                productDetails.setImageUrl(imageUrl);
            }

            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
