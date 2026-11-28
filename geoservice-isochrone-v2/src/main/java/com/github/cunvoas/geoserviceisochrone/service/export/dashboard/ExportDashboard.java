package com.github.cunvoas.geoserviceisochrone.service.export.dashboard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * État des lieux des exports JSON/GeoJSON du dépôt de publication.
 * Lecture seule : sert au dashboard avant le commit final.
 */
@Data
public class ExportDashboard {

	private String repoPath;
	private String head;
	private String branch;
	private boolean repositoryFound;
	private String snapshotHash;
	private LocalDateTime generatedAt = LocalDateTime.now();
	private Long scopeEpciId;
	private String scopeLabel;
	private Integer yearFilter;
	private int page;
	private int pageSize;
	private long totalEpcis;
	private List<String> changedFiles = new ArrayList<>();
	private List<EpciState> epcis = new ArrayList<>();
	private List<String> blockers = new ArrayList<>();
	private List<String> warnings = new ArrayList<>();

	@Data
	public static class EpciState {
		private Long id;
		private String name;
		private String siren;
		private Integer year;
		private boolean referential;
		private boolean territoires;
		private boolean statsUrbans;
		private boolean statsSuburbs;
		private boolean statsAll;
		private int citiesTotal;
		private int citiesWithStats;
		private int citiesWithNdvi;
		private boolean parkOutline;
		private boolean isochrone;
		private boolean carres;
		private boolean iris;
		private boolean ndvi;
		private String status;

		public void computeStatus() {
			boolean any = referential || territoires || statsUrbans || statsSuburbs || statsAll
					|| parkOutline || isochrone || carres || iris || ndvi || citiesWithStats > 0;
			boolean all = referential && territoires && statsUrbans && statsSuburbs && statsAll
					&& parkOutline && isochrone && carres && iris
					&& citiesTotal > 0 && citiesWithStats == citiesTotal;
			if (all) {
				status = "OK";
			} else if (any) {
				status = "PARTIEL";
			} else {
				status = "ABSENT";
			}
		}
	}
}
