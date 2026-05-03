package com.github.cunvoas.geoserviceisochrone.extern.overpass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.common.AbstractOverpassParser;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.common.LatLon;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.geojsondto.DtoGeojsonFeature;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

import jakarta.transaction.Transactional;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;


/**
 * Parseur Overpass GEOJSON streaming pour gros fichiers.
 * @see https://overpass-turbo.eu/#
 * @see https://wiki.openstreetmap.org/wiki/Tag:landuse%3Dgreenery
 */
@Component
public class OverpassGeojsonParser extends AbstractOverpassParser {

    @Value("${overpass.parser.batch-size:100}")
    private int batchSize;

    private final ObjectMapper objectMapper;
    private final ParkOverpassRepository parkOverpassRepository;

    @Autowired
    public OverpassGeojsonParser(ObjectMapper objectMapper, ParkOverpassRepository parkOverpassRepository) {
        this.objectMapper = objectMapper;
        this.parkOverpassRepository = parkOverpassRepository;
    }

    /**
     * Parse les éléments du flux Overpass un par un et les transmet au consumer.
     */
    public void parseElements(InputStream inputStream, Consumer<DtoGeojsonFeature> consumer) throws IOException {
        try (JsonParser parser = objectMapper.createParser(inputStream)) {
            // Cherche le champ "features"
            while (parser.nextToken() != null) {
                JsonToken token = parser.currentToken();
                if (token == JsonToken.PROPERTY_NAME && "features".equals(parser.currentName())) {
                    token = parser.nextToken(); // doit être START_ARRAY
                    if (token == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != null && parser.currentToken() != JsonToken.END_ARRAY) {
                            if (parser.currentToken() == JsonToken.START_OBJECT) {
                                DtoGeojsonFeature feature = parser.readValueAs(DtoGeojsonFeature.class);
                                consumer.accept(feature);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Helper pour parser un fichier Overpass à partir d'un Path.
     * Accumule les entités par batch de {@code batchSize} avant de les persister via saveAll().
     *
     * <p>Configuration Spring :
     * <pre>
     * overpass.parser.batch-size=100          # chunk applicatif
     * spring.jpa.properties.hibernate.jdbc.batch_size=50
     * spring.jpa.properties.hibernate.order_inserts=true
     * spring.jpa.properties.hibernate.order_updates=true
     * </pre>
     *
     * @param path chemin du fichier Overpass JSON
     * @throws IOException en cas d'erreur d'accès ou de parsing
     */
    @Transactional
    public void parseEntityFromFilePath(Path path) throws IOException {
        List<ParkOverpass> batch = new ArrayList<>(batchSize);
        try (InputStream in = Files.newInputStream(path)) {
            this.parseElements(in, feature -> {
                ParkOverpass entity = map(feature);
                batch.add(entity);
                if (batch.size() >= batchSize) {
                    parkOverpassRepository.saveAll(batch);
                    batch.clear();
                }
            });
            // flush du dernier batch partiel
            if (!batch.isEmpty()) {
                parkOverpassRepository.saveAll(batch);
                batch.clear();
            }
        }
    }
    
    public void parseEntityFromDirectoryPath(Path path) throws IOException {
        // Recherche tous les fichiers .geojson du répertoire (non récursif) en amont
        try (var files = Files.list(path)) {
            files.filter(p -> p.toString().endsWith(".geojson"))
                 .forEach(p -> {
                     try {
                         this.parseEntityFromFilePath(p);
                     } catch (IOException e) {
                         throw new RuntimeException("Erreur lors du parsing du fichier : " + p, e);
                     }
                 });
        }
    }
    
    /**
     * Map data from Overpass Element (Node, Way, Relation) to ParkOverpass entity.
     * Remplit les champs principaux et les tags. Les géométries sont à compléter selon besoin.
     *
     * @param element élément Overpass (Node, Way, Relation)
     * @return entité ParkOverpass partiellement remplie
     */
    public ParkOverpass map(DtoGeojsonFeature feature) {
        ParkOverpass out = new ParkOverpass();
        // Extraire l'id depuis properties : "@id" ou "id" (ex: "relation/3428166")
        String idStr = feature.properties != null ? (asString(feature.properties.get("@id")) != null ? asString(feature.properties.get("@id")) : asString(feature.properties.get("id"))) : null;
        if (idStr != null) {
            String idNum = idStr.replaceAll("^(relation/|way/|node/)", "");
            try {
                out.setId(Long.valueOf(idNum));
            } catch (NumberFormatException e) {
                out.setId(8880000000000000000L+UUID.randomUUID().getLeastSignificantBits());
            }
        }
        if (feature.properties != null) {
            out.setType(asString(feature.properties.get("leisure")));
            out.setName(asString(feature.properties.get("name")));
            out.setOperatorName(asString(feature.properties.get("operator")));
            out.setOpeningHours(asString(feature.properties.get("opening_hours")));
            out.setAccesible("yes".equalsIgnoreCase(asString(feature.properties.get("access"))));
            out.setTags(feature.properties);
        }
        // Mapping GeoJSON geometry -> JTS Geometry
        if (feature.geometry != null) {
            Geometry jtsGeom = parseGeoJsonGeometry(feature.geometry);
            out.setShape(jtsGeom);
            // Calcul géodésique de la surface (WGS84)
            Double surface = null;
            if ("Polygon".equalsIgnoreCase(feature.geometry.type)) {
                surface = computeGeodeticAreaFromGeoJsonPolygon(feature.geometry.coordinates);
            } else if ("MultiPolygon".equalsIgnoreCase(feature.geometry.type)) {
                surface = computeGeodeticAreaFromGeoJsonMultiPolygon(feature.geometry.coordinates);
            }
            if (surface != null) {
                out.setSurface(surface);
            }
        }
        return out;
    }

    /**
     * Calcule l'aire géodésique d'un Polygon GeoJSON (en m², WGS84).
     */
    private Double computeGeodeticAreaFromGeoJsonPolygon(List<Object> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) return null;
        try {
            List<?> exterior = (List<?>) coordinates.get(0);
            List<LatLon> latlons = new java.util.ArrayList<>();
            for (Object pt : exterior) {
                List<?> coord = (List<?>) pt;
                double lon = ((Number) coord.get(0)).doubleValue();
                double lat = ((Number) coord.get(1)).doubleValue();
                latlons.add(new LatLon(lat, lon));
            }
            return geodeticArea(latlons);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calcule l'aire géodésique d'un MultiPolygon GeoJSON (somme des aires, en m², WGS84).
     */
    private Double computeGeodeticAreaFromGeoJsonMultiPolygon(List<Object> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) return null;
        double total = 0.0;
        try {
            for (Object polyObj : coordinates) {
                List<?> rings = (List<?>) polyObj;
                if (!rings.isEmpty()) {
                    List<?> exterior = (List<?>) rings.get(0);
                    List<LatLon> latlons = new java.util.ArrayList<>();
                    for (Object pt : exterior) {
                        List<?> coord = (List<?>) pt;
                        double lon = ((Number) coord.get(0)).doubleValue();
                        double lat = ((Number) coord.get(1)).doubleValue();
                        latlons.add(new LatLon(lat, lon));
                    }
                    total += geodeticArea(latlons);
                }
            }
            return total;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convertit une géométrie GeoJSON (Polygon/MultiPolygon) en JTS Geometry.
     */
    private Geometry parseGeoJsonGeometry(com.github.cunvoas.geoserviceisochrone.extern.overpass.geojsondto.DtoGeojsonFeature.Geometry geom) {
        if (geom == null || geom.type == null || geom.coordinates == null) return null;
        try {
            if ("Polygon".equalsIgnoreCase(geom.type)) {
                // coordinates: List<List<List<Double>>>
                List<?> rings = geom.coordinates;
                List<org.locationtech.jts.geom.Point> shell = new java.util.ArrayList<>();
                if (!rings.isEmpty()) {
                    List<?> exterior = (List<?>) rings.get(0);
                    for (Object pt : exterior) {
                        List<?> coord = (List<?>) pt;
                        double lon = ((Number) coord.get(0)).doubleValue();
                        double lat = ((Number) coord.get(1)).doubleValue();
                        shell.add(com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper.getPoint(lon, lat));
                    }
                }
                return com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper.getPolygon(shell);
            } else if ("MultiPolygon".equalsIgnoreCase(geom.type)) {
                // coordinates: List<List<List<List<Double>>>>
                List<org.locationtech.jts.geom.Polygon> outers = new java.util.ArrayList<>();
                List<?> polygons = geom.coordinates;
                for (Object polyObj : polygons) {
                    List<?> rings = (List<?>) polyObj;
                    List<org.locationtech.jts.geom.Point> shell = new java.util.ArrayList<>();
                    if (!rings.isEmpty()) {
                        List<?> exterior = (List<?>) rings.get(0);
                        for (Object pt : exterior) {
                            List<?> coord = (List<?>) pt;
                            double lon = ((Number) coord.get(0)).doubleValue();
                            double lat = ((Number) coord.get(1)).doubleValue();
                            shell.add(com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper.getPoint(lon, lat));
                        }
                    }
                    outers.add(com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper.getPolygon(shell));
                }
                return null;//com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper.getMultiPolygon(outers);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    
    /**
     * Convertit une valeur en chaîne, gère les types connus.
     * @param value valeur à convertir
     * @return chaîne résultante ou null
     */
    private String asString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Number) return ((Number) value).toString();
        return value.toString();
    }
    
}