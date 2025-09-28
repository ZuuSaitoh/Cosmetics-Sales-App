package com.example.demo.controller;


import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdatePasswordRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Users;
import com.example.demo.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;

    @PostMapping("/create")
    ApiResponse<Users> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<Users> apiResponse = new ApiResponse<>();
        apiResponse.setResult(usersService.create(request));
        return  apiResponse;
    }

    @GetMapping("/getAllUsers")
    ApiResponse<List<Users>> getAllUsers(){
        return new ApiResponse<List<Users>>(9999,"List of Users", usersService.getAllUsers());
    }

    @GetMapping("/{id}")
    Users getUser(@PathVariable int id){
        return usersService.getUserByID(id);
    }

    @PutMapping("/update/{id}")
    Users updateUsers(@PathVariable int id, @RequestBody @Valid UserUpdateRequest request){
        return usersService.updateUser(id, request);
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<String> deleteUserById(@PathVariable int id){
        usersService.deleteUser(id);
        return new ApiResponse<String>(1012,"User Deleted!");
    }

    @PutMapping("/update/password/{id}")
    ApiResponse<String> updatePasswordById(@PathVariable int id, @RequestBody UserUpdatePasswordRequest request){
        usersService.updatePassword(id, request);
        return new ApiResponse<String>(2222,"Update Password Successfully!");
    }
}
