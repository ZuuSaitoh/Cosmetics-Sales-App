package com.example.demo.service;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdatePasswordRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.entity.Users;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UsersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UsersService {

    @Autowired
    UsersRepository usersRepository;

    public Users create(UserCreationRequest request){
        if (usersRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);
        if (usersRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        if (!request.getPassword().matches(request.getConfirm_password()))
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        Users users = new Users();
        users.setUsername(request.getUsername());
        users.setPasswordHash(request.getPassword());
        users.setEmail(request.getEmail());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        users.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if(request.getRole().equals("User") || request.getRole().equals("Admin")||request.getRole().equals("Staff")){
            users.setRole(request.getRole());
        } else{
            throw new AppException(ErrorCode.INVALID_ROLE);
        }
        return usersRepository.save(users);
    }
    public int getUserIDByUsername(String username){
        return  usersRepository.findByUsername(username).map(Users::getUserID).orElseThrow(() -> new RuntimeException("Customer not found"));    }

    public List<Users> getAllUsers(){
        return usersRepository.findAll();
    }

    public Users getUserByID(int userID){
        return usersRepository.findById(userID).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public Users updateUser(int userID, UserUpdateRequest request){
        Users users = getUserByID(userID);
        if(request.getPhoneNumber()!=null)
            users.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress()!=null)
            users.setAddress(request.getAddress());
        if (request.getEmail()!=null)
            users.setEmail(request.getEmail());
        if (request.getRole()!=null)
            users.setRole(request.getRole());
        return usersRepository.save(users);
    }

    public void deleteUser(int userID){
        usersRepository.deleteById(userID);
    }

    public Users updatePassword(int userID, UserUpdatePasswordRequest request){
        Users users = getUserByID(userID);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        users.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return usersRepository.save(users);
    }
}
