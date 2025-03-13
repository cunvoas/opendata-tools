package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkNew;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.UploadFormValidator;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusPrefEnum;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkJardinService;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkTypeService;
import com.github.cunvoas.geoserviceisochrone.service.park.PhotoService;
import com.github.cunvoas.geoserviceisochrone.service.park.dto.PhotoDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Page controler for park (new and current impl).
 */
@Controller
@RequestMapping("/mvc/park/new")
@Slf4j
public class ParkNewControler {
	
	private static final DateFormat DF1 =new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat DF2 =new SimpleDateFormat("yyyy-MM-dd");
	private NumberFormat NF = new DecimalFormat("#.##O");
	
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	@Autowired
	private ParkJardinService serviceParkJardinService;
	
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Autowired
	private ServiceParcPrefecture serviceParcPrefecture;
	
	@Autowired
	private ParkTypeService parkTypeService;
	
	@Autowired
	private GeoJson2GeometryHelper geoJson2GeometryHelper;
	
    @Autowired
    private UploadFormValidator validatorUpload;
    
	@Autowired
	private PhotoService photoService;
	
	private String formName = "newPark";

	/**
	 * select region.
	 * @param form form
	 * @param model form
	 * @return page name populated
	 */
	@PostMapping("/region")
	public String changeRegion(@ModelAttribute FormParkNew form, Model model) {
		form.setIdCommunauteDeCommunes(null);
		form.setIdCommune(null);
		return getForm(form, model);
	}
	
	/**
	 * list com2co.
	 * @param id region
	 * @param txt search
	 * @return list
	 */
	@GetMapping("/comm2co")
	public List<CommunauteCommune> getCommunauteCommuneByRegion(@RequestParam("regionId") Long id, @RequestParam("txt") String txt){
		
		List<CommunauteCommune> comm2cos=null;
		if (id==null) {
			comm2cos= serviceReadReferences.getCommunauteCommune();
		} else {
			comm2cos= serviceReadReferences.getCommunauteByRegionId(id);
		}
		if (txt==null || txt.trim().length()==0) {
			return comm2cos;
		} else {
			return comm2cos.stream()
					.filter(comm2co -> comm2co.getName()
                    .toLowerCase()
                    .contains(txt.toLowerCase()))
					.limit(15)
					.collect(Collectors.toList());
		}
	}

	/**
	 * select com2co.
	 * @param form form
	 * @param model form
	 * @return page name populated
	 */
	@PostMapping("/commDeCo")
	public String changeCommunauteDeCommune(@ModelAttribute FormParkNew form, Model model) {
		form.setIdCommune(null);
		return getForm(form, model);
	}

	/**
	 * select city.
	 * @param form form
	 * @param model form
	 * @return page name populated
	 */
	@PostMapping("/city")
	public String changeCity(@ModelAttribute FormParkNew form, Model model) {
		Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
		form.setMapLng(String.valueOf(location.getX()));
		form.setMapLat(String.valueOf(location.getY()));
		return getForm(form, model);
	}
	
	
	/**
	 * get for check.
	 * @param idPark park
	 * @param form form
	 * @param model form
	 * @return page name populated
	 */
	@GetMapping("/check")
	public String checkPark(
			@RequestParam("idPark") Long idPark,
			@ModelAttribute FormParkNew form,
			Model model) {
		
		ParcEtJardin pj = serviceReadReferences.getParcEtJardinById(idPark);
		return getForm(form, model, pj);
	}
	
	/**
	 * mapper 
	 * @param form form
	 * @return BO
	 */
	protected ParcEtJardin map(final FormParkNew form) {
		ParcEtJardin pj=null;
		if (form.getIdPark()!=null) {
			pj = serviceReadReferences.getParcEtJardinById(form.getIdPark());
		} else {
			pj = new ParcEtJardin();
			pj.setSource(ParcSourceEnum.AUTMEL);
		}
		pj.setStatus(ParcStatusEnum.TO_QUALIFY);

		City commune = serviceReadReferences.getCity(form.getIdCommune());
		pj.setCommune(commune);
		
		pj.setName(form.getName());
		pj.setQuartier(form.getQuartier());
		pj.setType(form.getType());
		pj.setSousType(form.getSousType());
		
		pj.setSurface(form.getSurface());

		if (StringUtils.isNoneBlank(form.getDateDebut())) {
			try {
				pj.setDateDebut(DF2.parse(form.getDateDebut()));
			} catch (ParseException e) {
				try {
					pj.setDateDebut(DF1.parse(form.getDateDebut()));
				} catch (ParseException e1) {
				}
			}
		}
		
		if (StringUtils.isNoneBlank(form.getDateFin())) {
			try {
				pj.setDateFin(DF2.parse(form.getDateFin()));
			} catch (ParseException e) {
				try {
					pj.setDateFin(DF1.parse(form.getDateFin()));
				} catch (ParseException e1) {
				}
			}
		}

		// set parktype
		pj.setTypeId(form.getTypeId());
		pj.setOmsCustom(form.getOmsCustom());
		if (form.getOmsCustom()==null && form.getTypeId()!=null) {
			ParkType pt = parkTypeService.get(form.getTypeId());
			pj.setOmsCustom(pt.getOms());
		}
		
		
		String sGeom = form.getSGeometry();
		try {
			Geometry geom = null;
			
			if ( StringUtils.isNotBlank(sGeom) ) {
				log.warn("start process parseGeoman");
				geom = geoJson2GeometryHelper.parseGeoman(sGeom);

				if (geom!=null) {
					pj.setCoordonnee(geom.getCentroid());
					pj.setContour(geom);
				}
				log.warn("end process parseGeoman");
			}
			
		} catch (JsonProcessingException e) {
			log.error("geoman parsing error = ", sGeom);
		}	
		
		
		return pj;
	}
	
	
	/**
	 * save.
	 * @param form form
	 * @param model form
	 * @param bindingResult binding
	 * @return page name populated
	 */
	@PostMapping("/save")
	@Transactional //(isolation = Isolation.READ_COMMITTED) //(noRollbackFor = ExceptionGeo.class)
	public String save(@ModelAttribute FormParkNew form, Model model, BindingResult bindingResult) {
		log.warn("Generic save: {}", form);
		
		validatorUpload.validate(form, bindingResult);
		
		ParcEtJardin pj = this.map(form);
		
		pj=serviceParkJardinService.save(pj, form.getSGeometry()!=null);
		
		PhotoDto dto= this.getPhotoPath(form);
		dto.setParcEtJardin(pj);
		photoService.savePhoto(dto);
		
		// create poly
		if("new".equals(form.getEtatAction())) {
			log.warn("NEW: {}", form);
//			this.addPark(form, model);
			
		} else if("add".equals(form.getEtatAction())) {
			log.warn("ADD: {}", form);
//			this.addPark(form, model);
			
		} else if("edit".equals(form.getEtatAction())) {
			log.warn("EDIT: {}", form);
//			this.updPark(form, model);
			
		// change poly
		} else if("change".equals(form.getEtatAction())) {
			log.warn("CHANGE: {}", form);
//			this.updPark(form, model);
		
		// remove poly
		} else if("remove".equals(form.getEtatAction())) {
			log.warn("REMOVE: {}", form);
//			this.delPark(form, model);
			
		} else {
			//nothing todo
			log.warn("Nothing to DO: {}", form);
		}
		
		
		
		
		return getForm(form, model, pj);
	}
	
//	protected void addPark( FormParkNew form, Model model) {
//		//TODO
//		
//		 if("pref".equals(form.getEtat())) {
//			 
//			 
//		 } else if ("p&j".equals(form.getEtat())) {
//			 
//		 }
//		
//	}
//	
//	protected void updPark( FormParkNew form, Model model) {
//		//TODO
//		
//	}
//	
//	protected void delPark( FormParkNew form, Model model) {
//		//TODO
//		
//		 if("pref".equals(form.getEtat())) {
//			 Optional<ParcPrefecture> opt = serviceReadReferences.getParcPrefectureById( form.getId());
//			 if (opt.isPresent()) {
//				 ParcPrefecture pp = opt.get();
//				 // logical delete
//				 pp.setStatus(ParcStatusPrefEnum.CANCEL);
//				 serviceParcPrefecture.update(pp);
//			 }
//			 form.setId(null);
//			 form.setEtat(null);
//			 form.setEtatAction(null);
//			 
//			 
//		 } else if ("p&j".equals(form.getEtat())) {
//			 
//		 }
//		
//	}
	
	
	
	/**
	 * First call a.
	 * @param form
	 * @param model
	 * @return
	 */
	@GetMapping
	public String getForm(@ModelAttribute FormParkNew form, Model model) {

		form = populateListInForm(form);
		model.addAttribute(formName, form);
		model.addAttribute("regions", form.getRegions());
		model.addAttribute("communautesDeCommunes", form.getCommunautesDeCommunes());
		model.addAttribute("communes", form.getCommunes());
		model.addAttribute("parkTypes", parkTypeService.findAll());
		model.addAttribute("parkSources", serviceReadReferences.getParcSource());
		
		return formName;
	}
	
	/**
	 * populate form from business.
	 * @param form
	 * @param model
	 * @param pj
	 * @return
	 */
	private String getForm(@ModelAttribute FormParkNew form, Model model, ParcEtJardin pj) {

		form.setId(pj.getId());
		form.setIdPark(pj.getId());
		form.setIdRegion(pj.getCommune().getRegion().getId());
		if (pj.getCommune().getCommunauteCommune()!=null) {
			form.setIdCommunauteDeCommunes(pj.getCommune().getCommunauteCommune().getId());
		}
		form.setIdCommune(pj.getCommune().getId());
		
		if (pj.getCoordonnee()!=null) {
			form.setMapLng(String.valueOf(pj.getCoordonnee().getX()));
			form.setMapLat(String.valueOf(pj.getCoordonnee().getY()));
		} else {
			// no point, goto city center
			Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
			form.setMapLng(String.valueOf(location.getX()));
			form.setMapLat(String.valueOf(location.getY()));
		}
		
		form.setHadGeometry(pj.getContour()!=null);
		form.setName(pj.getName());
		form.setQuartier(pj.getQuartier());
		form.setType(pj.getType());
		form.setSousType(pj.getSousType());
		form.setSource(pj.getSource());
		form.setStatus(pj.getStatus().name());
		
		if (pj.getDateDebut()!=null) {
			form.setDateDebut(DF2.format(pj.getDateDebut()));
		}
		if (pj.getDateFin()!=null) {
			form.setDateFin(DF2.format(pj.getDateFin()));
		}
		form.setTypeId(pj.getTypeId());
		form.setOmsCustom(pj.getOmsCustom());
		
		
		
		
		form.setSurface(pj.getSurface());
		form.setSurfaceContour(pj.getSurfaceContour());	
		return getForm(form, model);
	}
	
	
	

	/**
	 *  form data filler.
	 * @param form
	 * @return
	 */
	protected FormParkNew populateListInForm( FormParkNew form) {
		if (form==null) {
			form = new FormParkNew();	
		}
		if (form.getIdRegion()==null) {
			form.autoLocate();
			
			Coordinate location = serviceReadReferences.getCoordinate(form.getIdCommune());
			if (location!=null) {
				form.setMapLng(String.valueOf(location.getX()));
				form.setMapLat(String.valueOf(location.getY()));
			}
		}
		
		// Populate Selection List
		form.setRegions(serviceReadReferences.getRegion());
		form.setCommunautesDeCommunes(serviceReadReferences.getCommunauteByRegionId(form.getIdRegion()));
		
		// if only one preselect it
		if (form.getCommunautesDeCommunes()!=null && form.getCommunautesDeCommunes().size()==1) {
			form.setIdCommunauteDeCommunes(form.getCommunautesDeCommunes().get(0).getId());
		}
		
		if (form.getIdRegion()!=null) {
			if (form.getIdCommunauteDeCommunes()!=null) {
				form.setCommunes(serviceReadReferences.getCityByCommunauteCommuneId(form.getIdCommunauteDeCommunes()));
			} else {
				form.setCommunes(serviceReadReferences.getCityByRegionId(form.getIdRegion()));
			}
		}
		
		if (form.getIdCommune()!=null) {
			City city = serviceReadReferences.getCityById(form.getIdCommune());
			form.setNameCommune(city!=null?city.getName():"");
			form.setCommune(city);
		}
		
		
		
		return form;
	}
		

	private PhotoDto getPhotoPath(FormParkNew form) {
		
		PhotoDto dto = new PhotoDto();
		dto.setPhoto(form.getFileupload());

		dto.setStoreRoot(applicationBusinessProperties.getPhotoPath());
		dto.setStoreRootOrigin(applicationBusinessProperties.getPhotoPathOrigin());
		
		
		City c = this.serviceReadReferences.getCityById(form.getIdCommune());
		dto.setCommuneId(form.getIdCommune());
		dto.setInseeCode(c.getInseeCode());
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(String.format("%s", form.getIdRegion()));
		
		if (form.getIdCommunauteDeCommunes()!=null) {
			sb.append("/c2c/");
			sb.append(String.format("%s", form.getIdCommunauteDeCommunes()));
		} else {
			String dpt = c.getInseeCode().substring(0, 2);
			sb.append("/dept/").append(dpt);
		}
		sb.append("/");
		sb.append(c.getInseeCode());
		dto.setStoreFolder(sb.toString());
		return dto;
	}

}
