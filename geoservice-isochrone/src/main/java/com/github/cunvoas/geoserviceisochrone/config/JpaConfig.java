package com.github.cunvoas.geoserviceisochrone.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Setup for JPA, Transation and Model.
 */
@Configuration
@EntityScan("com.github.cunvoas.geoserviceisochrone.model")
@EnableJpaRepositories(
	basePackages = { 
			"com.github.cunvoas.geoserviceisochrone.repo"
})
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
public class JpaConfig {


//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return errors;
//    }

}
