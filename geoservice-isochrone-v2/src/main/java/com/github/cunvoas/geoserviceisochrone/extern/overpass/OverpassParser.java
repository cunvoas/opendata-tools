package com.github.cunvoas.geoserviceisochrone.extern.overpass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.dto.Element;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.dto.LatLon;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.dto.Node;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.dto.Relation;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.dto.Relation.Member;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.dto.Way;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;


/**
 * Parseur Overpass JSON streaming pour gros fichiers.
 * <p>
 * Exemple d'utilisation :
 * <pre>{@code
 * @Autowired
 * OverpassParser parser;
 * ...
 * try (InputStream in = new FileInputStream("/chemin/vers/overpass.json")) {
 *     parser.parseElements(in, element -> {
 *         // Traitement de chaque élément (Node, Way, Relation)
 *         System.out.println(element.getClass().getSimpleName() + " id=" + element.id);
 *     });
 * }
 * }</pre>
 */
@Component
public class OverpassParser {
	
    private final ObjectMapper objectMapper;
    private final ParkOverpassRepository parkOverpassRepository;

    @Autowired
    public OverpassParser(ObjectMapper objectMapper, ParkOverpassRepository parkOverpassRepository) {
        this.objectMapper = objectMapper;
        this.parkOverpassRepository = parkOverpassRepository;
    }

    /**
     * Parse les éléments du flux Overpass un par un et les transmet au consumer.
     */
    public void parseElements(InputStream inputStream, Consumer<ParkOverpass> consumer) throws IOException {
        try (JsonParser parser = objectMapper.createParser(inputStream)) {
            // Cherche le champ "elements"
            while (parser.nextToken() != null) {
                JsonToken token = parser.currentToken();
                if (token == JsonToken.PROPERTY_NAME && "elements".equals(parser.currentName())) {
                    token = parser.nextToken(); // doit être START_ARRAY
                    if (token == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != null && parser.currentToken() != JsonToken.END_ARRAY) {
                            if (parser.currentToken() == JsonToken.START_OBJECT) {
                                Element element = parser.readValueAs(Element.class);
                                // skip Node, juste a POI
                                if (!"node".equalsIgnoreCase(element.type)) {
                                    consumer.accept(map(element));
                                }
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
     * Prépare la boucle de traitement sur chaque élément.
     *
     * @param path chemin du fichier Overpass JSON
     * @throws IOException en cas d'erreur d'accès ou de parsing
     */
    public void parseEntityFromPath(java.nio.file.Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            this.parseElements(in, entity -> {
                // traitement de chaque élément (Node, Way, Relation)
            	parkOverpassRepository.save(entity);
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
    public ParkOverpass map(Element element) {
        ParkOverpass out = new ParkOverpass();
        out.setId(Long.valueOf(element.id));
        out.setType(element.type);
        out.setName(element instanceof Node node && node.tags != null ? node.tags.getOrDefault("name", null) : null);
        
        if (element.bounds!=null) {
	        LatLon cne=new LatLon();
	        cne.lat=element.bounds.maxlat;
	        cne.lon=element.bounds.maxlon;
	        out.setCornerNorthEast(mapPoint(cne));
	
	        LatLon cso=new LatLon();
	        cso.lat=element.bounds.minlat;
	        cso.lon=element.bounds.minlon;
	        out.setCornerSouthWest(mapPoint(cso));
        }
        
        if (element instanceof Way way) {
        	out.setShape(this.mapPolygon( way.geometry) );
            if (way.tags != null) {
                out.setTags(way.tags);
                out.setOperatorName(way.tags.getOrDefault("operator", null));
                out.setOpeningHours(way.tags.getOrDefault("opening_hours", null));
                out.setAccesible("yes".equalsIgnoreCase(way.tags.getOrDefault("access", "")));
                out.setName(way.tags.getOrDefault("name", null));
            }
        } else if (element instanceof Relation relation) {
        	//
        	List<Relation.Member> members = relation.members;
            out.setShape(this.mapMultiPolygon(members));
        	 
            if (relation.tags != null) {
                out.setTags(relation.tags);
                out.setOperatorName(relation.tags.getOrDefault("operator", null));
                out.setOpeningHours(relation.tags.getOrDefault("opening_hours", null));
                out.setAccesible("yes".equalsIgnoreCase(relation.tags.getOrDefault("access", "")));
                out.setName(relation.tags.getOrDefault("name", null));
            }
        }
        return out;
    }
    
    /**
     * Construit une géométrie JTS (Polygon) à partir d'une liste de LatLon.
     * <p>
     * Utilise mapListPoint pour convertir chaque LatLon en Point JTS, puis GeoShapeHelper.getPolygon pour créer le polygone.
     * Retourne null si la liste est vide ou invalide.
     *
     * @param geometry liste de coordonnées LatLon (souvent issue du champ "geometry" d'un élément Overpass)
     * @return Polygon JTS ou null
     */
    public Geometry mapPolygon(List<LatLon> geometry) {
        return GeoShapeHelper.getPolygon(mapListPoint(geometry));
    }
    
    // List de Member
    public Geometry mapMultiPolygon(List<Relation.Member> members) {
    	Geometry multi=null;
    	List<Polygon> outers=new ArrayList<>();
    	List<Polygon> inners=new ArrayList<>();
    	
        for (Member member : members) {
            // Vérifie que le member.element n'est pas null et est bien un Way
            if (member.element == null || !(member.element instanceof Way)) {
                continue;
            }
            Way way = (Way) member.element;
            if ("outer".equalsIgnoreCase(member.role)) {
                List<LatLon> geometry = way.geometry;
                Polygon poly = GeoShapeHelper.getPolygon(mapListPoint(geometry));
                // si surface >0 alors ajouter à la liste
                if (poly != null && poly.getArea() > 0) {
                    outers.add(poly);
                }
            } else if ("inner".equalsIgnoreCase(member.role)) {
                List<LatLon> geometry = way.geometry;
                Polygon poly = GeoShapeHelper.getPolygon(mapListPoint(geometry));
                // si surface >0 alors ajouter à la liste
                if (poly != null && poly.getArea() > 0) {
                    inners.add(poly);
                }
            }
        }
    	if (outers.isEmpty()) {
    		// no shapes
    	} else if (outers.size()==1 && inners.size()==0) {
    		multi = outers.get(0);
    	} else {
        	// assembler les outers et inners en MultiPolygon ou Polygon avec trous
    		MultiPolygon mPoly = GeoShapeHelper.getMultiPolygon(outers, inners);
    		multi = mPoly;
    	}
    	
    	return multi;
    }

    /**
     * Transforme une liste de LatLon en liste de Point JTS.
     * <p>
     * Chaque LatLon est converti en Point JTS via mapPoint.
     * Retourne une liste vide si l'entrée est nulle.
     *
     * @param latlon liste de coordonnées LatLon
     * @return liste de Point JTS
     */
    public List<Point> mapListPoint(List<LatLon> latlon) {
        if (latlon == null) return List.of();
        return latlon.stream()
                .map(this::mapPoint)
                .collect(Collectors.toList());
    }

    /**
     * Transforme un LatLon en Point JTS.
     * <p>
     * Utilise GeoShapeHelper.getPoint(lat, lon) pour la conversion.
     *
     * @param latlon objet LatLon (lat, lon)
     * @return Point JTS
     */
    public Point mapPoint(LatLon latlon) {
        return GeoShapeHelper.getPoint(latlon.lat, latlon.lon);
    }
    
    
}