package com.github.cunvoas.geoserviceisochrone.service.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardSummary;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.AssociationRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

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
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkEntranceRepository parkEntranceRepository;
	
	
	public DashboardSummary getDashboard() {
		DashboardSummary ret = new DashboardSummary();
		
		ret.setNbContributeur(contributeurRepository.count());
		ret.setNbAssociation(associationRepository.count());
		
		ret.setNbCarreau(inseeCarre200mRepository.count());

		ret.setNbCommunauteCommune(communauteCommuneRepository.count());
		ret.setNbCommune(cadastreRepository.count());
		
		ret.setNbParcReference(parkJardinRepository.count());
		ret.setNbParc(parkAreaRepository.count());
		ret.setNbParcIsochrone(parkAreaComputedRepository.count());
		ret.setNbParcEntance(parkEntranceRepository.count());
		
		return ret;
	}
}
