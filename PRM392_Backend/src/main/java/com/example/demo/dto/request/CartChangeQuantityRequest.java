package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartChangeQuantityRequest {
    @NotNull(message = "ENTER_ALL_FIELDS")
    Integer cartItemsID;
    @NotNull(message = "ENTER_ALL_FIELDS")
    Integer Quantity;
}
