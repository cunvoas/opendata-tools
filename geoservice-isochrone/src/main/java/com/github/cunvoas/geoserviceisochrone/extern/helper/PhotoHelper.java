package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

/**
 * Composant utilitaire pour la gestion et le traitement des photos.
 * Permet de redimensionner des images et d'extraire les coordonnées GPS des métadonnées EXIF.
 * @author cunvoas
 * @see https://www.baeldung.com/java-resize-image
 */
@Component
public class PhotoHelper {

	@Value("${application.admin.photo-path}")
	private String imagesPath = "/tmp/";
	private int targetSize = 1024;

	/**
	 * Redimensionne une image et l'enregistre sous le nom du parc.
	 * @param img fichier image source
	 * @param parkName nom du parc (utilisé pour le nom du fichier de sortie)
	 * @throws IOException en cas d'erreur de lecture/écriture
	 */
	public void resizeImage(File img, String parkName) throws IOException {
		this.resizeImage(img, new File(imagesPath + parkName+".jpg"));
	}
	/**
	 * Redimensionne une image et l'enregistre dans le fichier de sortie.
	 * @param img fichier image source
	 * @param out fichier de sortie
	 * @throws IOException en cas d'erreur de lecture/écriture
	 */
	public void resizeImage(File img, File out) throws IOException {

		BufferedImage originalImage = ImageIO.read(img);

		int currentWidth = originalImage.getWidth();
		int currentHeight = originalImage.getHeight();

		// define max size orentation
		Scalr.Mode mode = Scalr.Mode.FIT_TO_WIDTH;
		if (currentHeight > currentWidth) {
			mode = Scalr.Mode.FIT_TO_HEIGHT;
		}

		BufferedImage newOne = Scalr.resize(originalImage, Scalr.Method.QUALITY, mode, targetSize, Scalr.OP_ANTIALIAS);

		ImageIO.write(newOne, "jpg", out);
	}

	/**
	 * Extrait les coordonnées GPS des métadonnées EXIF d'une image.
	 * @param img fichier image
	 * @return coordonnées GPS extraites ou null si absentes
	 * @throws IOException en cas d'erreur de lecture
	 * @throws ImageProcessingException en cas d'erreur de traitement des métadonnées
	 */
	public Coordinate getCoordinateFromExif(File img) throws IOException, ImageProcessingException {
		Coordinate gps = null;

		Metadata metadata = ImageMetadataReader.readMetadata(img);
		
		for (Directory directory : metadata.getDirectories()) {
			
			if ("GPS".equals(directory.getName())) {
				if (directory instanceof GpsDirectory) {
					GpsDirectory gpsd = (GpsDirectory)directory;
					GeoLocation geo = gpsd.getGeoLocation();
					gps=new Coordinate(geo.getLongitude(), geo.getLatitude());
					
					break;
				}
				
				// old code to be deleted
				String lat = "";
				String lon = "";
				for (Tag tag : directory.getTags()) {
					
					if (tag.getTagType()>=1 && tag.getTagType()<=2) {
						lat+=" "+tag.getDescription();
					}
					if (tag.getTagType()>=3 && tag.getTagType()<=4) {
						lon+=" "+tag.getDescription();
					}
				}
				gps=new Coordinate(getDecimalGps(lon), getDecimalGps(lat));
			}
		}

		return gps;
	}
	
	/**
	 * Transform °'" into decimal.
	 * @param coord in °'"
	 * @return decimal
	 */
	public Double getDecimalGps(String coord) {

		Double ret=null;
		if (coord!=null && coord.trim().length()>0) {

			String[] splited = coord.split(" ");

			String sig="";
			String loc = splited[1];
			
			switch (loc) {
				case "N": 
				case "E": 
					sig="+";
					break;
					
				case "S": 
				case "O": 
					sig="-";
					break;
					
				default:
					throw new IllegalArgumentException("Unexpected value: " + loc);
			}
			
			String sDeg = splited[2].replace("°", "");
			String sMin = splited[3].replace("'", "");
			String sSec = splited[4].replace("\"", "").replace(",", ".");
			
			Double deg = Double.valueOf(sig+sDeg);
			Double min = Double.valueOf(sMin);
			Double sec = Double.valueOf(sSec);
			
			// raise up precision
			ret = (deg*3600 + min*60 + sec)/3600;
		}
		return ret;
	}
	
	
	
	

}