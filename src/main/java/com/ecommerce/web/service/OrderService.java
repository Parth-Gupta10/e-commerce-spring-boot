package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.OrderDTO;
import com.ecommerce.web.dto.request.OrderRequestDTO;

public interface OrderService {
    OrderDTO placeOrder(OrderRequestDTO orderRequestDto, String paymentMethod);
}
