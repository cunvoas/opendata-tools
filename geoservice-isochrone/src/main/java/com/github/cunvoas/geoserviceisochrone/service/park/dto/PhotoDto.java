package com.github.cunvoas.geoserviceisochrone.service.park.dto;

import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;

import lombok.Data;

@Data
public class PhotoDto {
	
	private ParcEtJardin parcEtJardin;
	
	private MultipartFile photo;
	private String storeRoot;
	private String storeRootOrigin;
	private String storeFolder;
	private Long communeId;
	private String inseeCode;
	
}
