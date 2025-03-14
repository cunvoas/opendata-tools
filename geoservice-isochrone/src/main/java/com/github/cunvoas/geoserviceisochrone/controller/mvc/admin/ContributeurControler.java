package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormContributor;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;
import com.github.cunvoas.geoserviceisochrone.service.admin.AssociationService;
import com.github.cunvoas.geoserviceisochrone.service.admin.ContributeurService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Page controler for contributors.
 */
@Controller
@RequestMapping("/mvc/management/contrib")
@Slf4j
public class ContributeurControler {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ContributeurService contributeurService;
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private AssociationService associationService;

	private String formName = "editContributeur";
	private String listName = "listContributeur";
	
	
	
//	private static final Long ID_AUTMEL=1L;

	/**
	 * call list page.
	 * @param model form
	 * @return page name
	 */
	@GetMapping("/list")
	public String getList(Model model) {
		Contributeur contrib =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		List<Contributeur> contribs = null;
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			contribs = contributeurService.findAll();
		} else {
			contribs = contributeurService.findByAssociation(contrib.getAssociation());
		}
		model.addAttribute(listName, contribs);
		return listName;
	}
	
	
	/**
	 * call add page.
	 * @param model forme
	 * @return page name
	 */
	@GetMapping("/add")
	public String getForm(Model model) {

		model.addAttribute(formName, new FormContributor());
		model.addAttribute("regions", regionRepository.findAllOrderByName());
		model.addAttribute("assos", associationService.findAll());
		return formName;
	}
	

	/**
	 * edit other profil.
	 * @param model form
	 * @return page name
	 */
	@GetMapping("/edit")
	public String getForm(@RequestParam("id") Long id, Model model) {
		
		Contributeur contrib = contributeurService.get(id);
		model.addAttribute(formName, cloneToForm(contrib, model));
		

		Contributeur contribConnected =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (ContributeurRole.ADMINISTRATOR.equals(contribConnected.getRole()))  {
			model.addAttribute("assos", associationService.findAll());
			
		} else {
			List<Association> assos = new ArrayList<>(1);
			assos.add(contrib.getAssociation());
			model.addAttribute( "assos", assos );
		}
		
		return formName;
	}

	/**
	 * edit my profil.
	 * @param model form
	 * @return page name
	 */
	@GetMapping
	public String getMyForm(Model model) {
		
		Contributeur contribConnected =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		Contributeur contrib = contributeurService.get(contribConnected.getId());
		model.addAttribute(formName, cloneToForm(contrib, model));
		
		List<Association> assos = new ArrayList<>(1);
		assos.add(contrib.getAssociation());
		model.addAttribute( "assos", assos );
		
		return formName;
	}

	
	
	/**
	 * reset passwd.
	 * @param model form
	 * @return page name
	 */
	@GetMapping("/resetMyPassword")
	public String resetMyPassword(Model model) {
		Contributeur contribConnected =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Contributeur contrib = contributeurService.get(contribConnected.getId());
		contrib = contributeurService.save(contrib,true);
		
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole()))  {
			model.addAttribute("assos", associationService.findAll());
		} else {
			List<Association> assos = new ArrayList<>(1);
			assos.add(contrib.getAssociation());
			model.addAttribute( "assos", assos );
		}
		model.addAttribute(formName, cloneToForm(contrib, model));
		return formName;
	}
	
	/**
	 * reset passwd.
	 * @param id contrib
	 * @param model form
	 * @return page name
	 */
	@GetMapping("/resetPassword")
	public String resetUserPassword(@RequestParam("id") Long id, Model model) {
		
		Contributeur contrib = contributeurService.get(id);
		
		contrib = contributeurService.save(contrib,true);
		
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole()))  {
			model.addAttribute("assos", associationService.findAll());
		} else {
			List<Association> assos = new ArrayList<>(1);
			assos.add(contrib.getAssociation());
			model.addAttribute( "assos", assos );
		}
		model.addAttribute(formName, cloneToForm(contrib, model));
		return formName;
	}
	
	/**
	 * save contrib.
	 * @param fContrib BO
	 * @param bindingResult binding
	 * @param model form
	 * @return page name
	 */
	@PostMapping("/edit")
	public String save(@Valid @ModelAttribute(value = "editContributeur") FormContributor fContrib, 
			final BindingResult bindingResult, final Model model)   {
		
		Contributeur contribConnected =  (Contributeur)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (ContributeurRole.ADMINISTRATOR.equals(contribConnected.getRole()))  {
			model.addAttribute("assos", associationService.findAll());
		} else {
			Association asso = associationService.findById(contribConnected.getAssociation().getId());
			model.addAttribute( "assos", (new ArrayList<Association>(1)).add(asso) );
		}
		
		if (bindingResult.hasErrors()) {
			fContrib.setPassword(null);
			model.addAttribute(formName, fContrib);
			return formName;
		}
				
		Contributeur contrib=mapFromForm(fContrib);
		try {
			if (contribConnected.equals(contrib)) {
				contribConnected.setIdRegion(contrib.getIdRegion());
				contribConnected.setIdCommunauteCommune(contrib.getIdCommunauteCommune());
				contribConnected.setIdCommune(contrib.getIdCommune());
			}
			contrib = contributeurService.save(contrib,false);
		} catch (ExceptionAdmin e) {
			log.error("sve contrib", e);
			model.addAttribute("errorMsg_"+e.getMessage(), messageSource.getMessage(e.getMessage(), null, Locale.FRANCE));
			model.addAttribute("errorMessage", messageSource.getMessage(e.getMessage(), null, Locale.FRANCE));

			return formName;
		}
		
		model.addAttribute(formName, cloneToForm(contrib, model));


		
		return formName;
	}
	
	/**
	 * mapper.
	 * @param in BO
	 * @param model form
	 * @return form
	 */
	private FormContributor cloneToForm(Contributeur in, Model model) {
		FormContributor clone = new FormContributor();
		clone.setId(in.getId());
		clone.setNom(in.getNom());
		clone.setPrenom(in.getPrenom());
		clone.setLogin(in.getLogin());
		clone.setEmail(in.getEmail());
		
		clone.setAvatar(in.getAvatar());
		clone.setRole(in.getRole());
		clone.setIdAsso(in.getAssociation().getId());
		
		clone.setIdRegion(in.getIdRegion());
		clone.setIdCommunauteDeCommunes(in.getIdCommunauteCommune());
		clone.setIdCommune(in.getIdCommune());
		
		if (in.getIdCommune()!=null) {
		Optional<City> oCity = cityRepository.findById(in.getIdCommune());
			if (oCity.isPresent()) {
				model.addAttribute("idCommune", in.getIdCommune());
				model.addAttribute("txtCommune", oCity.get().getName());
			}
		}
		
		clone.setRegions(regionRepository.findAllOrderByName());
		model.addAttribute("regions", clone.getRegions());
		if (in.getIdRegion()!=null) {
			clone.setCommunautesDeCommunes(communauteCommuneRepository.findByRegionId(in.getIdRegion()));
			model.addAttribute("communautesDeCommunes", clone.getCommunautesDeCommunes());
		}
		if (in.getIdCommunauteCommune()!=null) {
			clone.setCommunes(cityRepository.findByCommunauteCommuneId(in.getIdCommunauteCommune()));
			model.addAttribute("communes", clone.getCommunes());
		}
		
		return clone;
	}
	
	/**
	 * mapper.
	 * @param in form
	 * @return BO
	 */
	private Contributeur mapFromForm(FormContributor in) {
		Contributeur clone = new Contributeur();
		clone.setId(in.getId());
		clone.setNom(in.getNom());
		clone.setPrenom(in.getPrenom());
		clone.setLogin(in.getLogin());
		clone.setEmail(in.getEmail());
		clone.setPassword(in.getPassword());
		
		clone.setAvatar(in.getAvatar());
		clone.setRole(in.getRole());
		
		Association asso = associationService.findById(in.getIdAsso());
		clone.setAssociation(asso);
		
		clone.setIdRegion(in.getIdRegion());
		clone.setIdCommunauteCommune(in.getIdCommunauteDeCommunes());
		clone.setIdCommune(in.getIdCommune());
		return clone;
	}
	
	

}
