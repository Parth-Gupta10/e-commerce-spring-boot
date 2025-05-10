package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.OrderDTO;
import com.ecommerce.web.dto.request.OrderItemDTO;
import com.ecommerce.web.dto.request.OrderRequestDTO;
import com.ecommerce.web.dto.request.PaymentDTO;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.*;
import com.ecommerce.web.repository.*;
import com.ecommerce.web.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    @Override
    public OrderDTO placeOrder(OrderRequestDTO orderRequestDTO, String paymentMethod) {
        String userEmail = authUtil.loggedInEmail();

        Cart cart = cartRepository.findCartByEmail(userEmail);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", userEmail);
        }

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty, cannot place order");
        }

        Address address = addressRepository.findById(orderRequestDTO.getAddressId()).
                orElseThrow(() -> new ResourceNotFoundException("Address", "id", orderRequestDTO.getAddressId()));

        Order order = new Order();
        order.setEmail(userEmail);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, orderRequestDTO.getPaymentStatus(),
                orderRequestDTO.getPaymentGatewayResponse(), orderRequestDTO.getPaymentGatewayId());
        payment.setOrder(order);

        paymentRepository.save(payment);

        order.setPayment(payment);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrderedQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

        // Collect products to remove
        List<Long> productsToRemove = new ArrayList<>();
        cart.getCartItems().forEach(cartItem -> {
            Integer quantity = cartItem.getQuantity();
            Product product = cartItem.getProduct();

            product.setProductQuantity(product.getProductQuantity() - quantity);
            productRepository.save(product);

            productsToRemove.add(product.getProductId());
        });

        // Remove products from cart after iteration
        productsToRemove.forEach(productId -> cartService.removeProductFromCart(cart.getCartId(), productId));

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        savedOrderItems.forEach(orderItem -> orderDTO.getOrderItemsDTOS().
                add(modelMapper.map(orderItem, OrderItemDTO.class)));

        orderDTO.setAddressId(order.getAddress().getAddressId());
        orderDTO.setPaymentDTO(modelMapper.map(savedOrder.getPayment(), PaymentDTO.class));

        return orderDTO;
    }
}
