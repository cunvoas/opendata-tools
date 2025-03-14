package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.repo.ParkTypeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

/**
 * Business Service impl.
 */
@Service
public class ParkTypeService {
	@Autowired
	private ParkTypeRepository parkTypeRepository;

	@Autowired
	private ParkJardinRepository parkJardinRepository;
	
	@Autowired
	private ResourceBundleMessageSource messageSource;
	
	/**
	 * @return list ParkType
	 */
	public List<ParkType> findAll() {
		
		List<ParkType> types = parkTypeRepository.findAll();
		for (ParkType parkType : types) {
			this.setLabel(parkType);
		}
		
		return types;
	}
	
	/**
	 * setLabel.
	 * @param parkType ParkType
	 */
	public void setLabel(ParkType parkType) {
		Locale locale = LocaleContextHolder.getLocale();
		String trad = "";
		Boolean strict = Boolean.TRUE;
		if (parkType.getI18n()!=null) {
			trad = messageSource.getMessage(parkType.getI18n(), null, locale);
			strict = Boolean.valueOf(messageSource.getMessage(parkType.getI18n()+".oms.strict", null, locale));
		}
		parkType.setLabel(trad);
		parkType.setStrict(strict);
	}
	
	/**
	 * populate.
	 * @param parkAreas list
	 */
	public void populate(List<ParkArea> parkAreas) {
		if (!CollectionUtils.isEmpty(parkAreas)) {
			for (ParkArea parkArea : parkAreas) {
				this.populate(parkArea);
			}
		}
	}
	
	/**
	 * get.
	 * @param parkTypeId id
	 * @return ParkType
	 */
	public ParkType get(Long parkTypeId) {
		ParkType pt = null;
		Optional<ParkType> o = parkTypeRepository.findById(parkTypeId);
		if (o.isPresent()) {
			pt = o.get();
			setLabel(pt);
		}
		return pt;
	}
	
	
	/**
	 * populate.
	 * @param parkArea ParkArea
	 */
	public void populate(ParkArea parkArea) {
		Locale locale = LocaleContextHolder.getLocale();
		ParkType parkType = parkArea.getType();
		String trad = messageSource.getMessage(parkType.getI18n(), null, locale);
		parkType.setLabel(trad);
		
		Boolean oms = Boolean.TRUE;
		Boolean strict = Boolean.TRUE;
		if (parkType.getOms()==null) {
			oms = Boolean.valueOf(messageSource.getMessage(parkType.getI18n()+".oms", null, locale));
			parkType.setOms(oms);
		}
		if (parkType.getStrict()==null) {
			strict = Boolean.valueOf(messageSource.getMessage(parkType.getI18n()+".oms.strict", null, locale));
			parkType.setStrict(strict);
		}
	}
	

}
