package com.github.cunvoas.geoserviceisochrone.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Classe utilitaire permettant de réduire le nombre de points d'une géométrie d'origine.
 * Étapes principales :
 * <ul>
 *   <li>Détecter l'enveloppe pour obtenir les points extrêmes (et réserver les points qui intersectent la géométrie d'origine)</li>
 *   <li>Supprimer les voisins de ces points</li>
 *   <li>Supprimer les autres points sur la base d'une régression linéaire des formes internes</li>
 * </ul>
 */
@Component
@Slf4j
public class GeometryPointReducer {

	/**
     * Réduit la géométrie en ne conservant que les points les plus à l'extérieur (enveloppe convexe).
     * @param complexGeo géométrie d'origine
     * @return géométrie réduite à son enveloppe convexe
     */
    public Geometry reduceByConvexHull(Geometry complexGeo) {
        if (complexGeo == null || complexGeo.isEmpty()) return complexGeo;
        // Utilisation de l'algorithme ConvexHull de JTS
        org.locationtech.jts.algorithm.ConvexHull hullAlg = new org.locationtech.jts.algorithm.ConvexHull(complexGeo);
        Geometry convexHull = hullAlg.getConvexHull();
        return convexHull;
    }

    /**
     * Réduit la géométrie à son enveloppe convexe puis ramène le nombre de points à 10 maximum (et 6 minimum).
     * <ul>
     *   <li>Si le ConvexHull a moins de 6 points, il est retourné tel quel.</li>
     *   <li>Si le ConvexHull a entre 6 et 10 points, il est retourné tel quel.</li>
     *   <li>Si le ConvexHull a plus de 10 points, on échantillonne uniformément les sommets pour n'en garder que 10.</li>
     * </ul>
     * @param complexGeo géométrie d'origine
     * @return géométrie réduite à 6-10 points sur l'enveloppe convexe
     */
    public Geometry reduceConvexHullToMax10Min6(Geometry complexGeo) {
        if (complexGeo == null || complexGeo.isEmpty()) return complexGeo;
        org.locationtech.jts.algorithm.ConvexHull hullAlg = new org.locationtech.jts.algorithm.ConvexHull(complexGeo);
        Geometry convexHull = hullAlg.getConvexHull();
        Coordinate[] coords = convexHull.getCoordinates();
        int n = coords.length;
        if (n <= 6) {
            return convexHull;
        } else if (n <= 10) {
            return convexHull;
        } else {
            // Échantillonnage uniforme pour garder 10 points (fermeture du polygone si besoin)
            int target = 10;
            List<Coordinate> sampled = new ArrayList<>();
            double step = (double)(n - 1) / (target - 1); // n-1 car le dernier point = premier pour polygone fermé
            for (int i = 0; i < target - 1; i++) {
                int idx = (int)Math.round(i * step);
                sampled.add(coords[idx]);
            }
            // Ajouter le dernier point pour fermer si c'est un polygone
            if (convexHull instanceof org.locationtech.jts.geom.Polygon) {
                sampled.add(sampled.get(0));
                return convexHull.getFactory().createPolygon(sampled.toArray(new Coordinate[0]));
            } else {
                sampled.add(coords[n - 1]);
                return convexHull.getFactory().createLineString(sampled.toArray(new Coordinate[0]));
            }
        }
    }
    
    /**
     * Retourne la liste des points distincts (sans doublons) composant la géométrie fournie.
     * @param geom géométrie d'entrée (Polygon, LineString, Multi*, etc.)
     * @return liste des objets Point uniques (ordre d'apparition)
     */
    public List<Point> getPoints(Geometry geom) {
        List<Point> points = new ArrayList<>();
        if (geom == null || geom.isEmpty()) return points;
        org.locationtech.jts.geom.GeometryFactory factory = geom.getFactory();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (Coordinate coord : geom.getCoordinates()) {
            String key = coord.x + "," + coord.y;
            if (!seen.contains(key)) {
                points.add(factory.createPoint(coord));
                seen.add(key);
            }
        }
        return points;
    }
}