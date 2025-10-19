package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdatePasswordRequest {
    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String oldPassword;
    @NotBlank(message = "ENTER_ALL_FIELDS")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String newPassword;

}
