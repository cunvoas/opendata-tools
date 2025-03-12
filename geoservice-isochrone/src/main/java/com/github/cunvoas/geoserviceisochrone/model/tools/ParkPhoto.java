package com.github.cunvoas.geoserviceisochrone.model.tools;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model ParkPhoto.
 */
@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "parc_photo")
@Table(indexes = {
		  @Index(name = "idx_parcphoto_loc", columnList = "communeId, location"),
		  @Index(name = "idx_parcphoto_hash", columnList = "hash")
		})
public class ParkPhoto {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_photo")
    @SequenceGenerator(
    		name="seq_photo",
    		allocationSize=1,
    		initialValue = 1 )
	private Long id;

	@Column(name = "parc_id")
	private Long parcId;
	
	@Column(name = "commune_id")
	private Long communeId;
	@Column(name = "insee_code", length = 5)
	private String inseeCode;

	@Column(name = "folder", length = 255)
	private String storedFolder;
	
	@Column(name = "name", length = 50)
	private String currentFileName;
	
	@Column(name = "name_org", length = 50)
	private String originalFileName;
	
	@Column(name = "hash", length = 64)
	private String originalFileHash;

	@Column(name = "location")
	private Point location;
	
	
}
