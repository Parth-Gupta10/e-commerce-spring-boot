package com.ecommerce.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    @NotBlank
    @Size(min = 3, message = "Product name should be at least 3 characters long")
    private String productName;
    @NotBlank
    @Size(min = 10, message = "Product description should be at least 10 characters long")
    private String productDescription;
    @NotNull
    private Double productPrice;
    @NotNull
    private Integer productQuantity;
    private Double productDiscount;
    private Double productDiscountedPrice;
    private String productImageUrl;

    private CategoryDTO category;
}
