package com.example.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReserveRequest {

    @NotBlank(message="sku can not be empty")
    private String sku;

    @Min(value=1,message=" Quantity must be atLeast 1 ")
    private int quantity;

}
