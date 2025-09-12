package com.github.cunvoas.geoserviceisochrone.extern.geojson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper.GeoJsonInspire;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Intégrateur de masse de fichiers GeoJSON pour les carreaux de 200m.
 * <p>
 * Cette classe permet d'importer en masse des entités à partir de fichiers GeoJSON INSPIRE.
 * ATTENTION : Import très lent (environ 17 000 lignes/heure, soit 33 jours pour un import complet).
 * <br>
 * Il est recommandé d'utiliser une importation directe en base de données.
 * </p>
 * <p>
 * Classe dépréciée.
 * Voir massImportCarreau200mSeuls.sql pour une alternative plus performante.
 * </p>
 */
@Component
@Slf4j
@Deprecated
public class MassGeoJsonIntegratorParser {

	@Autowired
	private GeoJson2GeometryHelper geoParser;

	@Autowired
	private Filosofil200mRepository filosofil200mRepository;

	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;

	/**
	 * Parse un fichier GeoJSON INSPIRE et enregistre les entités correspondantes.
	 *
	 * @param fGeojson Chemin du fichier GeoJSON à parser
	 */
	public void parseAndSave(String fGeojson) {

		String theLine = null;
		BufferedReader br = null;
		Reader rGeojson = null;
		List<InseeCarre200mOnlyShape> shapes = new ArrayList<>(100);
		try {
			rGeojson = new FileReader(fGeojson);
			// open input stream test.txt for reading purpose.
			br = new BufferedReader(rGeojson);

			long start = System.currentTimeMillis();
			System.out.println();
			int i = 0;
			while ((theLine = br.readLine()) != null) {

				InseeCarre200mOnlyShape shape = this.processLine(theLine);
				if (shape != null) {
					shapes.add(shape);
					i++;
				}

				if (i == 100) {
					// batch update
					inseeCarre200mOnlyShapeRepository.saveAll(shapes);
					shapes.clear();
					i = 0;
				}
			}

			// ens the last uncomplete block
			if (!shapes.isEmpty()) {
				inseeCarre200mOnlyShapeRepository.saveAll(shapes);
			}

			System.out.println(System.currentTimeMillis() - start);

		} catch (IOException e) {
			log.error("err:{}", theLine);
		}

	}

	/**
	 * @param theLine
	 * @return
	 */
	public InseeCarre200mOnlyShape processLine(String theLine) {
		InseeCarre200mOnlyShape shape = null;
		try {
			GeoJsonInspire idInspire = geoParser.parseInspire(theLine);

			shape = new InseeCarre200mOnlyShape();
			shape.setIdInspire(idInspire.idInspire);
			shape.setIdCarre1km(idInspire.id1km);
			if (idInspire.geometry != null) {
				shape.setGeoShape(idInspire.geometry);
				// gain de 2% en retirant ce bloc
        		shape.setGeoPoint2d(idInspire.geometry.getCentroid());
			}

			// gain max 15% en retirant ce bloc
    		List<Filosofil200m> filo = filosofil200mRepository.findByIdInspire(idInspire.idInspire);
    		if (filo!=null && !filo.isEmpty()) {
    			shape.setWithPop(Boolean.TRUE);
    		} else {
    			shape.setWithPop(Boolean.FALSE);
    		}

		} catch (JsonProcessingException e) {
			log.info("err:{}", theLine);
		}
		return shape;
	}

}