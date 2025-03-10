package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

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

@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "adm_activity_stats")
@Table(indexes = {
		  @Index(name = "idx_activity_action", columnList = "action"),
		  @Index(name = "idx_activity_userId", columnList = "userId")
		})
public class Stats {
	
	public static final String EVT_ENTRANCE="EVT_ENTRANCE";
	public static final String EVT_ISOCHRONE="EVT_ISOCHRONE";
	public static final String EVT_PARK="EVT_PARK";
	public static final String EVT_ADMIN="EVT_ADMIN";
	public static final String MODE_ADD="ADD";
	public static final String MODE_UPD="UPD";
	
	public Stats(String action) {
		super();
		this.action=action;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_activity_stats")
    @SequenceGenerator(
    		name="seq_activity_stats",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;
	
	private Long userId;
	private String action;
	private String mode;
	private Date update=new Date();

}
