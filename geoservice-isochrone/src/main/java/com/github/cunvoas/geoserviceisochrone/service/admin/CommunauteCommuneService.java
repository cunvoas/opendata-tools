package com.github.cunvoas.geoserviceisochrone.service.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

/**
 * Service de gestion des communautés de communes.
 * Permet d'effectuer des opérations CRUD sur les entités CommunauteCommune.
 */
@Service
public class CommunauteCommuneService {
	
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	
	/**
	 * Recherche une communauté de communes par son identifiant.
	 * 
	 * @param id Identifiant de la communauté de communes
	 * @return CommunauteCommune ou null si non trouvée
	 */
	public CommunauteCommune findById(Long id) {
		Optional<CommunauteCommune> opt = communauteCommuneRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	
	/**
	 * Recherche les communautés de communes accessibles selon le contexte utilisateur.
	 * 
	 * @param contrib Contributeur connecté
	 * @return Liste des communautés de communes accessibles
	 */
	public List<CommunauteCommune> findByContextUser(Contributeur contrib) {
		List<CommunauteCommune> ret = null;
		
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			// Admin : accès à toutes les communautés
			ret = communauteCommuneRepository.findAll();
			
		} else {
			// Autres utilisateurs : accès à toutes les communautés (à affiner selon les besoins)
			ret = communauteCommuneRepository.findAll();
		}
		
		return ret;
	}
	
	/**
	 * Recherche les communautés de communes accessibles selon le contexte utilisateur et filtrées par région.
	 * 
	 * @param contrib Contributeur connecté
	 * @param regionId Identifiant de la région (optionnel)
	 * @return Liste des communautés de communes accessibles
	 */
	public List<CommunauteCommune> findByContextUserAndRegion(Contributeur contrib, Long regionId) {
		List<CommunauteCommune> ret = null;
		
		if (ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			// Admin : accès à toutes les communautés
			if (regionId != null) {
				ret = communauteCommuneRepository.findByRegionId(regionId);
			} else {
				ret = communauteCommuneRepository.findAll();
			}
			
		} else {
			// Autres utilisateurs : accès à toutes les communautés (à affiner selon les besoins)
			if (regionId != null) {
				ret = communauteCommuneRepository.findByRegionId(regionId);
			} else {
				ret = communauteCommuneRepository.findAll();
			}
		}
		
		return ret;
	}
	
	/**
	 * Retourne toutes les communautés de communes.
	 * 
	 * @return Liste de toutes les communautés de communes
	 */
	public List<CommunauteCommune> findAll() {
		return communauteCommuneRepository.findAll();
	}
	
	/**
	 * Sauvegarde ou met à jour une communauté de communes.
	 * 
	 * @param comm2co CommunauteCommune à sauvegarder
	 * @return CommunauteCommune sauvegardée
	 */
	public CommunauteCommune save(CommunauteCommune comm2co) {
		if (comm2co != null) {
			CommunauteCommune pComm2co = null;
			if (comm2co.getId() != null) {
				Optional<CommunauteCommune> oComm2co = communauteCommuneRepository.findById(comm2co.getId());
				if (oComm2co.isPresent()) {
					pComm2co = oComm2co.get();
					pComm2co.setName(comm2co.getName());
					pComm2co.setRegion(comm2co.getRegion());
				}

				comm2co = communauteCommuneRepository.save(pComm2co);
			} else {
				comm2co = communauteCommuneRepository.save(comm2co);
			}
		}
		return comm2co;
	}
}
