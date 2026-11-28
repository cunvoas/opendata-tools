package com.github.cunvoas.geoserviceisochrone.service.admin;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;

/**
 * Portée d'accès du contributeur connecté.
 * Un administrateur voit tous les EPCI ; un gestionnaire d'association est limité
 * à son EPCI préféré ({@code idCommunauteCommune}).
 */
@Service
public class AccessScopeService {

	public Contributeur current() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof Contributeur contrib) {
			return contrib;
		}
		return null;
	}

	public boolean isAdmin(Contributeur contrib) {
		return contrib != null && ContributeurRole.ADMINISTRATOR.equals(contrib.getRole());
	}

	public boolean isManager(Contributeur contrib) {
		return contrib != null && ContributeurRole.ASSOCIATION_MANAGER.equals(contrib.getRole());
	}

	/**
	 * @return null pour un administrateur (pas de filtre), l'EPCI du gestionnaire sinon
	 */
	public Long scopedEpciId(Contributeur contrib) {
		return isAdmin(contrib) ? null : contrib == null ? null : contrib.getIdCommunauteCommune();
	}

	public boolean canAccessEpci(Contributeur contrib, Long epciId) {
		if (isAdmin(contrib)) {
			return true;
		}
		return isManager(contrib)
				&& epciId != null
				&& contrib.getIdCommunauteCommune() != null
				&& epciId.equals(contrib.getIdCommunauteCommune());
	}
}
