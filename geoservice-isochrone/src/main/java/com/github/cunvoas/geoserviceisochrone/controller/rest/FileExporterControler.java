package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.github.cunvoas.geoserviceisochrone.service.export.ServicePublicationExporter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * export a CSV file.
 */
@RestController
@RequestMapping("/map/export")
@Slf4j
public class FileExporterControler {
	@Value("${application.security.export-credential-key}")
	private String credKey="DEFAULT_KEY";
	@Value("${application.security.export-ip-address}")
	private String credIp="192.168.1.0";
	
	@Autowired
	private ServicePublicationExporter servicePublicationExporter;
	
	
	
	/**
	 * write geojson.
	 * @param credKey
	 * @param request
	 * @return
	 */
	@GetMapping("/json")
	public ResponseEntity<Boolean> exportLocations( @RequestParam("key") String credKey, HttpServletRequest request) {
		Boolean success=Boolean.FALSE;
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		if (!this.credKey.equals(credKey)) {
			log.info("exportLocations: credKey");
			status = HttpStatus.FORBIDDEN;
			return new ResponseEntity<Boolean>(success, status);
		} else {
			String remote = getClientIp(request);
			if (!matchOk(remote)) {
				status = HttpStatus.FORBIDDEN;
				return new ResponseEntity<Boolean>(success, status);
			}
		}
		
		try {
			
			
//			servicePublicationExporter.writeRegions();
//			servicePublicationExporter.writeGeoJsonCadastres();
//
			servicePublicationExporter.writeGeoJsonIsochrone();
			servicePublicationExporter.writeGeoJsonCarreaux();
			
			status = HttpStatus.CREATED;
			success = Boolean.TRUE;
		} catch (StreamWriteException e) {
			log.error("writeRegions", e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		} catch (DatabindException e) {
			log.error("writeRegions", e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		} catch (IOException e) {
			log.error("writeRegions", e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		return new ResponseEntity<Boolean>(success, status);
	}
	

	/**
	 * check autoriezd IPs.
	 * @param ip
	 * @return
	 */
	private Boolean matchOk(String ip) {
		log.info("exportLocations->matchOk: {}", ip);
		Boolean ret = Boolean.FALSE;
		String[] creds = credIp.split("\\.");
		String[] ips = ip.split("\\.");
		
		if("127.0.0.1".equals(ip)) {
			ret = Boolean.TRUE;
			
		} else if (creds.length==4) {
			boolean mem=true;
			for (int i = 0; i < 3; i++) {
				// OR at least one false, all is false
				mem=mem || creds[i]==ips[i];
			}
			ret=mem;
		}
		return ret;
	}
	
	private static final String[] IP_HEADER_CANDIDATES = {
	        "X-Forwarded-For",
	        "Proxy-Client-IP",
	        "WL-Proxy-Client-IP",
	        "HTTP_X_FORWARDED_FOR",
	        "HTTP_X_FORWARDED",
	        "HTTP_X_CLUSTER_CLIENT_IP",
	        "HTTP_CLIENT_IP",
	        "HTTP_FORWARDED_FOR",
	        "HTTP_FORWARDED",
	        "HTTP_VIA",
	        "REMOTE_ADDR"
	    };
	
	/**
	 * get IP.
	 * @param request
	 * @return
	 */
	protected static String getClientIp(HttpServletRequest request) {
        for (String header: IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
                String ip = ipList.split(",")[0];
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
