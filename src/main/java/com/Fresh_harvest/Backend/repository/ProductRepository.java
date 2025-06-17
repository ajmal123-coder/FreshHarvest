package com.Fresh_harvest.Backend.repository;


import com.Fresh_harvest.Backend.model.Product;
import com.Fresh_harvest.Backend.model.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("UPDATE Product p SET p.category = NULL, p.isAvailable = FALSE WHERE p.category.id = :categoryId")
    void disassociateProductsAndMarkUnavailableByCategoryId(@org.springframework.data.repository.query.Param("categoryId") Long categoryId);



    Page<Product> findByNameContainingIgnoreCase(String name,Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndIsAvailableTrue(String name, Pageable pageable);

    Page<Product> findByIsAvailableTrue(Pageable pageable);

    Page<Product>findByCategoryIdAndIsAvailableTrue(Long categoryId, Pageable pageable);

    Page<Product> findBySellerAndIsAvailableTrue(Seller seller, Pageable pageable);

    Page<Product> findBySellerAndIsAvailableFalse(Seller seller, Pageable pageable);
}

