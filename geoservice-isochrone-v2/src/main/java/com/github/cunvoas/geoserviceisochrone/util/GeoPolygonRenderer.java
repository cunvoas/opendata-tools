package com.github.cunvoas.geoserviceisochrone.util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GeoPolygonRenderer {
    /**
     * Génère une image (BufferedImage) représentant la Geometry JTS.
     */
    public static BufferedImage toImage(Geometry geometry, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(2f));

        // Trouver les bornes de la géométrie
        Envelope env = geometry.getEnvelopeInternal();
        double minX = env.getMinX();
        double minY = env.getMinY();
        double maxX = env.getMaxX();
        double maxY = env.getMaxY();
        double scaleX = (width - 20) / (maxX - minX);
        double scaleY = (height - 20) / (maxY - minY);
        double scale = Math.min(scaleX, scaleY);

        drawGeometry(g2, geometry, width, height, minX, minY, scale);
        g2.dispose();
        return image;
    }

    private static void drawGeometry(Graphics2D g2, Geometry geometry, int width, int height, double minX, double minY, double scale) {
        if (geometry instanceof Polygon) {
            drawPolygon(g2, (Polygon) geometry, width, height, minX, minY, scale);
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                drawPolygon(g2, (Polygon) mp.getGeometryN(i), width, height, minX, minY, scale);
            }
        } else if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, width, height, minX, minY, scale);
        } else if (geometry instanceof MultiLineString) {
            MultiLineString mls = (MultiLineString) geometry;
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                drawLineString(g2, (LineString) mls.getGeometryN(i), width, height, minX, minY, scale);
            }
        } else if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, width, height, minX, minY, scale);
        } else if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                drawPoint(g2, (Point) mp.getGeometryN(i), width, height, minX, minY, scale);
            }
        } else if (geometry instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) geometry;
            for (int i = 0; i < gc.getNumGeometries(); i++) {
                drawGeometry(g2, gc.getGeometryN(i), width, height, minX, minY, scale);
            }
        }
    }

    private static void drawPolygon(Graphics2D g2, Polygon polygon, int width, int height, double minX, double minY, double scale) {
        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        int[] xPoints = new int[coords.length];
        int[] yPoints = new int[coords.length];
        for (int i = 0; i < coords.length; i++) {
            xPoints[i] = (int) ((coords[i].x - minX) * scale) + 10;
            yPoints[i] = height - ((int) ((coords[i].y - minY) * scale) + 10);
        }
        g2.drawPolygon(xPoints, yPoints, coords.length);
        // Optionally fill
        Color old = g2.getColor();
        g2.setColor(new Color(173, 216, 230, 128)); // light blue, semi-transparent
        g2.fillPolygon(xPoints, yPoints, coords.length);
        g2.setColor(old);
    }

    private static void drawLineString(Graphics2D g2, LineString line, int width, int height, double minX, double minY, double scale) {
        Coordinate[] coords = line.getCoordinates();
        for (int i = 1; i < coords.length; i++) {
            int x1 = (int) ((coords[i-1].x - minX) * scale) + 10;
            int y1 = height - ((int) ((coords[i-1].y - minY) * scale) + 10);
            int x2 = (int) ((coords[i].x - minX) * scale) + 10;
            int y2 = height - ((int) ((coords[i].y - minY) * scale) + 10);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private static void drawPoint(Graphics2D g2, Point point, int width, int height, double minX, double minY, double scale) {
        int x = (int) ((point.getX() - minX) * scale) + 10;
        int y = height - ((int) ((point.getY() - minY) * scale) + 10);
        g2.fillOval(x - 4, y - 4, 8, 8);
    }

	/**
	 * Génère une représentation SVG de 2 Geometry JTS, à la même échelle, chaque géométrie avec une couleur différente.
	 */
	public static String toSVG(Geometry geometry1, Geometry geometry2, int width, int height) {
		// Calculer l'enveloppe englobante des deux géométries
		Envelope env1 = geometry1.getEnvelopeInternal();
		Envelope env2 = geometry2.getEnvelopeInternal();
		double minX = Math.min(env1.getMinX(), env2.getMinX());
		double minY = Math.min(env1.getMinY(), env2.getMinY());
		double maxX = Math.max(env1.getMaxX(), env2.getMaxX());
		double maxY = Math.max(env1.getMaxY(), env2.getMaxY());
		double scaleX = (width - 20) / (maxX - minX);
		double scaleY = (height - 20) / (maxY - minY);
		double scale = Math.min(scaleX, scaleY);

		StringBuilder sb = new StringBuilder();
		sb.append("<svg width=\"").append(width).append("\" height=\"").append(height).append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
		// Première géométrie : couleur 1
		appendGeometrySVG(sb, geometry1, width, height, minX, minY, scale, "#1E90FF", "#87CEFA", 0.5); // bleu
		// Deuxième géométrie : couleur 2
		appendGeometrySVG(sb, geometry2, width, height, minX, minY, scale, "#FF4500", "#FFDAB9", 0.5); // orange
		sb.append("\n</svg>");
		return sb.toString();
	}

	// Surcharge de appendGeometrySVG pour gérer les couleurs
	private static void appendGeometrySVG(StringBuilder sb, Geometry geometry, int width, int height, double minX, double minY, double scale, String strokeColor, String fillColor, double fillOpacity) {
		if (geometry instanceof Polygon) {
			appendPolygonSVG(sb, (Polygon) geometry, width, height, minX, minY, scale, strokeColor, fillColor, fillOpacity);
		} else if (geometry instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) geometry;
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				appendPolygonSVG(sb, (Polygon) mp.getGeometryN(i), width, height, minX, minY, scale, strokeColor, fillColor, fillOpacity);
			}
		} else if (geometry instanceof LineString) {
			appendLineStringSVG(sb, (LineString) geometry, width, height, minX, minY, scale, strokeColor);
		} else if (geometry instanceof MultiLineString) {
			MultiLineString mls = (MultiLineString) geometry;
			for (int i = 0; i < mls.getNumGeometries(); i++) {
				appendLineStringSVG(sb, (LineString) mls.getGeometryN(i), width, height, minX, minY, scale, strokeColor);
			}
		} else if (geometry instanceof Point) {
			appendPointSVG(sb, (Point) geometry, width, height, minX, minY, scale, strokeColor);
		} else if (geometry instanceof MultiPoint) {
			MultiPoint mp = (MultiPoint) geometry;
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				appendPointSVG(sb, (Point) mp.getGeometryN(i), width, height, minX, minY, scale, strokeColor);
			}
		} else if (geometry instanceof GeometryCollection) {
			GeometryCollection gc = (GeometryCollection) geometry;
			for (int i = 0; i < gc.getNumGeometries(); i++) {
				appendGeometrySVG(sb, gc.getGeometryN(i), width, height, minX, minY, scale, strokeColor, fillColor, fillOpacity);
			}
		}
	}

	private static void appendPolygonSVG(StringBuilder sb, Polygon polygon, int width, int height, double minX, double minY, double scale, String strokeColor, String fillColor, double fillOpacity) {
		sb.append("  <polygon points=\"");
		Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
		for (Coordinate c : coords) {
			int x = (int) ((c.x - minX) * scale) + 10;
			int y = height - ((int) ((c.y - minY) * scale) + 10);
			sb.append(x).append(",").append(y).append(" ");
		}
		sb.append("\" style=\"fill:").append(fillColor).append(";stroke:").append(strokeColor).append(";stroke-width:2;fill-opacity:").append(fillOpacity).append("\"/>");
	}

	private static void appendLineStringSVG(StringBuilder sb, LineString line, int width, int height, double minX, double minY, double scale, String strokeColor) {
		sb.append("  <polyline points=\"");
		Coordinate[] coords = line.getCoordinates();
		for (Coordinate c : coords) {
			int x = (int) ((c.x - minX) * scale) + 10;
			int y = height - ((int) ((c.y - minY) * scale) + 10);
			sb.append(x).append(",").append(y).append(" ");
		}
		sb.append("\" style=\"fill:none;stroke:").append(strokeColor).append(";stroke-width:2\"/>");
	}

	private static void appendPointSVG(StringBuilder sb, Point point, int width, int height, double minX, double minY, double scale, String color) {
		int x = (int) ((point.getX() - minX) * scale) + 10;
		int y = height - ((int) ((point.getY() - minY) * scale) + 10);
		sb.append("  <circle cx=\"").append(x).append("\" cy=\"").append(y).append("\" r=\"5\" fill=\"").append(color).append("\"/>");
	}

    // Pour compatibilité ascendante
    public static BufferedImage toImage(Polygon polygon, int width, int height) {
        return toImage((Geometry) polygon, width, height);
    }
    public static String toSVG(Polygon polygon, int width, int height) {
        return toSVG((Geometry) polygon, width, height);
    }

    public static String toSVG(Geometry geometry, int width, int height) {
        // Utilise la méthode existante pour une seule géométrie, couleur bleue par défaut
        StringBuilder sb = new StringBuilder();
        Envelope env = geometry.getEnvelopeInternal();
        double minX = env.getMinX();
        double minY = env.getMinY();
        double maxX = env.getMaxX();
        double maxY = env.getMaxY();
        double scaleX = (width - 20) / (maxX - minX);
        double scaleY = (height - 20) / (maxY - minY);
        double scale = Math.min(scaleX, scaleY);
        sb.append("<svg width=\"").append(width).append("\" height=\"").append(height).append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        appendGeometrySVG(sb, geometry, width, height, minX, minY, scale, "#1E90FF", "#87CEFA", 0.5);
        sb.append("\n</svg>");
        return sb.toString();
    }
}