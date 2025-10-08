package com.example.demo.controller;

import com.example.demo.dto.request.StoreLocationCreateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.StoreLocation;
import com.example.demo.service.StoreLocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/store-locations")
public class StoreLocationController {
    @Autowired
    StoreLocationService service;


    @GetMapping("/get-all-store-locations")
    public ApiResponse<List<StoreLocation>> getAll() {
        return new ApiResponse<List<StoreLocation>>(9999, "Get successfully", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<StoreLocation> getById(@PathVariable Integer id) {
        return new ApiResponse<StoreLocation>(9999, "Get successfully", service.getById(id));
    }

    @PostMapping("/create")
    public ApiResponse<StoreLocation> create(@RequestBody @Valid StoreLocationCreateRequest request) {
        return new ApiResponse<StoreLocation>(9999, "Create successfully", service.create(request));
    }

    @PutMapping("/update/{id}")
    public ApiResponse<StoreLocation> update(@PathVariable Integer id, @RequestBody @Valid StoreLocationCreateRequest request) {
        return new ApiResponse<StoreLocation>(9999, "Update successfully", service.update(id, request));
    }

    @DeleteMapping("delete/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        service.deleteStoreLocation(id);
        return new ApiResponse<String>(9999, "Delete successfully");
    }
}
