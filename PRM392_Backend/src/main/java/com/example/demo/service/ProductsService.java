package com.example.demo.service;

import com.example.demo.dto.request.ProductsAddRequest;
import com.example.demo.dto.request.UpdateProductInfoRequest;
import com.example.demo.dto.request.UpdateQuantityProductRequest;
import com.example.demo.entity.Category;
import com.example.demo.entity.Products;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductsService {
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CategoryService categoryService;

    public Products addNewProducts(ProductsAddRequest request) {
        if (!categoryRepository.existsById(request.getCategoryID())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }
        Category category = categoryService.getCategoryByID(request.getCategoryID());
        Products products = new Products(request.getProductName(), request.getBriefDescription(), request.getFullDescription(),
                request.getPrice(), request.getInstockQuantity(), request.getImageURL(), category, request.getBrand());
        return productsRepository.save(products);
    }

    public Products getProductByID(int id) {
        return productsRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
    }

    public List<Products> getAllProducts() {
        return productsRepository.findAll();
    }

    public void deleteProductByID(int id) {
        productsRepository.deleteById(id);
    }

    public Products updateProductInfo(int id, UpdateProductInfoRequest updateProductInfo) {
        Products products = getProductByID(id);
        if (updateProductInfo.getProductName() != null)
            products.setProductName(updateProductInfo.getProductName());
        if (updateProductInfo.getBriefDescription() != null)
            products.setBriefDescription(updateProductInfo.getBriefDescription());
        if (updateProductInfo.getFullDescription() != null)
            products.setFullDescription(updateProductInfo.getFullDescription());
        if (updateProductInfo.getPrice() != null)
            products.setPrice(updateProductInfo.getPrice());
        if (updateProductInfo.getImageURL() != null)
            products.setImageURL(updateProductInfo.getImageURL());
        if (updateProductInfo.getCategoryID() != null
                && !updateProductInfo.getCategoryID().equals(products.getCategoryID().getCategoryID())) { //trong Category co categoryID
            Category category = categoryService.getCategoryByID(updateProductInfo.getCategoryID());
            products.setCategoryID(category);
        }
        if (updateProductInfo.getBrand() != null)
            products.setBrand(updateProductInfo.getBrand());

        return productsRepository.save(products);
    }

    public Products updateQuantity(int id, UpdateQuantityProductRequest request) {
        Products products = getProductByID(id);
        if (request.getInstockQuantity() != products.getInstockQuantity()) {
            products.setInstockQuantity(request.getInstockQuantity());
            return productsRepository.save(products);
        } else {
            throw new AppException(ErrorCode.QUANTITY_NOT_CHANGED);
        }
    }

    public List<Products> getProductsByCategoryID(int categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }
        return productsRepository.findByCategoryID_CategoryID(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
    }

}
