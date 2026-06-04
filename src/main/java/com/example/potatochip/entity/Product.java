package com.example.potatochip.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private int stock;

    public Long getId() {return id; }
    public String getName() {return name; }
    public int getPrice() {return price; }
    public int getStock() {return stock; }

}
