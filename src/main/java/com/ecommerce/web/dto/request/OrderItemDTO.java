package com.ecommerce.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private Integer orderedQuantity;
    private Double orderedProductPrice;
    private Double discount;
    private ProductDTO product;
}
