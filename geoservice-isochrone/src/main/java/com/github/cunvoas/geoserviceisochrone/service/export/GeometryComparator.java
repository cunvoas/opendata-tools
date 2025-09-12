package com.github.cunvoas.geoserviceisochrone.service.export;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Comparateur de géométries basé sur le centroïde.
 * Permet de trier des objets Geometry selon la position de leur centroïde (coordonnées X puis Y).
 */
public class GeometryComparator implements Comparator<Geometry> {
	
	/**
	 * Compare deux objets Geometry selon la position de leur centroïde.
	 * Si l'un des objets est null, il est considéré comme inférieur ou supérieur selon le cas.
	 *
	 * @param g0 première géométrie à comparer
	 * @param g1 seconde géométrie à comparer
	 * @return un entier négatif, nul ou positif selon l'ordre des centroïdes
	 */
	@Override
	public int compare(Geometry g0, Geometry g1) {
		CompareToBuilder ctb = new CompareToBuilder();

		Point centroid0 = null;
		Point centroid1 = null;
		
		if (g0!=null) {
			centroid0 = g0.getCentroid();
		}
		
		if (g1!=null) {
			centroid1 = g1.getCentroid();
		}

		double x0=centroid0!=null?centroid0.getX():-181;
		double x1=centroid1!=null?centroid1.getX():+181;
		ctb.append(x0, x1);
		
		double y0=centroid0!=null?centroid0.getY():-91;
		double y1=centroid1!=null?centroid1.getY():+91;
		ctb.append(y0, y1);
		
		return ctb.toComparison();
	}

}