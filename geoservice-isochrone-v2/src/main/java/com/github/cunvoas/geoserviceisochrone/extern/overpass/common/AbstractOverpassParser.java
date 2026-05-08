package com.github.cunvoas.geoserviceisochrone.extern.overpass.common;

import java.util.List;

public abstract class AbstractOverpassParser {
	
    /**
     * Calcule l'aire géodésique (en m²) d'un polygone défini par une liste de LatLon (WGS84).
     * Utilise la formule sphérique adaptée (algorithme de l'aire de Shoelace sur la sphère).
     * @param geometry liste de LatLon (doit être fermée ou sera fermée automatiquement)
     * @return aire en mètres carrés
     */
    public double geodeticArea(List<LatLon> coords) {
        if (coords == null || coords.size() < 3) return 0.0;
        // Rayon moyen de la Terre en mètres (WGS84)
        final double R = 6371008.8;
        double area = 0.0;
        int n = coords.size();
        // S'assurer que le polygone est fermé
        boolean closed = coords.get(0).lat == coords.get(n-1).lat && coords.get(0).lon == coords.get(n-1).lon;
        int max = closed ? n-1 : n;
        for (int i = 0; i < max; i++) {
            LatLon p1 = coords.get(i);
            LatLon p2 = coords.get((i+1)%n);
            double lon1 = Math.toRadians(p1.lon);
            double lat1 = Math.toRadians(p1.lat);
            double lon2 = Math.toRadians(p2.lon);
            double lat2 = Math.toRadians(p2.lat);
            area += (lon2 - lon1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }
        area = area * R * R / 2.0;
        return Math.abs(area);
    }

}
