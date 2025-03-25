package com.zachholt.nightout.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Configuration class to set default profile if none is explicitly set.
 */
@Configuration
public class DefaultProfileConfig {
    private static final Logger logger = Logger.getLogger(DefaultProfileConfig.class.getName());
    
    private final Environment env;
    
    public DefaultProfileConfig(Environment env) {
        this.env = env;
    }
    
    @PostConstruct
    public void init() {
        String[] activeProfiles = env.getActiveProfiles();
        
        if (activeProfiles.length == 0) {
            logger.info("No active profile set, setting default profile to 'prod'");
            System.setProperty("spring.profiles.active", "prod");
        } else {
            logger.info("Active profiles: " + Arrays.toString(activeProfiles));
        }
    }
} 