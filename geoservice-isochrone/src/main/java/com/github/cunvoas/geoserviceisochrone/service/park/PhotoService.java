package com.github.cunvoas.geoserviceisochrone.service.park;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageProcessingException;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionPhoto;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.PhotoHelper;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.tools.ParkPhoto;
import com.github.cunvoas.geoserviceisochrone.repo.ParkPhotoRepository;
import com.github.cunvoas.geoserviceisochrone.service.park.dto.PhotoDto;
import com.google.common.hash.Hashing;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhotoService {
	
	@Autowired
	private ParkPhotoRepository parkPhotoRepository;
	
	@Autowired
	private PhotoHelper photoHelper;
	
	public String savePhoto(PhotoDto dto) {
		String savedPath = null;
		
		if (dto.getPhoto().isEmpty()) {
			return savedPath;
			//throw new ExceptionPhoto("Failed to store empty file.");
		}
		
		// create folder if required
		File folder = new File(dto.getStoreRoot()+dto.getStoreFolder());
		if (!folder.exists()) {
			folder.mkdirs();
		}
		folder = new File(dto.getStoreRootOrigin()+dto.getStoreFolder());
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		try {
			ParkPhoto photo = this.map(dto);
			photo = parkPhotoRepository.save(photo);
			
		} catch (IllegalStateException e) {
			log.error(dto.toString(), e);
			throw new ExceptionPhoto("IllegalStateException");
		} catch (ImageProcessingException e) {
			log.error(dto.toString(), e);
			throw new ExceptionPhoto("ImageProcessingException");
		} catch (IOException e) {
			log.error(dto.toString(), e);
			throw new ExceptionPhoto("IOException");
		}
		
		return savedPath;
	}
	
	private ParkPhoto map(PhotoDto dto) throws IllegalStateException, IOException, ImageProcessingException {
		String rand=StringUtils.right(String.valueOf(System.nanoTime()), 4);
		
		ParkPhoto photo = new ParkPhoto();
		photo.setParcId(dto.getParcEtJardin().getId());
		photo.setCommuneId(dto.getCommuneId());
		photo.setInseeCode(dto.getInseeCode());
		photo.setStoredFolder(dto.getStoreFolder()+"/");
		photo.setOriginalFileName(dto.getPhoto().getOriginalFilename());
		
	    File fOrignal = new File(dto.getStoreRootOrigin()+dto.getStoreFolder()+"/"+photo.getOriginalFileName());
	    dto.getPhoto().transferTo(fOrignal);
	    photo.setOriginalFileHash(getHash(fOrignal));
	    
		photo.setCurrentFileName(String.format("%s_%s_%s.jpg", dto.getInseeCode(), photo.getParcId(), rand));
		
		Coordinate coord=photoHelper.getCoordinateFromExif(fOrignal);
		photo.setLocation(GeoShapeHelper.getPoint(coord));
		
		File fResize = new File(dto.getStoreRoot()+dto.getStoreFolder()+"/"+photo.getCurrentFileName());
		photoHelper.resizeImage(fOrignal, fResize);
		
		return photo;
	}
	

	private String getHash(File originalFile) throws IOException {
		Path p = Path.of(originalFile.getPath());
		log.info("read for hash {}", originalFile.getPath());
		byte[] content = Files.readAllBytes(p);
		
		String sha256hex = Hashing.sha256()
				  .hashBytes(content)
				  .toString();
		log.info("hash is {}, len={}", sha256hex, sha256hex.length());
		return  sha256hex;
	}
	
}
