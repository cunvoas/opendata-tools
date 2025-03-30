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
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper.GeoJsonIris;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 *  Intégrateur des fond iris.
 */
@Component
@Slf4j
public class IrisGeoJsonIntegratorParser {

	@Autowired
	private GeoJson2GeometryHelper geoParser;

	@Autowired
	private IrisShapeRepository irisShapeRepository;

	/**
	 * Parse INSPIRE geojson file.
	 * 
	 * @param fGeojson
	 */
	public void parseAndSave(String fGeojson) {

		String theLine = null;
		BufferedReader br = null;
		Reader rGeojson = null;
		List<IrisShape> shapes = new ArrayList<>(100);
		try {
			rGeojson = new FileReader(fGeojson);
			// open input stream test.txt for reading purpose.
			br = new BufferedReader(rGeojson);

			long start = System.currentTimeMillis();
			System.out.println();
			int i = 0;
			while ((theLine = br.readLine()) != null) {

				IrisShape shape = this.processLine(theLine);
				if (shape != null) {
					shapes.add(shape);
					i++;
				}

				if (i == 100) {
					// batch update
					irisShapeRepository.saveAll(shapes);
					shapes.clear();
					i = 0;
				}
			}

			// ens the last uncomplete block
			if (!shapes.isEmpty()) {
				irisShapeRepository.saveAll(shapes);
			}

			System.out.println(System.currentTimeMillis() - start);

		} catch (IOException e) {
			log.error("err:{}", theLine);
		}

	}

	/**
	 * processLine.
	 * @param theLine geojson line
	 * @return IrisShape
	 */
	public IrisShape processLine(String theLine) {
		IrisShape shape = null;
		try {
			GeoJsonIris geoIris = geoParser.parseIris(theLine);

			shape = new IrisShape();
			shape.setIris(geoIris.codeIris);
			if (geoIris.geometry != null) {
				shape.setContour(geoIris.geometry);
				// gain de 2% en retirant ce bloc
        		shape.setCoordonnee(geoIris.geometry.getCentroid());
			}
			shape.setCodeInsee(geoIris.codeInsee);
			shape.setCleabs(geoIris.cleabs);
			shape.setCommune(geoIris.nomCommune);
			shape.setFid(geoIris.fid);
			shape.setIris4(geoIris.irisCourt);
			shape.setNomIris(geoIris.nomIris);
			shape.setTypeIris(geoIris.typeIris);


		} catch (JsonProcessingException e) {
			log.info("err:{}", theLine);
		}
		return shape;
	}

}
