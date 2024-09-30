package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

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
@RequestMapping("/mvc/management/contrib")
@Slf4j
public class ContributeurRestControler {
	
	
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	
	
	
	 @GetMapping("/search_c2c")
	 public List<SearchListDto> searchC2C(
			 @RequestParam(value = "r") Long idRegion,
			 @RequestParam(value = "q", required = false) String query
			 ) {
        if (StringUtils.isEmpty(query)) {
            return convertListToStream(communauteCommuneRepository.findByRegionId(idRegion))
                         .limit(50)
                         .map(this::mapToDto)
                         .collect(Collectors.toList());
        }
        return convertListToStream(communauteCommuneRepository.findByRegionId(idRegion))
        		 	 .map(this::mapToDto)
                     .filter(dto -> dto.getText()
                                           .toLowerCase()
                                           .contains(query))
                     .limit(15)
                     .collect(Collectors.toList());
    }

	 @GetMapping("/search_city")
	 public List<SearchListDto> searchCity(
			 @RequestParam(value = "c") Long idC2C,
			 @RequestParam(value = "q", required = false) String query
			 ) {
        if (StringUtils.isEmpty(query)) {
            return convertListToStream(cityRepository.findByCommunauteCommuneId(idC2C))
                         .limit(50)
                         .map(this::mapToDto)
                         .collect(Collectors.toList());
        }
        return convertListToStream(cityRepository.findByCommunauteCommuneId(idC2C))
        		 	 .map(this::mapToDto)
                     .filter(dto -> dto.getText()
                                           .toLowerCase()
                                           .contains(query))
                     .limit(15)
                     .collect(Collectors.toList());
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


}
