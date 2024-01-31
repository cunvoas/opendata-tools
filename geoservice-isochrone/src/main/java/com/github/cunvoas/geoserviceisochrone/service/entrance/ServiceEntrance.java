package com.github.cunvoas.geoserviceisochrone.service.entrance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceEntrance {
	@Autowired
	private ParkEntranceRepository parkEntranceRepository;
	
	public ParkEntrance save(ParkEntrance entrance) {
		entrance = parkEntranceRepository.save(entrance);
		
		
		
		
		return entrance;
	}
	
	
	
}
