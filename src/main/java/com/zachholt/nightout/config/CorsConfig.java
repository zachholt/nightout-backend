package com.zachholt.nightout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8081",
            "http://localhost:19006",
            "http://localhost:19000",
            "http://localhost:19001",
            "http://localhost:19002",
            "http://192.168.6.20:8081",
            "http://192.168.6.20:19006",
            "http://192.168.6.20:19000",
            "http://192.168.6.20:19001",
            "http://192.168.6.20:19002",
            "exp://192.168.6.20:8081",
            "exp://192.168.6.20:19000",
            "exp://192.168.6.20:19001",
            "exp://192.168.6.20:19002",
            "exp://localhost:8081",
            "exp://localhost:19000",
            "exp://localhost:19001",
            "exp://localhost:19002",
            "http://44.203.161.109:8080",
            "http://44.203.161.109:19000",
            "http://44.203.161.109:19006",
            "exp://44.203.161.109:8080",
            "exp://44.203.161.109:19000"
        ));
        corsConfiguration.setAllowedHeaders(Arrays.asList(
            "Origin", 
            "Access-Control-Allow-Origin", 
            "Content-Type", 
            "Accept", 
            "Authorization", 
            "Origin, Accept", 
            "X-Requested-With", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Origin", 
            "Content-Type", 
            "Accept", 
            "Authorization", 
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials"
        ));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
} 