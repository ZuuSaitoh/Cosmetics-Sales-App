package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsersUpdatePasswordByMailRequest {
    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Email(message = "INVALID_EMAIL")
    String mail;
    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String newPassword;
}
