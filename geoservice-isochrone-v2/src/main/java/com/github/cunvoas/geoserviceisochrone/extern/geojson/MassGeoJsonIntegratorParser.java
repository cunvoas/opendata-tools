package com.github.cunvoas.geoserviceisochrone.extern.geojson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper.GeoJsonInspire;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Intégrateur de masse de fichiers GeoJSON pour les carreaux de 200m (deprecated).
 * <p>
 * Permet d'importer en masse des entités à partir de fichiers GeoJSON INSPIRE.
 * <b>ATTENTION :</b> Import très lent (environ 17 000 lignes/heure, soit 33 jours pour un import complet).
 * Il est recommandé d'utiliser une importation directe en base de données (voir massImportCarreau200mSeuls.sql).
 * </p>
 * <p>
 * <b>Classe dépréciée.</b> Utiliser une solution SQL pour les imports volumineux.
 * </p>
 *
 * @author cunvoas
 * @deprecated Utiliser massImportCarreau200mSeuls.sql pour les imports volumineux.
 */
@Component
@Slf4j
@Deprecated
public class MassGeoJsonIntegratorParser {

    /**
     * Constructeur avec injection des dépendances nécessaires.
     * @param geoParser helper de parsing GeoJSON
     * @param filosofil200mRepository repository Filosofil200m
     * @param inseeCarre200mOnlyShapeRepository repository InseeCarre200mOnlyShape
     */
    @Autowired
    public MassGeoJsonIntegratorParser(GeoJson2GeometryHelper geoParser, Filosofil200mRepository filosofil200mRepository, InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository) {
        this.geoParser = geoParser;
        this.filosofil200mRepository = filosofil200mRepository;
        this.inseeCarre200mOnlyShapeRepository = inseeCarre200mOnlyShapeRepository;
    }

    private final GeoJson2GeometryHelper geoParser;
    private final Filosofil200mRepository filosofil200mRepository;
    private final InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;

    /**
     * Parse un fichier GeoJSON INSPIRE et enregistre les entités correspondantes en base.
     * @param fGeojson Chemin du fichier GeoJSON à parser
     */
    public void parseAndSave(String fGeojson) {
        String theLine = null;
        BufferedReader br = null;
        Reader rGeojson = null;
        List<InseeCarre200mOnlyShape> shapes = new ArrayList<>(100);
        try {
            rGeojson = new FileReader(fGeojson);
            br = new BufferedReader(rGeojson);
            long start = System.currentTimeMillis();
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
            // Enregistre le dernier bloc incomplet
            if (!shapes.isEmpty()) {
                inseeCarre200mOnlyShapeRepository.saveAll(shapes);
            }
            System.out.println(System.currentTimeMillis() - start);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture : {}", theLine);
        }
    }

    /**
     * Parse une ligne GeoJSON et retourne l'entité correspondante.
     * @param theLine ligne GeoJSON
     * @return entité InseeCarre200mOnlyShape ou null en cas d'erreur
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
                shape.setGeoPoint2d(idInspire.geometry.getCentroid());
            }
            List<Filosofil200m> filo = filosofil200mRepository.findByIdInspire(idInspire.idInspire);
            shape.setWithPop(filo != null && !filo.isEmpty());
        } catch (Exception e) {
            log.info("Erreur de parsing ligne : {}", theLine);
        }
        return shape;
    }

}