package com.github.cunvoas.geoserviceisochrone.controller.mvc.validator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkNew;

/**
 * Form validator to uploads.
 */
@Component
public class UploadFormValidator implements Validator{

	@Autowired
	private ResourceBundleMessageSource messageSource;
    
	@Override
	public boolean supports(Class<?> clazz) {
        return clazz.getName().matches(".FormParkNew");
	}

	@Override
	public void validate(Object target, Errors errors) {

        
		FormParkNew form = (FormParkNew) target;
        
        
//        ValidationUtils.rejectIfEmpty(errors, "file", "errors.file1",
//                new Object[] { "nom" },
//                getMessage("valid.application.upload.file.empty", new Object[] {}));
        
        if (form.getFileupload()!=null) {
            MultipartFile file = form.getFileupload();
            
            String fileName = form.getFileupload().getName();
            int dernierPoint = fileName.lastIndexOf('.');
            String extensionFichier = fileName.substring(dernierPoint + 1, fileName.length());
            extensionFichier = extensionFichier.toLowerCase();
            
            String contentType= file.getContentType();
            String check= MIME.get(contentType);
            
            if ( !(check!=null && check.equals(extensionFichier)) ) {

        		Locale locale = LocaleContextHolder.getLocale();
        		
                errors.rejectValue(
                        "fileupload",
                        "errors.file2",
                        new Object[] { "fileupload" },
                        messageSource.getMessage("upload.mismatch",
                                new Object[] {}, locale));
            }
        }
		
	}
	
	private static Map<String, String> MIME = new HashMap<>();
	static {
		MIME.put("image/jpeg", "jpg");
		MIME.put("image/png", "png");
		MIME.put("image/gif", "gif");
		MIME.put("image/webp", "webp");
	}

}
