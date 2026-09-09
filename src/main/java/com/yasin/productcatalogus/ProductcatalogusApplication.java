package com.yasin.productcatalogus;

import com.yasin.productcatalogus.utilities.EnvLoaderUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductcatalogusApplication {

    public static void main(String[] args) {
        EnvLoaderUtility envLoaderUtility = new EnvLoaderUtility();
        envLoaderUtility.loadIntoSystemProperties();
        SpringApplication.run(ProductcatalogusApplication.class, args);
    }

}
