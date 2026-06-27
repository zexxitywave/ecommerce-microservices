package com.hacisimsek.product.repository;

import com.hacisimsek.product.model.Product;
import com.hacisimsek.product.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    @Query("""
    SELECT p FROM Product p
    WHERE (
        CAST(:keyword AS string) IS NULL OR
        LOWER(COALESCE(p.name, '')) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') OR
        LOWER(COALESCE(p.description, '')) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') OR
        LOWER(COALESCE(p.brand, '')) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%')
    )
    AND (:categoryId IS NULL OR p.category.id = :categoryId)
    AND (:status IS NULL OR p.status = :status)
    AND (:minPrice IS NULL OR p.price >= :minPrice)
    AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    AND (CAST(:brand AS string) IS NULL OR p.brand = CAST(:brand AS string))
""")
    Page<Product> search(
            @Param("keyword")    String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("status")     ProductStatus status,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            @Param("brand")      String brand,
            Pageable pageable
    );
}
