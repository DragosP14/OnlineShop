package com.practice.onlineShop.repositories;

import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.enums.Currencies;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@DataJpaTest
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    public void findByCode_whenCodeIsPresentInDb_shouldReturnTheProduct () {
        Product product = new Product();
        product.setCode("aProductCode");
        product.setPrice(100);
        product.setStock(1);
        product.setValid(true);
        product.setCurrency(Currencies.USD);
        product.setDescription("a bad product");

        Product product2 = new Product();
        product.setCode("aProductCode2");
        product.setPrice(100);
        product.setStock(1);
        product.setValid(true);
        product.setCurrency(Currencies.USD);
        product.setDescription("a bad product");

        testEntityManager.persist(product);
        testEntityManager.persist(product2);
        testEntityManager.flush();

        Optional<Product> productFromDb = productRepository.findByCode(product.getCode());

        assertThat(productFromDb).isPresent();
        assertThat(productFromDb.get().getCode()).isEqualTo(product.getCode());
    }

    @Test
    public void findByCode_whenCodeIsNotPresentInDb_shouldReturnEmpty(){
        Optional<Product> productFromDb = productRepository.findByCode("asd");

        assertThat(productFromDb).isNotPresent();
    }
}