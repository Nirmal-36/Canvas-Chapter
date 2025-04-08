package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    // ✅ 1. Place an Order
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Order order) {
        double total = 0;
        for (String productId : order.getProductIds()) {
            Optional<Product> optionalProduct = productRepository.findById(productId);
            if (optionalProduct.isEmpty()) {
                return ResponseEntity.badRequest().body("Product not found: " + productId);
            }
            Product product = optionalProduct.get();
            if (product.getStock() <= 0) {
                return ResponseEntity.badRequest().body("Out of stock: " + product.getName());
            }
            total += product.getPrice();
            product.setStock(product.getStock() - 1);
            productRepository.save(product);
        }
        order.setTotalPrice(total);
        order.setOrderDate(new Date());
        return ResponseEntity.ok(orderRepository.save(order));
    }

    // ✅ 2. Get All Orders (Admin/Global)
    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ 3. Get Orders by User
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable String userId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
