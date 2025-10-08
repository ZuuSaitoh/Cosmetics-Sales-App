package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Size(min = 3,message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String confirm_password;

    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Email(message = "INVALID_EMAIL")
    String email;

    @Pattern(
            regexp = "^(\\+84|0)(3|5|7|8|9)[0-9]{8}$",
            message = "PHONE_NUMBER_INVALID",
            flags = Pattern.Flag.CASE_INSENSITIVE
    )
    String phoneNumber;

    @NotBlank(message = "ENTER_ALL_FIELDS")
    String role;
}
