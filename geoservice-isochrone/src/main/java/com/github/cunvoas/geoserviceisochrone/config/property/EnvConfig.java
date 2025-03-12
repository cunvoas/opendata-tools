package com.github.cunvoas.geoserviceisochrone.config.property;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

/**
 * setup environment file.
 */
//@Configuration
public class EnvConfig {

	/**
	 * override .env name.
	 * @return
	 */
	@Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new FileSystemResource("secret.env"));
        configurer.setIgnoreResourceNotFound(false);
        return configurer;
    }
}
