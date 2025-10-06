package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemsAddProductToCartRequest {
    @NotNull(message = "ENTER_ALL_FIELDS")
    Integer cartID;
    @NotNull(message = "ENTER_ALL_FIELDS")
    Integer productID;
    @NotNull(message = "ENTER_ALL_FIELDS")
    @Min(value = 1, message = "QUANTITY_CANNOT_BE_NEGATIVE")
    Integer quantity;
}
