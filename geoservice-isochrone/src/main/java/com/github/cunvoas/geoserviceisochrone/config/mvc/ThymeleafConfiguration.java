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
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

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
//	public SpringResourceTemplateResolver templateResolver(){
//        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
//        templateResolver.setApplicationContext(this.applicationContext);
//        templateResolver.setPrefix("templates/");
//        templateResolver.setSuffix(".html");
//        templateResolver.setTemplateMode(TemplateMode.HTML);
//        // Template cache is true by default. Set to false if you want
//        // templates to be automatically updated when modified.
//        templateResolver.setCacheable(true);
//        return templateResolver;
//    }
//	
//
//
//
//@Bean
//@Description("Thymeleaf Template Engine")
//public SpringTemplateEngine templateEngine(){
//    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//    templateEngine.setEnableSpringELCompiler(true); // Compiled SpringEL should speed up executions
//    templateEngine.setTemplateResolver(templateResolver());
//    templateEngine.setEnableSpringELCompiler(true);
//    templateEngine.setTemplateEngineMessageSource(messageSource());
////    templateEngine.addDialect(new SpringSecurityDialect());
//    return templateEngine;
//}
//	
//
//	
//	@Bean
//	@Description("Thymeleaf View Resolver")
//    public ThymeleafViewResolver viewResolver(){
//        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
//        viewResolver.setTemplateEngine(templateEngine());
//        return viewResolver;
//    }
//	


//	
//
//	@Bean
//	public LocaleChangeInterceptor localeChangeInterceptor() {
//		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
//		localeChangeInterceptor.setParamName("lang");
//		return localeChangeInterceptor;
//	}
//	



}
