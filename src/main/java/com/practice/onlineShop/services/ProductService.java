package com.practice.onlineShop.services;

import com.practice.onlineShop.entities.Product;
import com.practice.onlineShop.exceptions.InvalidProductCodeException;
import com.practice.onlineShop.mappers.ProductMapper;
import com.practice.onlineShop.repositories.ProductRepository;
import com.practice.onlineShop.vos.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    public void addProduct(ProductVO productVO, Long customerId) {
        System.out.println("Customer with id " + customerId + " is in service");
        Product product = productMapper.toEntity(productVO);
        productRepository.save(product);
    }

    public ProductVO getProduct(String productCode) throws InvalidProductCodeException {
        Product product = getProductEntity(productCode);

        return productMapper.toVO(product);
    }

    public List<ProductVO> getProducts(){
        List<ProductVO> products = new ArrayList<>();
        Iterable<Product> productsFromDbIterable = productRepository.findAll();
        Iterator<Product> iterator = productsFromDbIterable.iterator();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            ProductVO productVO = productMapper.toVO(product);
            products.add(productVO);
        }

        return products;
    }
    
    public void updateProduct(ProductVO productVO, Long customerId) throws InvalidProductCodeException {
        System.out.println("Customer with id " + customerId + " is in service for update");
        verifyProductCode(productVO.getCode());

        Product product = getProductEntity(productVO.getCode());
        product.setValid(productVO.isValid());
        product.setPrice(productVO.getPrice());
        product.setDescription(productVO.getDescription());
        product.setCurrency(productVO.getCurrency());
        product.setStock(productVO.getStock());

        productRepository.save(product);
    }

    public void deleteProduct(String productCode, Long customerId) throws InvalidProductCodeException {
        System.out.println("User with id: " + customerId + " is deleting " + productCode);
        verifyProductCode(productCode);

        Product product = getProductEntity(productCode);
        productRepository.delete(product);
    }

    @Transactional
    public void addStock(String productCode, Integer quantity, Long customerId) throws InvalidProductCodeException {
        System.out.println("User with id: " + customerId + " is adding stock for:" + productCode + ", "  +
                "number of items: " + quantity);
        verifyProductCode(productCode);
        Product product = getProductEntity(productCode);

        int oldStock = product.getStock();
        product.setStock(oldStock + quantity);
    }

    private Product getProductEntity(String productCode) throws InvalidProductCodeException {
        Optional<Product> productOptional = productRepository.findByCode(productCode);

        if (!productOptional.isPresent()) {
            throw new InvalidProductCodeException();
        }

        return productOptional.get();
    }

    private void verifyProductCode(String productCode) throws InvalidProductCodeException {
        if (productCode == null){
            throw new InvalidProductCodeException();
        }
    }
}
































