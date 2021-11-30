package com.practice.onlineShop.services;

import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.enums.Currencies;
import com.practice.onlineShop.exceptions.InvalidProductCodeException;
import com.practice.onlineShop.mappers.ProductMapper;
import com.practice.onlineShop.repositories.ProductRepository;
import com.practice.onlineShop.vos.ProductVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ProductServiceTest {

    @TestConfiguration
    static class ProductServiceTestContextConfiguration {

        @MockBean
        private ProductMapper productMapper;

        @MockBean
        private ProductRepository productRepository;

        @Bean
        public ProductService productService() {
            return new ProductService(productMapper, productRepository);
        }
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void addProduct() {

        Product product = new Product();
        product.setCurrency(Currencies.EUR);
        product.setPrice(11);
        product.setValid(true);
        product.setStock(1);
        product.setCode("aProductCode");

        when(productMapper.toEntity(any())).thenReturn(product);

        ProductVO productVO = new ProductVO();
        productVO.setValid(true);
        productVO.setDescription("A description");
        productVO.setStock(1);
        productVO.setPrice(11);
        productVO.setCurrency(Currencies.EUR);
        productVO.setId(1);
        productVO.setCode("aProductCode");

        Long customerId = 99L;
        productService.addProduct(productVO, customerId);

        verify(productMapper).toEntity(productVO);
        verify(productRepository).save(product);
    }

    @Test
    public void getProduct_whenProductIsNotInDb_shouldThrowAnException() {
        try {
            productService.getProduct("asd");
        } catch (InvalidProductCodeException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    public void getProduct_whenProductIsInDb_shouldReturnIt() throws InvalidProductCodeException {
        Product product = new Product();
        product.setCode("aCode");
        when(productRepository.findByCode(any())).thenReturn(Optional.of(product));
        ProductVO productVO = new ProductVO();
        productVO.setCode("aCode");
        when(productMapper.toVO(any())).thenReturn(productVO);

        ProductVO returnedProduct = productService.getProduct("aCode");

        assertThat(returnedProduct.getCode()).isEqualTo("aCode");

        verify(productRepository).findByCode("aCode");
        verify(productMapper).toVO(product);
    }

    @Test
    public void getProducts() {
        ArrayList<Product> products = new ArrayList<>();
        Product product1 = new Product();
        product1.setCode("aCode");
        products.add(product1);
        Product product2 = new Product();
        product2.setCode("aCode");
        products.add(product2);

        ProductVO productVO1 = new ProductVO();
        product1.setCode("aCode");
        ProductVO productVO2 = new ProductVO();
        productVO2.setCode("aCode2");

        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toVO(product1)).thenReturn(productVO1);
        when(productMapper.toVO(product2)).thenReturn(productVO2);

        List<ProductVO> productList = productService.getProducts();

        assertThat(productList).hasSize(2);
        assertThat(productList).containsOnly(productVO1, productVO2);

        verify(productRepository).findAll();
        verify(productMapper).toVO(product1);
        verify(productMapper).toVO(product2);
    }

    @Test
    public void updateProduct_whenProductCodeIsNull_shouldThrowAnException() {
        ProductVO productVO = new ProductVO();

        try {
            productService.updateProduct(productVO, 1L);
        } catch (InvalidProductCodeException e) {
            assert true;
            return;
        }

        assert false;
    }

    @Test
    public void updateProduct_whenProductCodeIsInvalid_ShouldThrowAnException() {
        ProductVO productVO = new ProductVO();
        productVO.setCode("asd");

        try {
            productService.updateProduct(productVO, 1L);
        } catch (InvalidProductCodeException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    public void updateProduct_whenProductCodeIsValid_shouldUpdateTheProducT() throws InvalidProductCodeException {
        ProductVO productVO = new ProductVO();
        productVO.setCode("a new Code");
        productVO.setDescription("a new Description");

        Product product = new Product();
        product.setCode("aCode");
        product.setDescription("an old Description");

        when(productRepository.findByCode(any())).thenReturn(Optional.of(product));

        productService.updateProduct(productVO, 1L);

        verify(productRepository).findByCode(productVO.getCode());

        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productArgumentCaptor.capture());

        Product productSendAsCapture = productArgumentCaptor.getValue();

        assertThat(productSendAsCapture.getDescription()).isEqualTo(productVO.getDescription());
    }

    @Test
    public void deleteProduct_whenCodeIsNull_shouldThrowAnException(){
        try {
            productService.deleteProduct(null, 1L);
        } catch (InvalidProductCodeException e) {
            assert true;
            return;
        }

        assert false;
    }

    @Test
    public void deleteProduct_whenCodeIsValid_shouldDeleteTheProduct() throws InvalidProductCodeException {
        Product product = new Product();
        product.setCode("aCode");
        when(productRepository.findByCode(any())).thenReturn(Optional.of(product));

        productService.deleteProduct("aCode", 1L);

        verify(productRepository).findByCode("aCode");
        verify(productRepository).delete(product);
    }
}

























