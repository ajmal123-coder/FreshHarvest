package com.Fresh_harvest.Backend.repository;

import com.Fresh_harvest.Backend.model.Seller;
import com.Fresh_harvest.Backend.model.SellerStatus;
import com.Fresh_harvest.Backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {

    Optional<Seller> findByUser(User user);

    Optional<Seller> findByUserId(Long aLong);

    Optional<Seller> findBySellerName(String sellerName);

    List<Seller> findByStatus(SellerStatus status);

    Page<Seller> findByStatus(SellerStatus status, Pageable pageable);
}
