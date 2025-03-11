package com.github.cunvoas.geoserviceisochrone.service.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardCache;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardSummary;
import com.github.cunvoas.geoserviceisochrone.repo.DashboardCacheRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.AssociationRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

/**
 * Business Service impl.
 */
@Service
public class DashboadService {
	
	@Autowired
	private ContributeurRepository contributeurRepository;
	@Autowired
	private AssociationRepository associationRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mRepository;
	@Autowired
	private Filosofil200mRepository filosofil200mRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkEntranceRepository parkEntranceRepository;
	@Autowired
	private DashboardCacheRepository dashboardCacheRepository;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	public void refresh() {
		List<DashboardCache> cache = new ArrayList<>();
		
		cache.add(new DashboardCache(DashboardCache.ANNES, applicationBusinessProperties.getInseeAnnees().length));
		cache.add(new DashboardCache(DashboardCache.CARREAUX, inseeCarre200mRepository.count()));
		cache.add(new DashboardCache(DashboardCache.FILOSOFIL, filosofil200mRepository.count()));
		cache.add(new DashboardCache(DashboardCache.ASSOS, associationRepository.count()));
		cache.add(new DashboardCache(DashboardCache.CONTRIB, contributeurRepository.count()));
		cache.add(new DashboardCache(DashboardCache.COM2CO, communauteCommuneRepository.count()));
		cache.add(new DashboardCache(DashboardCache.COMMUNE, cadastreRepository.count()));
		cache.add(new DashboardCache(DashboardCache.PARC_CALC, parkAreaComputedRepository.count()));
		cache.add(new DashboardCache(DashboardCache.PARC_CHCK, parkAreaRepository.count()));
		cache.add(new DashboardCache(DashboardCache.PARC_ENTREE, parkEntranceRepository.count()));
		cache.add(new DashboardCache(DashboardCache.PARC_REF, parkJardinRepository.count()));
		
		dashboardCacheRepository.saveAll(cache);
	}

	public DashboardSummary getDashboardAndRefresh() {
		refresh();
		return getDashboard();
	}
	
	public DashboardSummary getDashboard() {
		
		if (11!=dashboardCacheRepository.count()) {
			refresh();
		}
		
		DashboardSummary ret = new DashboardSummary();
		
		ret.setNbContributeur(dashboardCacheRepository.findById(DashboardCache.CONTRIB).get().getIndicator());
		ret.setNbAssociation(dashboardCacheRepository.findById(DashboardCache.ASSOS).get().getIndicator());

		ret.setNbAnnee(dashboardCacheRepository.findById(DashboardCache.ANNES).get().getIndicator());
		ret.setNbCarreau(dashboardCacheRepository.findById(DashboardCache.CARREAUX).get().getIndicator());
		ret.setNbFilosofil(dashboardCacheRepository.findById(DashboardCache.FILOSOFIL).get().getIndicator());

		ret.setNbCommunauteCommune(dashboardCacheRepository.findById(DashboardCache.COM2CO).get().getIndicator());
		ret.setNbCommune(dashboardCacheRepository.findById(DashboardCache.COMMUNE).get().getIndicator());
		
		ret.setNbParcReference(dashboardCacheRepository.findById(DashboardCache.PARC_REF).get().getIndicator());
		ret.setNbParc(dashboardCacheRepository.findById(DashboardCache.PARC_CHCK).get().getIndicator());
		ret.setNbParcIsochrone(dashboardCacheRepository.findById(DashboardCache.PARC_CALC).get().getIndicator());
		ret.setNbParcEntance(dashboardCacheRepository.findById(DashboardCache.PARC_ENTREE).get().getIndicator());
		
		return ret;
	}
}
