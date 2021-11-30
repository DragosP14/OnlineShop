package com.practice.onlineShop.controllers;

import com.practice.onlineShop.exceptions.*;
import com.practice.onlineShop.services.OrderService;
import com.practice.onlineShop.vos.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public void addOrder (@RequestBody OrderVO orderVO) throws InvalidProductsException, InvalidCustomerIdException, InvalidProductIdException, NotEnoughStockException {
        orderService.addOrder(orderVO);
    }

    @PatchMapping("/{orderId}/{customerId}")
    public void deliver(@PathVariable Integer orderId, @PathVariable Long customerId) throws InvalidOrderIdException, OrderCanceledException {
        orderService.deliver(orderId, customerId);
    }

    @PatchMapping("/cancel/{orderId}/{customerId}")
    public void cancelOrder(@PathVariable Integer orderId, @PathVariable Long customerId) throws OrderAlreadyDeliveredException, InvalidOrderIdException, InvalidOperationException {
        orderService.cancelOrder(orderId, customerId);
    }

    @PatchMapping("/return/{orderId}/{customerId}")
    public void returnOrder(@PathVariable Integer orderId, @PathVariable Long customerId) throws InvalidOrderIdException, OrderNotDeliveredYetException, OrderCanceledException {
        orderService.returnOrder(orderId, customerId);
    }
}













































