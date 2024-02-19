package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.repo.ParkTypeRepository;

@Service
public class ParkTypeService {
	@Autowired
	private ParkTypeRepository parkTypeRepository;
	
	
	@Autowired
	private ResourceBundleMessageSource messageSource;
	
	public List<ParkType> findAll() {
		Locale locale = LocaleContextHolder.getLocale();
		
		List<ParkType> types = parkTypeRepository.findAll();
		for (ParkType parkType : types) {
			String trad = messageSource.getMessage(parkType.getI18n(), null, locale);
			parkType.setLabel(trad);
		}
		
		return types;
	}
	
	public void setLabel(ParkType parkType) {
		Locale locale = LocaleContextHolder.getLocale();
		String trad = messageSource.getMessage(parkType.getI18n(), null, locale);
		parkType.setLabel(trad);
	}

}
