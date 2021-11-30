package com.practice.onlineShop.entities;

import com.practice.onlineShop.enums.Currencies;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String code;
    private String description;
    private double price;
    private int stock;
    private boolean valid;
    @Enumerated(EnumType.STRING)
    private Currencies currency;
}
