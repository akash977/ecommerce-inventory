package com.example.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplyRequest {

    @NotBlank (message="sku is Mandatory")
    private String sku;

    @NotBlank (message="Item Name is Mandatory ")
    private String itemName;

    @Min(value=1,message=" Quantity must be atLeast 1 ")
    private int quantity;
}
