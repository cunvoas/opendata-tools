package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.topo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;
import com.github.cunvoas.geoserviceisochrone.repo.ignTopo.IgnTopoVegetalRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IgnTopoJsonIntegratorParser {

	@Autowired
	private GeoJson2GeometryHelper geoParser;

	@Autowired
	private IgnTopoVegetalRepository ignTopoVegetalRepository;

	/**
	 * Parse un fichier GeoJSON IGN-TOPO et enregistre les entités  IGN-TOP correspondantes.
	 *
	 * @param annee Année d'intégration des données
	 * @param fGeojson Chemin du fichier GeoJSON à parser
	 */
	public void parseAndSaveVegetal(String fGeojson) {

		String theLine = null;
		BufferedReader br = null;
		Reader rGeojson = null;
		List<IgnTopoVegetal> shapes = new ArrayList<>(100);
		try {
			rGeojson = new FileReader(fGeojson);
			// open input stream test.txt for reading purpose.
			br = new BufferedReader(rGeojson);

			long start = System.currentTimeMillis();
			System.out.println();
			int i = 0;
			while ((theLine = br.readLine()) != null) {

				IgnTopoVegetal shape = geoParser.parseIgnTopo(theLine);
				if (shape != null) {
					shapes.add(shape);
					i++;
				}

				if (i == 100) {
					// batch update
					ignTopoVegetalRepository.saveAll(shapes);
					shapes.clear();
					i = 0;
				}
			}

			// ens the last uncomplete block
			if (!shapes.isEmpty()) {
				ignTopoVegetalRepository.saveAll(shapes);
			}

			System.out.println(System.currentTimeMillis() - start);

		} catch (IOException e) {
			log.error("err:{}", theLine);
		}
		
	}

}
