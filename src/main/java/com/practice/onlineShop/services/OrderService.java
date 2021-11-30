package com.practice.onlineShop.services;

import com.practice.onlineShop.entities.Orders;
import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.exceptions.*;
import com.practice.onlineShop.mappers.OrderMapper;
import com.practice.onlineShop.repositories.OrderRepository;
import com.practice.onlineShop.vos.OrderVO;
import lombok.RequiredArgsConstructor;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StockService stockService;

    public void addOrder (OrderVO orderVO) throws InvalidCustomerIdException, InvalidProductsException, InvalidProductIdException, NotEnoughStockException {
        validateStock(orderVO);

        Orders order = orderMapper.toEntity(orderVO);
        order.getOrderItems().forEach(orderItem -> {
            int oldProductStock = orderItem.getProduct().getStock();
            int productId = (int) orderItem.getProduct().getId();
            int sellStock = orderVO.getProductsIdsToQuantity().get(productId);
            orderItem.getProduct().setStock(oldProductStock - sellStock);
        });

        orderRepository.save(order);
    }

    @Transactional
    public void deliver(Integer orderId, Long customerId) throws InvalidOrderIdException, OrderCanceledException {
        System.out.println("Custmer-ul cu id-ul: " + customerId + " este in service");
        ThrowExceptionIfOrderIdIsAbsent(orderId);

        Orders order = getOrderOrThrowException(orderId);
        if (order.isCanceled()) {
            throw new OrderCanceledException();
        }

        order.setDelivered(true);
    }

    @Transactional
    public void cancelOrder(Integer orderId, Long customerId) throws InvalidOrderIdException, OrderAlreadyDeliveredException, InvalidOperationException {
        System.out.println("Customer-ul cu id-ul: " + customerId + " este in service pentru a anula comanda " + orderId);

        ThrowExceptionIfOrderIdIsAbsent(orderId);
        Orders order = getOrderOrThrowException(orderId);
        if (order.isDelivered()) {
            throw new OrderAlreadyDeliveredException();
        }
        // implementarea mea
        if (customerId != order.getUser().getId()) {
            throw new InvalidOperationException();
        }
        order.setCanceled(true);
    }

    @Transactional
    public void returnOrder(Integer orderId, Long customerId) throws InvalidOrderIdException, OrderNotDeliveredYetException, OrderCanceledException {
        System.out.println("Customer-ul cu id-ul: " + customerId + " este in service pentru a returna comanda " + orderId);

        ThrowExceptionIfOrderIdIsAbsent(orderId);
        Orders order = getOrderOrThrowException(orderId);

        if (!order.isDelivered()) {
            throw new OrderNotDeliveredYetException();
        }
        if (order.isCanceled()){
            throw new OrderCanceledException();
        }

        order.setReturned(true);
        order.getOrderItems().forEach(orderItem -> {
           Product product = orderItem.getProduct();
           int oldStock = product.getStock();
           product.setStock(oldStock + orderItem.getQuantity());
        });
    }

    private void ThrowExceptionIfOrderIdIsAbsent(Integer orderId) throws InvalidOrderIdException {
        if (orderId == null) {
            throw new InvalidOrderIdException();
        }
    }

    private Orders getOrderOrThrowException(Integer orderId) throws InvalidOrderIdException {
        Optional<Orders> orderOptional = orderRepository.findById(orderId.longValue());
        if (!orderOptional.isPresent()) {
            throw new InvalidOrderIdException();
        }
        return orderOptional.get();
    }

    private void validateStock(OrderVO orderVO) throws NotEnoughStockException {
        Map<Integer, Integer> productsIdsToQuantityMap = orderVO.getProductsIdsToQuantity();
        Set<Integer> productsIds = productsIdsToQuantityMap.keySet();
        for (Integer productId : productsIds){
            Integer quantity = productsIdsToQuantityMap.get(productId);
            boolean havingEnoughStock = stockService.isHavingEnoughStock(productId, quantity);
            if (!havingEnoughStock) {
                throw new NotEnoughStockException();
            }
        }
    }
}















