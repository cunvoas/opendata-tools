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
 * Abstract class is not scanned.
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
	 * isActive.
	 * @param pa ParkArea
	 * @param annee year 
	 * @return park is active?
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
	 * getSurface.
	 * @param geom  Geometry
	 * @return surface of Geometry
	 */
	public Long getSurface(Geometry geom) {
		return inseeCarre200mOnlyShapeRepository.getSurface(geom);
	}

}
