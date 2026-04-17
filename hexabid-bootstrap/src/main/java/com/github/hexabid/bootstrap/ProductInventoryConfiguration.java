package com.github.hexabid.bootstrap;

import com.github.hexabid.inventory.InventoryFacade;
import com.github.hexabid.product.ProductFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ProductInventoryConfiguration {

    @Bean
    ProductFacade productFacade() {
        return new ProductFacade();
    }

    @Bean
    InventoryFacade inventoryFacade() {
        return new InventoryFacade();
    }
}