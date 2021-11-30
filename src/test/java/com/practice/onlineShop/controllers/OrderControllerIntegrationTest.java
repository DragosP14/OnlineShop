package com.practice.onlineShop.controllers;

import com.practice.onlineShop.entities.OrderItem;
import com.practice.onlineShop.entities.Orders;
import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.entities.User;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.repositories.OrderRepository;
import com.practice.onlineShop.utils.UtilsComponent;
import com.practice.onlineShop.vos.OrderVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static com.practice.onlineShop.utils.UtilsComponent.LOCALHOST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @TestConfiguration
    static class ProductControllerIntegrationTestContextConfiguration{
        @Bean
        public RestTemplate restTemplateForPatch(){
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate restTemplateForPatch;

    @Autowired
    private UtilsComponent utilsComponent;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
    public void addOrder_whenOrderIsValid_shouldAddItToDB(){
        User user = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1", "code2");

        OrderVO orderVO = createOrderVo(user, product);

        testRestTemplate.postForEntity(LOCALHOST + port + "/order", orderVO, Void.class);

        List<Orders> ordersIterable = (List<Orders>) orderRepository.findAll();

        // order1 -> orderItems1 -> 1,2,3
        // order2 -> orderItems2 -> 3,4
        // List (orderItems1), List(orderItems2) -> List (orderItems1, orderItems2)
        // filter by product
        Optional<OrderItem> orderItemOptional = ordersIterable.stream()
                .map(order -> ((List<OrderItem>) order.getOrderItems()))
                .flatMap(List::stream)
                .filter(orderItem -> orderItem.getProduct().getId() == product.getId())
                .findFirst();

        assertThat(orderItemOptional).isPresent();
    }

    @Test
    public void addOrder_whenRequestIsMadeByAdmin_shouldThrowAnException(){
        User user = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForAdmin", "code2ForAdmin");

        OrderVO orderVO = createOrderVo(user, product);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(LOCALHOST + port + "/order", orderVO, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a executa aceasta operatiune!");
    }
    @Test
    public void addOrder_whenRequestIsMadeByExpeditor_shouldThrowAnException(){
        User user = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditor", "code2ForExpeditor");

        OrderVO orderVO = createOrderVo(user, product);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(LOCALHOST + port + "/order", orderVO, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getBody()).isEqualTo("Utilizatorul nu are permisiunea de a executa aceasta operatiune!");
    }

    @Test
    public void deliver_whenHavingAnOrderWhichIsNotCanceled_shouldDeliverItByExpeditor(){
        User expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForDeliver", "code2ForExpeditorForDeliver");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);

        orderRepository.save(orderWithProducts);

        restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        Orders orderFromDb = orderRepository.findById(orderWithProducts.getId()).get();

        assertThat(orderFromDb.isDelivered()).isTrue();
    }

    @Test
    public void deliver_whenHavingAnOrderWhichIsNotCanceled_shouldNOTDeliverItByAdmin(){
        User adminAsExpeditor = utilsComponent.saveUserWithRole(Roles.ADMIN);
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForDeliverWhenAdmin", "code2ForExpeditorForDeliverWhenAdmin");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);

        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + adminAsExpeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void deliver_whenHavingAnOrderWhichIsNotCanceled_shouldNOTDeliverByClient(){
        User clientAsExpeditor = utilsComponent.saveUserWithRole(Roles.CLIENT);
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForDeliverWhenClient", "code2ForExpeditorForDeliverWhenClient");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);

        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + clientAsExpeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void deliver_whenHavingAnOrderWhichIsCanceled_shouldThrowAnException(){
        User expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("code1ForExpeditorForCanceledOrder", "code2ForExpeditorForCanceledOrder2");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderWithProducts.setCanceled(true);
        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, String.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Comanda a fost anulata!]");
        }
    }

    @Test
    public void cancel_whenValidOrder_shouldCancelIt(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForCanceledOrder1ForClient", "productForCanceledOrder2ForClient");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client);
        orderWithProducts.setCanceled(true);
        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + client.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }

        Orders orderFromdb = orderRepository.findById(orderWithProducts.getId()).get();
        assertThat(orderFromdb.isCanceled()).isTrue();

    }

    @Test
    public void cancel_whenValidOrderIsCanceledByAnotherUser_shouldThrowAnException(){
        User client1 = utilsComponent.saveUserWithRole(Roles.CLIENT);
        User client2 = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForCanceledOrder1ForAnotherClient", "productForCanceledOrder2ForAnotherClient");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, client1);
        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + client2.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void cancel_whenOrderIsAlreadySent_shouldThrowAnException(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForCanceledSentOrder1", "productForCanceledSentOrder2");

        Orders orderWithProducts = utilsComponent.saveDeliveredOrder(client, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + client.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Comanda a fost deja expediata!]");
        }
    }

    @Test
    public void cancel_whenUserIsAdmin_shouldThrowAnException(){
        User admin = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForCanceledOrder1ForAdmin", "productForCanceledOrder2ForAdmin");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, admin);
        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/cancel/" + orderWithProducts.getId() + "/" + admin.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void cancel_whenUserIsAnExpeditor_shouldThrowAnException(){
        User expeditor = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForCanceledOrder1ForExpeditor", "productForCanceledOrder2ForExpeditor");

        Orders orderWithProducts = utilsComponent.generateOrderItems(product, expeditor);
        orderRepository.save(orderWithProducts);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/" + orderWithProducts.getId() + "/" + expeditor.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    @Transactional
    public void return_whenOrderValid_shouldReturnIt(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForReturn1", "productForReturn2");
        Orders orderWithProducts = utilsComponent.saveDeliveredOrder(client, product);

        restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        Orders orderFromDb = orderRepository.findById(orderWithProducts.getId()).get();

        assertThat(orderFromDb.isReturned()).isTrue();
        assertThat(orderFromDb.getOrderItems().get(0).getProduct().getStock()).isEqualTo(product.getStock() + orderWithProducts.getOrderItems().get(0).getQuantity());
    }

    @Test
    public void return_whenOrderIsNotDelivered_shouldThrowException(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForReturn1ForOrderNotDelivered", "productForReturn2ForOrderNotDelivered");
        Orders orderWithProducts = utilsComponent.saveOrder(client, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Comanda nu poate fi returnata deoarece nu a fost livrata!]");
        }
    }

    @Test
    public void return_whenOrderIsCanceled_shouldThrowException(){
        User client = utilsComponent.saveUserWithRole(Roles.CLIENT);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForReturn1ForCanceledOrder", "productForReturn2ForCanceledOrder");
        Orders orderWithProducts = utilsComponent.saveCanceledAndDeliveredOrder(client, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + client.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Comanda a fost anulata!]");
        }
    }

    @Test
    public void return_whenUserIsAdmin_shouldThrowException(){
        User adminAsClient = utilsComponent.saveUserWithRole(Roles.ADMIN);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForReturn1ForAdmin", "productForReturn2ForAdmin");
        Orders orderWithProducts = utilsComponent.saveDeliveredOrder(adminAsClient, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + adminAsClient.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    @Test
    public void return_whenUserIsExpeditor_shouldThrowException(){
        User expeditorAsClient = utilsComponent.saveUserWithRole(Roles.EXPEDITOR);
        Product product = utilsComponent.storeTwoProductsInDatabase("productForReturn1ForExpeditor", "productForReturn2ForExpeditor");
        Orders orderWithProducts = utilsComponent.saveDeliveredOrder(expeditorAsClient, product);

        try {
            restTemplateForPatch.exchange(LOCALHOST + port + "/order/return/" + orderWithProducts.getId() + "/" + expeditorAsClient.getId(),
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);

        } catch (RestClientException exception) {
            assertThat(exception.getMessage()).isEqualTo("400 : [Utilizatorul nu are permisiunea de a executa aceasta operatiune!]");
        }
    }

    private OrderVO createOrderVo(User user, Product product) {
        OrderVO orderVO = new OrderVO();
        orderVO.setUserId((int) user.getId());
        Map<Integer, Integer> orderMap = new HashMap<>();
        orderMap.put((int) product.getId(), 1);
        orderVO.setProductsIdsToQuantity(orderMap);
        return orderVO;
    }
}


























