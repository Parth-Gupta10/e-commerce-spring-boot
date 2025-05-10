package com.ecommerce.web.controller;

import com.ecommerce.web.dto.request.OrderDTO;
import com.ecommerce.web.dto.request.OrderRequestDTO;
import com.ecommerce.web.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/payment/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@RequestBody OrderRequestDTO orderRequestDTO,
                                                  @PathVariable String paymentMethod) {
        return new ResponseEntity<>(orderService.placeOrder(orderRequestDTO, paymentMethod), HttpStatus.CREATED);
    }
}

