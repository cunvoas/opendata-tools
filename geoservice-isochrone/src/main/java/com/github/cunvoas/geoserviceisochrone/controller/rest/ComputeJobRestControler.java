package com.github.cunvoas.geoserviceisochrone.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobRequest;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobResponse;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.admin.BatchJobService;

@RestController
@RequestMapping("/mvc/management/jobs/api")
public class ComputeJobRestControler {

	@Autowired
	private BatchJobService batchJobService;
	
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	
	/**
	 * @param req
	 * @return
	 */
	@PostMapping("/request")
	public ResponseEntity<ComputeJobResponse> request(ComputeJobRequest req) {
		HttpStatus code= HttpStatus.OK;
		ComputeJobResponse resp = new ComputeJobResponse(req);
		
		if (req.getParkId()!=null) {
			ParcEtJardin pj = new ParcEtJardin();
			pj.setId(req.getCityId());
			resp.setNbCarre(batchJobService.requestProcessParc(pj));
			code= HttpStatus.ACCEPTED;
			
		} else if (req.getCityId()!=null) {
			City n = cityRepository.getReferenceById(req.getCityId());
			resp.setNbCarre(batchJobService.requestProcessCity(n));
			code= HttpStatus.ACCEPTED;
			
		} else if (req.getCom2coId()!=null) {
			CommunauteCommune com2co = communauteCommuneRepository.getReferenceById(req.getCom2coId());
			resp.setNbCarre(batchJobService.requestProcessCom2Co(com2co));
			code= HttpStatus.ACCEPTED;
		}
		
		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}
}
