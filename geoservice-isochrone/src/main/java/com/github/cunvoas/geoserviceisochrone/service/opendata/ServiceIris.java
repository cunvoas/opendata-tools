package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.MurmurHash2;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisDataRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service métier pour la gestion des données et formes IRIS.
 * <p>
 * Ce service fournit des méthodes pour :
 * <ul>
 *   <li>Enregistrer en masse les données IRIS (IrisData) et les formes IRIS (IrisShape)</li>
 *   <li>Calculer et mettre à jour l'empreinte (footprint) d'une forme IRIS à partir de ses coordonnées</li>
 *   <li>Mettre à jour toutes les formes IRIS sans empreinte</li>
 * </ul>
 *
 * Les dépendances sont injectées via l'annotation @Autowired de Spring.
 */
@Service
@Slf4j
public class ServiceIris {
	@Autowired
	private IrisDataRepository irisDataRepository;
	@Autowired
	private IrisShapeRepository irisShapeRepository;
	
	/**
	 * Enregistre en base de données une liste d'entités IrisData.
	 * @param datas Liste des données IRIS à enregistrer
	 */
	@Transactional
	public void saveAllData(List<IrisData> datas) {
		irisDataRepository.saveAll(datas);
	}
	
	/**
	 * Enregistre en base de données une liste d'entités IrisShape.
	 * @param datas Liste des formes IRIS à enregistrer
	 */
	@Transactional
	public void saveAllShape(List<IrisShape> datas) {
		irisShapeRepository.saveAll(datas);
	}
	
	/**
	 * Calcule et met à jour l'empreinte (footprint) pour toutes les formes IRIS qui n'en possèdent pas.
	 */
	public void computeFootprint() {
		List<IrisShape> shapes = irisShapeRepository.findByFootprintIsNull();
		for (IrisShape irisShape : shapes) {
			this.update(irisShape);
		}
	}
	
	/**
	 * Calcule l'empreinte (footprint) d'une forme IRIS à partir de ses coordonnées et la met à jour en base.
	 * @param irisShape Forme IRIS à mettre à jour
	 * @return IrisShape mis à jour (avec empreinte)
	 */
	@Transactional
	public IrisShape update(IrisShape irisShape) {
		Geometry geom = irisShape.getContour();
		if (geom!=null) {
			List<String> sCoords= new ArrayList<>();
			Coordinate[] coords = geom.getCoordinates();
			sCoords.add(String.format("%s", coords.length));
			for (Coordinate coord : coords) {
				sCoords.add(String.format("X@%s:Y@%s", coord.x, coord.y));
			}
			Collections.sort(sCoords);
			StringBuilder sb = new StringBuilder();
			for (String sCoord : sCoords) {
				sb.append(sCoord).append("|");
			}
			// MurmurHash2 est un hash rapide non cryptographique
			Integer footprint = MurmurHash2.hash32(sb.toString());
			irisShape.setFootprint(footprint);
			return irisShapeRepository.save(irisShape);
		} else {
			return irisShape;
		}
	}
}