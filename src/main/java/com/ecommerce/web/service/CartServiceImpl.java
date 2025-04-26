package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CartDTO;
import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.Cart;
import com.ecommerce.web.model.CartItem;
import com.ecommerce.web.model.Product;
import com.ecommerce.web.repository.CartItemRepository;
import com.ecommerce.web.repository.CartRepository;
import com.ecommerce.web.repository.ProductRepository;
import com.ecommerce.web.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setProductQuantity(item.getQuantity());
            productDTO.setProductDiscountedPrice(calculateDiscountedPrice(item.getProduct()));
            return productDTO;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        return carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream().
                    map(item -> {
                        item.getProduct().setProductQuantity(item.getQuantity());
                        ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                        productDTO.setProductDiscountedPrice(calculateDiscountedPrice(item.getProduct()));
                        return productDTO;
                    }).
                    collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public CartDTO getUserCart() {
        String userEmail = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(userEmail);

        if (cart == null) {
            throw new APIException("Cart not found for user with email: " + userEmail, HttpStatus.NOT_FOUND);
        }

        cart.getCartItems().forEach(item -> item.getProduct().setProductQuantity(item.getQuantity()));

        return modelMapper.map(cart, CartDTO.class);
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
