package com.Fresh_harvest.Backend.service;

import com.Fresh_harvest.Backend.model.Product;
import com.Fresh_harvest.Backend.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.Fresh_harvest.Backend.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found" + id));
    }

    @Transactional
    public Product createProduct(Product product){
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product NOT found"));
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setImageUrl(productDetails.getImageUrl());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id){
        if (!productRepository.existsById(id)){
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

}

