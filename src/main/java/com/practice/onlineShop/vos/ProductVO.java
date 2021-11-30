package com.practice.onlineShop.vos;

import com.practice.onlineShop.enums.Currencies;
import lombok.Data;

@Data
public class ProductVO {
    private long id;
    private String code;
    private String description;
    private double price;
    private int stock;
    private boolean valid;
    private Currencies currency;
}
