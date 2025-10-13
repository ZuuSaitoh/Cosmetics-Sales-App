package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersPlaceNewOrdersRequest {
    @NotNull(message = "ENTER_ALL_FIELDS")
    int userID;
    @NotNull(message = "ENTER_ALL_FIELDS")
    String paymentMethod;
}
