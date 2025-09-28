package com.example.demo.controller;

import com.example.demo.dto.request.CategoryCreationRequest;
import com.example.demo.dto.request.CategoryUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Category;
import com.example.demo.entity.Users;
import com.example.demo.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @PostMapping("/create")
    ApiResponse<Category> create(@RequestBody @Valid CategoryCreationRequest request){
        ApiResponse<Category> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.createCategory(request));
        return apiResponse;
    }

    @GetMapping("/{id}")
    Category getCatogoryByID(@PathVariable int id){
        return categoryService.getCategoryByID(id);
    }

    @PutMapping("/update/{id}")
    Category updateCategoryByID(@PathVariable int id, @RequestBody CategoryUpdateRequest request){
        return categoryService.updateCategoryByID(id, request);
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<String> deleteUserById(@PathVariable int id){
        categoryService.deleteCategory(id);
        return new ApiResponse<String>(1012,"Category Deleted!");
    }

    @GetMapping("/fetchAll")
    ApiResponse<List<Category>> getAllCategory(){
        return new ApiResponse<List<Category>>(9999,"List of Categories", categoryService.getAllCategories());
    }

}
