package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CartDTO;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.Cart;
import com.ecommerce.web.model.CartItem;
import com.ecommerce.web.model.Product;
import com.ecommerce.web.repository.CartItemRepository;
import com.ecommerce.web.repository.CartRepository;
import com.ecommerce.web.repository.ProductRepository;
import com.ecommerce.web.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    private Double calculateDiscountedPrice(Product product) {
        return product.getProductPrice() - (product.getProductDiscount() * 0.01 * product.getProductPrice());
    }

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // find existing cart or create one
        Cart cart = findOrCreateCart();
        // retrieve product details

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        // perform validations for product
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());
        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already in cart", HttpStatus.BAD_REQUEST);
        }

        if (product.getProductQuantity() <= 0) {
            throw new APIException("Product " + product.getProductName() + " is out of stock", HttpStatus.BAD_REQUEST);
        }

        if (product.getProductQuantity() < quantity) {
            throw new APIException("Please make an order for the remaining quantity of " + product.getProductQuantity() + " " + product.getProductName());
        }

        // create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setCart(cart);
        newCartItem.setDiscount(product.getProductDiscount());
        newCartItem.setProductPrice(calculateDiscountedPrice(product));

        // save cart item
        cartItemRepository.save(newCartItem);

        // save updated cart
        cart.setTotalPrice(cart.getTotalPrice() + newCartItem.getProductPrice() * quantity);
        cart.getCartItems().add(newCartItem);

        cartRepository.save(cart);

        // convert cart to cartDTO and return
        return modelMapper.map(cart, CartDTO.class);
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        return carts.stream().map(cart -> modelMapper.map(cart, CartDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CartDTO getUserCart() {
        String userEmail = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(userEmail);

        if (cart == null) {
            throw new APIException("Cart not found for user with email: " + userEmail, HttpStatus.NOT_FOUND);
        }

        return modelMapper.map(cart, CartDTO.class);
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, int quantity) {
        Cart cart = findOrCreateCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getProductQuantity() == 0) {
            throw new APIException("Product " + product.getProductName() + " is out of stock", HttpStatus.BAD_REQUEST);
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());
        if (cartItem == null) {
            if (quantity > 0)
                return addProductToCart(productId, quantity);
            else throw new APIException("Cart cannot have negative product quantity", HttpStatus.BAD_REQUEST);
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if (quantity == -1 && newQuantity < 0) {
            throw new APIException("Cart cannot have negative product quantity", HttpStatus.BAD_REQUEST);
        }

        if (product.getProductQuantity() < newQuantity) {
            throw new APIException("No more " + product.getProductName() + " left in stock", HttpStatus.BAD_REQUEST);
        }

        double oldTotalPrice = cartItem.getProductPrice() * cartItem.getQuantity();
        cartItem.setQuantity(newQuantity);
        cartItem.setProductPrice(calculateDiscountedPrice(product));
        cartItem.setDiscount(product.getProductDiscount());

        double newTotalPrice = cartItem.getProductPrice() * newQuantity;
        cart.setTotalPrice(cart.getTotalPrice() - oldTotalPrice + newTotalPrice);

        if (newQuantity <= 0) {
            cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductPrice() * cartItem.getQuantity());
            cart.getCartItems().remove(cartItem);
            cartItemRepository.deleteCartItemByProductIdAndCartId(cart.getCartId(), productId);
        } else {
            cartItemRepository.save(cartItem);
        }

        Cart updatedCart = cartRepository.save(cart);

        return modelMapper.map(updatedCart, CartDTO.class);
    }

    @Transactional
    @Override
    public CartDTO removeProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).
                orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item", "id", cartId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductPrice() * cartItem.getQuantity());
        cart.getCartItems().remove(cartItem);
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        Cart updatedCart = cartRepository.save(cart);

        return modelMapper.map(updatedCart, CartDTO.class);
    }

    private Cart findOrCreateCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }
}
