package com.Fresh_harvest.Backend.service;

import com.Fresh_harvest.Backend.dto.ProductCreateRequest;
import com.Fresh_harvest.Backend.dto.ProductUpdateRequest;
import com.Fresh_harvest.Backend.exception.ResourceNotFoundException;
import com.Fresh_harvest.Backend.exception.UnauthorizedAccessException;
import com.Fresh_harvest.Backend.exception.ValidationException;
import com.Fresh_harvest.Backend.model.Category;
import com.Fresh_harvest.Backend.model.Product;
import com.Fresh_harvest.Backend.model.Seller;
import com.Fresh_harvest.Backend.model.SellerStatus;
import com.Fresh_harvest.Backend.repository.CategoryRepository;
import com.Fresh_harvest.Backend.repository.ProductRepository;
import com.Fresh_harvest.Backend.repository.SellerRepository;
import lombok.RequiredArgsConstructor; // Add Lombok annotation
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;


    //----------------------Customer Read End Points--------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<Product> getAllAvailableProducts(Pageable pageable){
        return productRepository.findByIsAvailableTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAvailableProductByCategory(Long categoryId, Pageable pageable){
        if (!categoryRepository.existsById(categoryId)){
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }
        return productRepository.findByCategoryIdAndIsAvailableTrue(categoryId, pageable); // Assuming isAvailable is true for customer view
    }

    @Transactional(readOnly = true)
    public Page<Product> searchAvailableProductsByName(String name, Pageable pageable){
        return productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(name,pageable); // Assuming isAvailable is true for customer view
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    //---------------------------------Seller Product Management--------------------------------------------

    @Transactional
    public Product createProduct(ProductCreateRequest createRequest, Long sellerId, MultipartFile file) throws IOException {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(()->new ResourceNotFoundException("Seller not found with ID: " + sellerId));

        if (seller.getStatus() != SellerStatus.APPROVED){
            throw new ValidationException("Seller is not approved to list products. Status: " + seller.getStatus());
        }

        Category category = categoryRepository.findById(createRequest.getCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Category not found with ID: " + createRequest.getCategoryId()));

        Product product = new Product();
        product.setName(createRequest.getName());
        product.setDescription(createRequest.getDescription());
        product.setPrice(createRequest.getPrice());
        product.setStockQuantity(Optional.ofNullable(createRequest.getStockQuantity()).orElse(0)); // Default to 0 if null
        product.setIsAvailable(Optional.ofNullable(createRequest.getIsAvailable()).orElse(false)); // Default to false if null

        if (product.getStockQuantity() < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }

        // Handle image upload
        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(file);
            product.setImageUrl(imageUrl);
        }

        product.setSeller(seller);
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductUpdateRequest updateRequest, Long requestingSellerId, MultipartFile file) throws IOException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found with ID: " + id));


        if (!existingProduct.getSeller().getId().equals(requestingSellerId)){
            throw new UnauthorizedAccessException("You are not authorized to update this product. It does not belong to your seller profile.");
        }


        Optional.ofNullable(updateRequest.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(updateRequest.getDescription()).ifPresent(existingProduct::setDescription);
        Optional.ofNullable(updateRequest.getPrice()).ifPresent(existingProduct::setPrice);

        if (updateRequest.getStockQuantity() != null) {
            if (updateRequest.getStockQuantity() < 0) {
                throw new ValidationException("Stock quantity cannot be negative.");
            }
            existingProduct.setStockQuantity(updateRequest.getStockQuantity());
        }

        Optional.ofNullable(updateRequest.getIsAvailable()).ifPresent(existingProduct::setIsAvailable);

        if (updateRequest.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(updateRequest.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + updateRequest.getCategoryId()));
            existingProduct.setCategory(newCategory);
        }

        if (file != null && !file.isEmpty()) {
            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty()) {
                cloudinaryService.deleteFileByUrl(existingProduct.getImageUrl()); // Assuming delete by URL
            }
            String newImageUrl = cloudinaryService.uploadFile(file);
            existingProduct.setImageUrl(newImageUrl);
        } else if (updateRequest.getImageUrl() != null && updateRequest.getImageUrl().isEmpty()) {
            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty()) {
                cloudinaryService.deleteFileByUrl(existingProduct.getImageUrl());
            }
            existingProduct.setImageUrl(null);
        }

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id, Long requestingSellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (!product.getSeller().getId().equals(requestingSellerId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this product. It does not belong to your seller profile.");
        }


        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            cloudinaryService.deleteFileByUrl(product.getImageUrl());
        }

        productRepository.delete(product);
    }

    @Transactional
    public Product patchProduct(Long productId, ProductUpdateRequest updateRequest, Long sellerId, MultipartFile file) throws IOException {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!existingProduct.getSeller().getId().equals(sellerId)) {
            throw new UnauthorizedAccessException("Seller is not authorized to update this product.");
        }

        if (existingProduct.getSeller().getStatus() != SellerStatus.APPROVED) {
            throw new ValidationException("Seller is not approved to list products. Current Status: " + existingProduct.getSeller().getStatus());
        }

        if (updateRequest.getName() != null) {
            existingProduct.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            existingProduct.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            existingProduct.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getStockQuantity() != null) {
            if (updateRequest.getStockQuantity() < 0) {
                throw new ValidationException("Stock quantity cannot be negative.");
            }
            existingProduct.setStockQuantity(updateRequest.getStockQuantity());
        }
        if (updateRequest.getIsAvailable() != null) {
            existingProduct.setIsAvailable(updateRequest.getIsAvailable());
        }

        if (updateRequest.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(updateRequest.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + updateRequest.getCategoryId()));
            existingProduct.setCategory(newCategory);
        }

        if (file != null && !file.isEmpty()) {
            // Delete old image if exists
            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty()) {
                cloudinaryService.deleteFileByUrl(existingProduct.getImageUrl());
            }

            String newImageUrl = cloudinaryService.uploadFile(file);
            existingProduct.setImageUrl(newImageUrl);
        } else if (updateRequest.getImageUrl() != null && updateRequest.getImageUrl().isEmpty()) {

            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty()) {
                cloudinaryService.deleteFileByUrl(existingProduct.getImageUrl());
            }
            existingProduct.setImageUrl(null);
        }


        return productRepository.save(existingProduct);
    }

    //---------------------------------Admin Product Management--------------------------------------------

    @Transactional
    public Product adminSetProductAvailability(Long productId, boolean isAvailable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        product.setIsAvailable(isAvailable);
        return productRepository.save(product);
    }

    @Transactional
    public void adminDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));


        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            cloudinaryService.deleteFileByUrl(product.getImageUrl());
        }

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findBySellerAndIsAvailable(Seller seller, Pageable pageable) {
        return productRepository.findBySellerAndIsAvailableTrue(seller, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findBySellerAndIsAvailableFalse(Seller seller, Pageable pageable) {
        return productRepository.findBySellerAndIsAvailableFalse(seller, pageable);
    }
}