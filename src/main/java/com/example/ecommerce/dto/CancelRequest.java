package com.example.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelRequest {

    @Min(value=1,message=" Quantity must be atLeast 1 ")
    private int quantity;
}

