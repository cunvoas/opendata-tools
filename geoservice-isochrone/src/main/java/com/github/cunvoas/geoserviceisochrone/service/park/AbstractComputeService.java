package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

/**
 * Classe abstraite utilitaire pour les services de calcul liés aux parcs et jardins.
 * <p>
 * Fournit des méthodes pour vérifier l'activité d'un parc sur une année donnée et pour calculer la surface d'une géométrie.
 * Cette classe n'est pas scannée par Spring.
 */
public abstract class AbstractComputeService {

	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	
	private ParkJardinRepository parkJardinRepository;
	
	public AbstractComputeService(
				ParkJardinRepository parkJardinRepository,
				InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository
				) {
		super();
		this.parkJardinRepository = parkJardinRepository;
		this.inseeCarre200mOnlyShapeRepository=inseeCarre200mOnlyShapeRepository;
	}
	
	/**
	 * Vérifie si un parc est actif pour une année donnée.
	 * @param pa ParkArea à vérifier
	 * @param annee Année de référence
	 * @return TRUE si le parc est actif pour l'année, FALSE sinon
	 */
	protected Boolean isActive(ParkArea pa, Integer annee) {
		Boolean active=false;
		
		Optional<ParcEtJardin> oPj = parkJardinRepository.findById(pa.getIdParcEtJardin());
		if (oPj.isPresent()) {
			ParcEtJardin pj = oPj.get();
			Date dd = pj.getDateDebut();
			Date df = pj.getDateFin();
			
			Calendar cal = Calendar.getInstance();
			
			int d = 1900;
			if (dd!=null) {
				cal.setTime(dd);
				d = cal.get(Calendar.YEAR);
			}
			
			int f = 2100;
			if (df!=null) {
				cal.setTime(df);
				f = cal.get(Calendar.YEAR);
			}
			
			active = d<=annee && annee<=f;
		}
	
		return active;
	}
	
	/**
	 * Calcule la surface d'une géométrie.
	 * @param geom Géométrie à mesurer
	 * @return Surface de la géométrie
	 */
	public Long getSurface(Geometry geom) {
		return inseeCarre200mOnlyShapeRepository.getSurface(geom);
	}

}