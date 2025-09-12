package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.controller.form.SearchListDto;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des listes déroulantes de contributeurs.
 * Permet la recherche de communautés de communes et de villes selon différents critères.
 */
@RestController
@RequestMapping("/mvc/ajax/dropdown")
@Slf4j
public class ContributeurRestControler {
	
	
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	
	
	private static int MAX_LINE=150;
	
	
	 /**
	 * Recherche les communautés de communes d'une région, avec ou sans filtre de texte.
	 *
	 * @param idRegion Identifiant de la région
	 * @param query Texte de recherche (optionnel)
	 * @return Liste de DTO correspondant aux communautés de communes trouvées
	 */
	@GetMapping("/search_c2c")
	 public List<SearchListDto> searchC2C(
			 @RequestParam(value = "r") Long idRegion,
			 @RequestParam(value = "q", required = false) String query
			 ) {
		 log.info("rest search_c2c reg:{},q:{}", idRegion, query);
        if (StringUtils.isEmpty(query)) {
            return convertListToStream(communauteCommuneRepository.findByRegionId(idRegion))
                         .limit(MAX_LINE)
                         .map(this::mapToDto)
                         .collect(Collectors.toList());
        }
        return convertListToStream(communauteCommuneRepository.findByRegionId(idRegion))
        		 	 .map(this::mapToDto)
                     .filter(dto -> dto.getText()
                                           .toLowerCase()
                                           .contains(query))
                     .limit(MAX_LINE)
                     .collect(Collectors.toList());
    }

	 /**
	 * Recherche les villes selon la région, la communauté de communes et/ou un texte de recherche.
	 *
	 * @param idRegion Identifiant de la région (optionnel)
	 * @param idC2C Identifiant de la communauté de communes (optionnel)
	 * @param query Texte de recherche (optionnel)
	 * @return Liste de DTO correspondant aux villes trouvées
	 */
	@GetMapping("/search_city")
	 public List<SearchListDto> searchCity(
			 @RequestParam(value = "r", required = false) Long idRegion,
			 @RequestParam(value = "c", required = false) Long idC2C,
			 @RequestParam(value = "q", required = false) String query
			 ) {
		 log.info("rest search_city reg:{},c2c:{},q:{}", idRegion, idC2C, query);
		 
		 List<SearchListDto> ret = new ArrayList<>();
		 
		String normalized = normalise(query);
		if (idC2C==null) {
	        if (StringUtils.isEmpty(query)) {
	        	ret=  convertListToStream(cityRepository.findByRegionId(idRegion))
	                         .limit(MAX_LINE)
	                         .map(this::mapToDto)
	                         .collect(Collectors.toList());
	        } else {
	        	ret=  convertListToStream(cityRepository.findByRegionIdAndName(idRegion, "%"+normalized+"%"))
	       		 	 .map(this::mapToDto)
	                    .filter(dto -> dto.getText()
	                                          .toUpperCase()
	                                          .contains(query))
	                    .limit(MAX_LINE)
	                    .collect(Collectors.toList());
	        }
	        
		} else { //idC2C!=null
	        if (StringUtils.isEmpty(query)) {
	        	if (idRegion!=null) {
		        	ret= convertListToStream(cityRepository.findByRegionIdAndCommunauteCommuneId(idRegion, idC2C))
	                        .limit(MAX_LINE)
	                        .map(this::mapToDto)
	                        .collect(Collectors.toList());
	        		
	        	} else {
		        	ret= convertListToStream(cityRepository.findByCommunauteCommuneId(idC2C))
	                        .limit(MAX_LINE)
	                        .map(this::mapToDto)
	                        .collect(Collectors.toList());
	        	}
	        	
	        } else {
	        	ret=  convertListToStream(cityRepository.findByCommunauteCommuneId(idC2C))
	       		 	 .map(this::mapToDto)
	                    .filter(dto -> dto.getText()
	                                          .toUpperCase()
	                                          .contains(normalized))
	                    .limit(MAX_LINE)
	                    .collect(Collectors.toList());
	        }

		}
		
		if (log.isDebugEnabled()) {
			SearchListDto src = SearchListDto.builder().id(2878L).text("LILLE").build();
			if (ret.contains(src)){	
				log.debug("found {}", src);
			}
		}
 		return ret;

    }


    // 
    /**
     * Fonction utilitaire générique pour convertir une liste en stream.
     *
     * @param <T> Type générique
     * @param list Liste d'éléments
     * @return Stream des éléments
     */
    private static <T> Stream<T> convertListToStream(List<T> list) {
        return list.stream();
    }
    
    /**
     * Convertit un objet CommunauteCommune en DTO pour la liste déroulante.
     *
     * @param bean Entité CommunauteCommune
     * @return DTO pour la liste déroulante
     */
    private SearchListDto mapToDto(CommunauteCommune bean) {
        return SearchListDto.builder()
                        .id(bean.getId())
                        .text(bean.getName())
                        .build();
    }
    
    /**
     * Convertit un objet City en DTO pour la liste déroulante.
     *
     * @param bean Entité City
     * @return DTO pour la liste déroulante
     */
    private SearchListDto mapToDto(City bean) {
        return SearchListDto.builder()
                        .id(bean.getId())
                        .text(bean.getName())
                        .build();
    }	
    
    /**
     * Normalise une chaîne de caractères (suppression des accents, mise en majuscules).
     *
     * @param q Chaîne à normaliser
     * @return Chaîne normalisée
     */
    private String normalise(String q) {
    	if (q!=null) {
	    	String work = StringUtils.stripAccents(q);
	    	return work.toUpperCase();
    	}
    	return "";
    }


}