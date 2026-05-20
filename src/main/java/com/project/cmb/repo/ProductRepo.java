package com.project.cmb.repo;

import com.project.cmb.entity.Product;
import com.project.cmb.projection.ProductListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

@RepositoryRestResource(path = "products")
public interface ProductRepo extends JpaRepository<Product, String> {

    Page<ProductListView> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

    Page<ProductListView> findByProductLine_ProductLine(String productLine, Pageable pageable);

    Page<ProductListView> findByProductVendorContainingIgnoreCase(String vendor, Pageable pageable);

    Page<ProductListView> findByProductCodeContainingIgnoreCase(String productCode, Pageable pageable);

    List<ProductListView> findByQuantityInStockLessThan(Short threshold);

    List<ProductListView> findAllProjectedBy();

}