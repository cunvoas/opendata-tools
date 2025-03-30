package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisDataRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * ServiceIris.
 */
@Service
@Slf4j
public class ServiceIris {
	
	@Autowired
	private IrisDataRepository irisDataRepository;
	@Autowired
	private IrisShapeRepository irisShapeRepository;
	
	@Transactional
	public void saveAllData(List<IrisData> datas) {
		irisDataRepository.saveAll(datas);
	}
	
	@Transactional
	public void saveAllShape(List<IrisShape> datas) {
		irisShapeRepository.saveAll(datas);
	}
	
	
}
