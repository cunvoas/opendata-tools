package com.github.cunvoas.geoserviceisochrone.config.mvc;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for Thymeleaf.
 */
@Configuration
@EnableWebMvc
@Slf4j
public class ThymeleafConfiguration  implements WebMvcConfigurer, ApplicationContextAware {

	/**
	 * mandatory for ApplicationContextAware
	 */
	private ApplicationContext applicationContext=null;
	
	/**
	 * inject app ctx.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

    public ThymeleafConfiguration() {
        super();
    }

    /**
     * set handlers mapping.
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    	log.debug("ApplicationContextAware setup: {}", applicationContext!=null);
    	
        WebMvcConfigurer.super.addResourceHandlers(registry);
        registry.addResourceHandler("/pub/**").addResourceLocations("classpath:/public/");
        registry.addResourceHandler("/mvc/static/**").addResourceLocations("classpath:/static/mvc/");
        registry.addResourceHandler("*.ico").addResourceLocations("classpath:/static/ico/");
//        registry.addResourceHandler("/images/**").addResourceLocations("/images/");
//        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
//        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
        registry.addResourceHandler("/mvc/**").addResourceLocations("/mvc/");
    }
    
	 /**
     *  Message externalization/internationalization.
 	 * @return
 	 */
 	@Bean
 	@Description("WebMvc Message Resolver")
 	public ResourceBundleMessageSource messageSource() {
 		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
 		messageSource.setBasename("i18n/messages");
 		return messageSource;
 	}
 	

	/**
	 * Language change setup.
	 * @return
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		return localeChangeInterceptor;
	}



}
