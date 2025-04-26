package com.ecommerce.web.dto.request;

import com.ecommerce.web.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    private Long cartId;
    private Double totalPrice;
    private List<CartItem> cartItems = new ArrayList<>();
}
