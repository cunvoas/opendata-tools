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

@Configuration
@EnableWebMvc
public class ThymeleafConfiguration  implements WebMvcConfigurer, ApplicationContextAware {

	private ApplicationContext applicationContext=null;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

    public ThymeleafConfiguration() {
        super();
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
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
     *  Message externalization/internationalization
     */
 	@Bean
 	@Description("WebMvc Message Resolver")
 	public ResourceBundleMessageSource messageSource() {
 		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
 		messageSource.setBasename("i18n/messages");
 		return messageSource;
 	}
 	

//	@Bean
//	public LocaleChangeInterceptor localeChangeInterceptor() {
//		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
//		localeChangeInterceptor.setParamName("lang");
//		return localeChangeInterceptor;
//	}



}
