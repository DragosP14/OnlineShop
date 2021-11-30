package com.practice.onlineShop.controllers;

import com.practice.onlineShop.exceptions.InvalidProductCodeException;
import com.practice.onlineShop.services.ProductService;
import com.practice.onlineShop.vos.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/{customerId}")
    public void addProduct(@RequestBody ProductVO productVO, @PathVariable Long customerId) {
        productService.addProduct(productVO, customerId);
    }

    @GetMapping("/{productCode}")
    public ProductVO getProduct(@PathVariable String productCode) throws InvalidProductCodeException {
        return productService.getProduct(productCode);
    }

    @GetMapping
    public ProductVO[] getProducts() {
        return productService.getProducts().toArray(new ProductVO[]{});
    }

    @PutMapping("/{customerId}")
    public void updateProduct(@RequestBody ProductVO productVO, @PathVariable Long customerId) throws InvalidProductCodeException {
        productService.updateProduct(productVO, customerId);
    }

    @DeleteMapping("/{productCode}/{customerId}")
    public void deleteProduct(@PathVariable String productCode, @PathVariable Long customerId) throws InvalidProductCodeException {
        productService.deleteProduct(productCode, customerId);
    }

    @PatchMapping("/{productCode}/{quantity}/{customerId}")
    public void addStock(@PathVariable String productCode, @PathVariable Integer quantity, @PathVariable Long customerId) throws InvalidProductCodeException {
        productService.addStock(productCode, quantity, customerId);
    }
}























