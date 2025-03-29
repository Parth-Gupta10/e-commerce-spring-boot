package com.ecommerce.web.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean(name = "model-mapper")
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}
