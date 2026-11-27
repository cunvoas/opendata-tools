package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.service.export.ServicePublicationExporter;
import com.github.cunvoas.geoserviceisochrone.util.LogSanitizer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.databind.DatabindException;

/**
 * Contrôleur REST pour l'export de fichiers (CSV, GeoJSON).
 * Permet d'exporter des données géographiques après vérification de la clé et de l'adresse IP.
 */
@RestController
@RequestMapping("/map/ajax/export")
@Slf4j
public class FileExporterControler {
	@Value("${application.security.export-credential-key}")
	private String credKey="DEFAULT_KEY";
	@Value("${application.security.export-ip-address}")
	private String credIp="192.168.1.0";
	
	@Autowired
	private ServicePublicationExporter servicePublicationExporter;
	
	
	
	/**
	 * Exporte les localisations au format GeoJSON.
	 * Nécessite une clé d'export et une adresse IP autorisée.
	 *
	 * @param credKeyParam Clé d'export (paramètre ?key=, déprécié)
	 * @param credKeyHeader Clé d'export (en-tête X-Export-Key, prioritaire)
	 * @param request Requête HTTP pour obtenir l'adresse IP du client
	 * @return Statut HTTP et succès de l'opération
	 */
	@GetMapping("/json")
	public ResponseEntity<Boolean> exportLocations( @RequestParam(name = "key", required = false) String credKeyParam,
			@RequestHeader(name = "X-Export-Key", required = false) String credKeyHeader, HttpServletRequest request) {
		Boolean success=Boolean.FALSE;
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		String givenKey = credKeyHeader != null ? credKeyHeader : credKeyParam;
		if (givenKey == null || !MessageDigest.isEqual(this.credKey.getBytes(StandardCharsets.UTF_8), givenKey.getBytes(StandardCharsets.UTF_8))) {
			log.info("exportLocations: credKey");
			status = HttpStatus.FORBIDDEN;
			return new ResponseEntity<Boolean>(success, status);
		} else {
			String remote = request.getRemoteAddr();
			if (!matchOk(remote)) {
				status = HttpStatus.FORBIDDEN;
				return new ResponseEntity<Boolean>(success, status);
			}
		}
		
		try {
			
			
//			servicePublicationExporter.writeRegions();
//			servicePublicationExporter.writeGeoJsonCadastres();

//			servicePublicationExporter.writeGeoJsonIsochrone();
			servicePublicationExporter.writeGeoJsonCarreauxAll();
			
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
	 * Vérifie si l'adresse IP du client est autorisée à exporter.
	 *
	 * @param ip Adresse IP à vérifier
	 * @return Vrai si l'adresse IP est autorisée, faux sinon
	 */
	private Boolean matchOk(String ip) {
		log.info("exportLocations->matchOk: {}", LogSanitizer.sanitize(ip));
		Boolean ret = Boolean.FALSE;
		String[] creds = credIp.split("\\.");
		String[] ips = ip.split("\\.");
		
		if("127.0.0.1".equals(ip)) {
			ret = Boolean.TRUE;
			
		} else if (creds.length==4 && ips.length==4) {
			boolean mem=true;
			for (int i = 0; i < 3; i++) {
				// all three first octets must match
				mem = mem && creds[i].equals(ips[i]);
			}
			ret=mem;
		}
		return ret;
	}
}