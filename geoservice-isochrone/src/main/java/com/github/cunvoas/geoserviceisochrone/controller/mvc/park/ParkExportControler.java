package com.github.cunvoas.geoserviceisochrone.controller.mvc.park;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.export.ServiceVerificationExporter;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.ParkExportCsv;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.ParkExportLine;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Page controler for park export.
 * /mvc/park/exportForCheck?communeId=2878
 */
@Controller
@RequestMapping("/mvc/park")
@Slf4j
public class ParkExportControler {

	@Autowired
	private ServiceReadReferences serviceReadReferences;
	@Autowired
	private ServiceVerificationExporter serviceVerificationExporter;
	@Autowired
	private ParkExportCsv parkExportCsv;
	
	/**
	 * export parks.
	 * @param com2coId com2co
	 * @param communeId city 
	 * @param response http response
	 * @return CSV file to download
	 */
	@GetMapping("/exportForCheck")
	public ResponseEntity<OutputStream> exportForCheck(
			@RequestParam(name = "com2coId", required = false) Long com2coId,
			@RequestParam("communeId") Long communeId, HttpServletResponse response){
		
		City city = serviceReadReferences.getCityById(communeId);
		
		List<ParkExportLine> lines = serviceVerificationExporter.export4verif(city);
		OutputStream bos=null;
		try {
			String name= city.getName().toUpperCase().replaceAll(" ", "_");
		    HttpHeaders headers = new HttpHeaders();
		    headers.add("Content-Type", "text/csv; charset=utf-8");
		    
		    headers.setContentDisposition(
		    		ContentDisposition
		    			.builder("attachment")
		    			.filename("PARCS_"+name+".csv").build()
		    		);
		    
			bos = parkExportCsv.steam(response.getOutputStream(), lines);
			
			 return ResponseEntity.ok()
			            .headers(headers)
			            .body(bos);
			 
			
		} catch (Exception e) {
			log.error("write on stream", e);
			return new ResponseEntity<OutputStream>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (bos!=null) {
					bos.close();
				}
			} catch (IOException e) {
				log.info("finally",e);
			}
		}
		
	
	}
}
