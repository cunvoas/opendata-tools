package com.github.cunvoas.geoserviceisochrone.service.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectSimulatorService {
	
	@Autowired
	private ProjectSimulatorRepository projectSimulatorRepository;
	
	public ProjectSimulator getById(Long id) {
		return projectSimulatorRepository.findById(id).orElse(null);
	}
	
	public ProjectSimulator save(ProjectSimulator projectSimulator) {
		return projectSimulatorRepository.save(projectSimulator);
	}
	
	public List<ProjectSimulator> findByCity(Long idCommune) {
		if (idCommune == null) {
			return List.of();
		}
		return projectSimulatorRepository.findByIdCommune(idCommune);
	}
	
}
