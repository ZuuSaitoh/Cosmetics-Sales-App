package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductsAddRequest {
    @NotBlank(message = "ENTER_ALL_FIELDS")
    String productName;
    @NotBlank(message = "ENTER_ALL_FIELDS")
    String briefDescription;
    @NotBlank(message = "ENTER_ALL_FIELDS")
    String fullDescription;
    @DecimalMin(value = "0.0", inclusive = false, message = "PRICE_MUST_BE_POSITIVE")
    @DecimalMax(value = "1000000000.0", message = "PRICE_TOO_HIGH")
    private BigDecimal price;
    @Min(value = 0, message = "QUANTITY_CANNOT_BE_NEGATIVE")
    int instockQuantity;
    @NotBlank(message = "ENTER_ALL_FIELDS")
    String imageURL;
    @Min(value = 0, message = "CATEGORY_ID_CANNOT_BE_NEGATIVE")
    int categoryID;
    @NotBlank(message = "ENTER_ALL_FIELDS")
    String brand;
}
