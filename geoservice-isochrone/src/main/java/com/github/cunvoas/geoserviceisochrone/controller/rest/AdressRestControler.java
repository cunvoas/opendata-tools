package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.controller.form.SearchListDto;
import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.AdresseClientService;
import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * REsT Controler for adress dropdown.
 */
@RestController
@RequestMapping("/mvc/ajax/dropdown")
@Slf4j
public class AdressRestControler {
	
	
	@Autowired
	private AdresseClientService adresseClientService;
	@Autowired
	private CityRepository cityRepository;
	
	private static int MAX_LINE=150;
	

	 /**
	 * search.
	 * @param idCity
	 * @param query
	 * @return
	 */
	@GetMapping("/search_adress")
	 public List<SearchListDto> searchAdress(
			 @RequestParam(value = "idCity", required = false) Long idCity,
			 @RequestParam(value = "q", required = false) String query
			 ) {
		 log.info("rest search_city idCity:{},q:{}", idCity,  query);
		 
		 List<SearchListDto> ret = new ArrayList<>();
		 
		String normalized = normalise(query);
		if (normalized.length()>3) {
			Optional<City> oCity = cityRepository.findById(idCity);
			if (oCity.isPresent()) {
				String insee=oCity.get().getInseeCode();
				Set<AdressBo> adresses = adresseClientService.getAdresses(insee, normalized);
				
				ret = adresses.stream()
	                    .limit(MAX_LINE)
	                    .map(this::mapToDto)
	                    .collect(Collectors.toList());
			}
		}
		
		log.info(ret.toString());
 		return ret;

    }


    // Generic function to convert a list to stream
//    private static <T> Stream<T> convertListToStream(List<T> list) {
//        return list.stream();
//    }
    
    private SearchListDto mapToDto(AdressBo bean) {
        return SearchListDto.builder()
                        .id(Long.valueOf(bean.hashCode()))
                        .text(bean.getLabel())
                        .value(this.getValue(bean.getPoint()))
                        .lon(String.valueOf(bean.getPoint().getX()))
                        .lat(String.valueOf(bean.getPoint().getY()))
                        .build();
    }
    
    private String getValue(Point point) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("lon|").append(point.getX());
    	sb.append("|lat|").append(point.getY());
    	return sb.toString();
    }
    
    private String normalise(String q) {
    	if (q!=null) {
	    	String work = StringUtils.stripAccents(q);
	    	return work.toUpperCase();
    	}
    	return "";
    }


}
