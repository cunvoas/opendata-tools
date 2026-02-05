package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${application.feature-flipping.show-stacktrace-on-error:false}")
	private boolean showStacktraceOnError;
	
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
        String stackTrace = getStackTrace(throwable);
        
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("exception", throwable != null ? throwable.getClass().getName() : "Unknown");
        model.addAttribute("trace", stackTrace);
        model.addAttribute("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR);
        model.addAttribute("status", 500);
        model.addAttribute("showStacktrace", showStacktraceOnError);
        
        return "error";
    }
    
    /**
     * Convertit une exception en sa représentation sous forme de string (stack trace).
     * @param throwable l'exception
     * @return la stack trace en string
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}