package com.example.demo.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationCreateRequest {
    @NotNull(message = "ENTER_ALL_FIELDS")
    BigDecimal latitude;
    @NotNull(message = "ENTER_ALL_FIELDS")
    BigDecimal longitude;
    @NotNull(message = "ENTER_ALL_FIELDS")
    String address;
}
