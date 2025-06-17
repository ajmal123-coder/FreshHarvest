package com.Fresh_harvest.Backend.controller;

import com.Fresh_harvest.Backend.config.CustomUserDetails;
import com.Fresh_harvest.Backend.dto.ProductCreateRequest;
import com.Fresh_harvest.Backend.dto.ProductResponseDto;
import com.Fresh_harvest.Backend.dto.ProductUpdateRequest;
import com.Fresh_harvest.Backend.exception.ResourceNotFoundException;
import com.Fresh_harvest.Backend.exception.UnauthorizedAccessException;
import com.Fresh_harvest.Backend.model.Category;
import com.Fresh_harvest.Backend.model.Product;
import com.Fresh_harvest.Backend.model.Seller;
import com.Fresh_harvest.Backend.repository.SellerRepository;
import com.Fresh_harvest.Backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Tag(name = "Products", description = "Product management APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final SellerRepository sellerRepository;


    //----------------------Customer End Points--------------------------------------------------------

    @Operation(
            summary = "Get all available products (paginated)",
            description = "Retrieves a paginated list of products that are currently marked as available to customers. Access: Public."
    )
    @GetMapping("/available")
    public ResponseEntity<Page<ProductResponseDto>> getAllAvailableProducts(@ParameterObject Pageable pageable){
        Page<Product> products = productService.getAllAvailableProducts(pageable);
        return ResponseEntity.ok(products.map(this::convertToProductResponseDto));
    }

    @Operation(
            summary = "Get available product by category - paginated",
            description = "Fetches all products belonging to a specific category for a customer. Access: Public."
    )
    @GetMapping("/available/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDto>> getAvailableProductByCategory(@PathVariable Long categoryId, Pageable pageable){
        Page<Product> products = productService.getAvailableProductByCategory(categoryId, pageable);
        return ResponseEntity.ok(products.map(this::convertToProductResponseDto));
    }

    @Operation(
            summary = "Search available products by name (paginated)",
            description = "Searches for available products whose names contain the given string, case-insensitively, with pagination. Access: Public."
    )
    @GetMapping("/available/search")
    public ResponseEntity<Page<ProductResponseDto>> searchAvailableProductsByName(
            @RequestParam String name, Pageable pageable){
        Page<Product> products = productService.searchAvailableProductsByName(name, pageable);
        return ResponseEntity.ok(products.map(this::convertToProductResponseDto));
    }

    @Operation(
            summary = "Get any product by ID",
            description = "Retrieves details of any product by its ID. Can be used by customers to fetch public products, or by authenticated users to fetch any product if they have the ID. For public access, ensure the corresponding service method handles 'isAvailable' if desired."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id){
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(convertToProductResponseDto(product));
    }

    //---------------------------------Seller Product End Points--------------------------------------------

    @Operation(
            summary = "Seller: Create a new product with optional image file",
            description = "Allows an **authenticated seller** to create a new product for their shop. " +
                    "Product details are provided as JSON, and an optional image file can be uploaded. " +
                    "The product will be associated with the currently authenticated seller. " +
                    "Requires SELLER role."
    )
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponseDto> createProductForSeller(
            @Parameter(
                    description = "Product details in JSON format. The sellerId in this DTO will be ignored as it's derived from authentication. Example: " +
                            "{ \"name\": \"Organic Apples\", \"description\": \"Freshly picked from farm.\", \"price\": 2.99, \"categoryId\": 101, \"stockQuantity\": 100, \"isAvailable\": true }",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductCreateRequest.class))
            )
            @RequestPart("product") @Valid String productJson,
            @Parameter(
                    description = "Optional image file for the product.",
                    required = false,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Attempting to create product.  Principal: {}, Authorities: {}", authentication.getPrincipal(), authentication.getAuthorities());

        ProductCreateRequest createRequest = objectMapper.readValue(productJson, ProductCreateRequest.class);

        Long requestingUserId = getCurrentAuthenticatedUserId();
        Long requestingSellerId = getSellerIdFromUserId(requestingUserId);

        Product createdProduct = productService.createProduct(createRequest, requestingSellerId, file);

        return new ResponseEntity<>(convertToProductResponseDto(createdProduct), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Seller: Update an existing product with optional new image file",
            description = "Allows an **authenticated seller** to update their **own** product details and optionally replace the product image. " +
                    "Requires SELLER role."
    )
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponseDto> updateProductForSeller(
            @PathVariable Long id,
            @Parameter(
                    description = "Product details for update in JSON format. Example: " +
                            "{ \"name\": \"Fresh Apples (Organic)\", \"price\": 13.00, \"isAvailable\": false, \"categoryId\": 102}",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductUpdateRequest.class))
            )
            @RequestPart("product") @Valid String productJson,
            @Parameter(
                    description = "Optional new image file for the product. If provided, replaces existing image. " +
                            "To remove image, ensure 'imageUrl' is explicitly null in JSON and no 'file' is provided.",
                    required = false,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ProductUpdateRequest updateRequest = objectMapper.readValue(productJson, ProductUpdateRequest.class);

        Long requestingUserId = getCurrentAuthenticatedUserId();
        Long requestingSellerId = getSellerIdFromUserId(requestingUserId);

        Product updatedProduct = productService.updateProduct(id, updateRequest, requestingSellerId, file);

        return ResponseEntity.ok(convertToProductResponseDto(updatedProduct));
    }

    @Operation(
            summary = "Seller: Delete a product",
            description = "Allows an **authenticated seller** to delete their **own** product by ID. Requires SELLER role."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteProductForSeller(@PathVariable Long id) {
        Long requestingUserId = getCurrentAuthenticatedUserId();
        Long requestingSellerId = getSellerIdFromUserId(requestingUserId);

        productService.deleteProduct(id, requestingSellerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Seller: Partially update an existing product with optional new image file",
            description = "Allows an **authenticated seller** to partially update their **own** product details and optionally replace the product image. " +
                    "Only provided fields will be updated. Requires SELLER role."
    )
    @PatchMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponseDto> patchProductForSeller(
            @PathVariable Long id,
            @Parameter(
                    description = "Product details for partial update in JSON format. Only provide fields you wish to change. Example: " +
                            "{ \"price\": 15.50, \"isAvailable\": false }",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductUpdateRequest.class))
            )
            @RequestPart("product") @Valid String productJson, // Keep as String for multipart
            @Parameter(
                    description = "Optional new image file for the product. If provided, replaces existing image. " +
                            "To remove image, ensure 'imageUrl' is explicitly null or empty string in JSON and no 'file' is provided.",
                    required = false,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Attempting to patch product {} by seller. Principal: {}, Authorities: {}", id, authentication.getPrincipal(), authentication.getAuthorities());

        ProductUpdateRequest updateRequest = objectMapper.readValue(productJson, ProductUpdateRequest.class);

        Long requestingUserId = getCurrentAuthenticatedUserId();
        Long requestingSellerId = getSellerIdFromUserId(requestingUserId);

        Product patchedProduct = productService.patchProduct(id, updateRequest, requestingSellerId, file);

        return ResponseEntity.ok(convertToProductResponseDto(patchedProduct));
    }

    //---------------------------------Admin End Points----------------------------------------------------

    @Operation(
            summary = "Admin: Get all products (paginated)",
            description = "Retrieves a paginated list of ALL products, including unavailable ones. Admin only."
    )
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductResponseDto>> getAllProductsForAdmin(Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products.map(this::convertToProductResponseDto));
    }

    @Operation(
            summary = "Admin: Search all products by name (paginated)",
            description = "Searches for all products (available or not) whose names contain the given string, case-insensitively, with pagination. Admin only."
    )
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductResponseDto>> searchProductsByNameForAdmin(@RequestParam String name, Pageable pageable) {
        Page<Product> products = productService.searchProductsByName(name, pageable);
        return ResponseEntity.ok(products.map(this::convertToProductResponseDto));
    }

    @Operation(
            summary = "Admin: Delete any product by ID",
            description = "Allows an **administrator** to delete any product by its ID. Requires ADMIN role."
    )
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteProduct(@PathVariable Long id) {
        productService.adminDeleteProduct(id); // No seller ID check needed for admin delete
        return ResponseEntity.noContent().build();
    }

    //---------------------------------Common Helper Methods------------------------------------------------

    private ProductResponseDto convertToProductResponseDto(Product product){
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setIsAvailable(product.getIsAvailable());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getSeller() != null){
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerName(product.getSeller().getSellerName());
        }
        if (product.getCategory() != null){
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
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
            // This case indicates an unexpected principal type; likely a configuration error.
            throw new UnauthorizedAccessException("Could not retrieve user ID from authentication principal. Unexpected type: " + principal.getClass().getName());
        }
    }


    private Seller getSellerFromUserId(Long userId) {
        Optional<Seller> sellerOptional = sellerRepository.findByUserId(userId);
        return sellerOptional
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for authenticated user ID: " + userId + ". A seller profile is required for this operation."));
    }


    private Long getSellerIdFromUserId(Long userId) {
        return getSellerFromUserId(userId).getId();
    }
}