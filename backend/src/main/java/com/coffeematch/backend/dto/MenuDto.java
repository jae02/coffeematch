package com.coffeematch.backend.dto;

import com.coffeematch.backend.entity.Menu;

public class MenuDto {
    private Long id;
    private String itemName;
    private int price;
    private boolean isRecommended;

    public MenuDto(Menu menu) {
        this.id = menu.getId();
        this.itemName = menu.getItemName();
        this.price = menu.getPrice();
        this.isRecommended = menu.isRecommended();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public int getPrice() {
        return price;
    }

    public boolean isRecommended() {
        return isRecommended;
    }
}
