package com.github.cunvoas.geoserviceisochrone.service.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.repo.admin.AssociationRepository;

/**
 * Business Service impl.
 */
@Service
public class AssociationService {
	
	@Autowired
	private AssociationRepository associationRepository;
	
	
	/**
	 * findById.
	 * @param id Association
	 * @return Association
	 */
	public Association findById(Long id){
		Association asso = null;
		Optional<Association> opt = associationRepository.findById(id);
		
		if (opt.isPresent()) {
			asso = opt.get();
		}
		return asso;
	}
	
	/**
	 * findByContextUser.
	 * @param contrib Contributeur
	 * @return list Association
	 */
	public List<Association> findByContextUser(Contributeur contrib){
		List<Association> asso = new ArrayList<>();
		
		Long id = null;
		if (contrib.getAssociation()!=null) {
			id = contrib.getAssociation().getId();
		}
		
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			asso.addAll(findAll());
		} else {

			Optional<Association> opt = associationRepository.findById(id);
			if (opt.isPresent()) {
				asso.add( opt.get() );
			}
		}
		return asso;
	}
	
	/**
	 * findAll.
	 * @return list Association
	 */
	public List<Association> findAll() {
		return associationRepository.findAllOrderByNom();
	}
	
	/**
	 * save.
	 * @param asso Association
	 * @return Association
	 */
	public Association save(Association asso) {
		return associationRepository.save(asso);
	}

}
