package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.ArrayList;
import java.util.Collections;
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
		
		SearchListDto src = SearchListDto.builder().id(2878L).text("LILLE").build();
		//if (Collections.binarySearch(ret, src)>0){
		if (ret.contains(src)){	
			log.error("found {}", src);
		}
 		return ret;

    }


    // Generic function to convert a list to stream
    private static <T> Stream<T> convertListToStream(List<T> list) {
        return list.stream();
    }
    
    private SearchListDto mapToDto(CommunauteCommune bean) {
        return SearchListDto.builder()
                        .id(bean.getId())
                        .text(bean.getName())
                        .build();
    }
    
    private SearchListDto mapToDto(City bean) {
        return SearchListDto.builder()
                        .id(bean.getId())
                        .text(bean.getName())
                        .build();
    }	
    
    private String normalise(String q) {
    	if (q!=null) {
	    	String work = StringUtils.stripAccents(q);
	    	return work.toUpperCase();
    	}
    	return "";
    }


}
