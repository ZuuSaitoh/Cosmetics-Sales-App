package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateQuantityProductRequest {
    @Min(value = 0, message = "QUANTITY_CANNOT_BE_NEGATIVE")
    int instockQuantity;
}
