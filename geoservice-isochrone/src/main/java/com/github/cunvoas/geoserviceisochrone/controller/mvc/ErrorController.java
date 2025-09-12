package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur de gestion des erreurs (advice).
 * <p>
 * Cette classe permet d'intercepter les exceptions non gérées dans l'application MVC
 * et d'afficher une page d'erreur personnalisée avec le message d'erreur et le statut HTTP.
 * </p>
 */
//@ControllerAdvice
@Slf4j
public class ErrorController  extends ResponseEntityExceptionHandler{
	
//	@Override
//	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
//		Map<String, String> errors = new HashMap<>();
//		ex.getBindingResult().getAllErrors().forEach((error) ->{
//			
//			String fieldName = ((FieldError) error).getField();
//			String message = error.getDefaultMessage();
//			errors.put(fieldName, message);
//		});
//		return new ResponseEntity<Object>(errors, HttpStatus.BAD_REQUEST);
//	}



	/**
	 * Gestionnaire d'exception générique.
	 * <p>
	 * Capture toute exception non gérée et affiche la page d'erreur avec le message associé.
	 * </p>
	 * @param throwable l'exception levée
	 * @param model le modèle pour la vue
	 * @return la vue "error"
	 */
	@ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String exception(final Throwable throwable, final Model model) {
        log.error("Exception during execution of SpringSecurity application", throwable);
        String errorMessage = (throwable != null ? throwable.getMessage() : "Unknown error");
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR);
        return "error";
    }

}