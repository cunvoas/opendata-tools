package com.github.cunvoas.geoserviceisochrone.util;

import java.util.HashMap;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;

public class RoleHelper {
	
	private static Map<ContributeurRole, Integer> MAP_ROLE=new HashMap<>();
	static {
		MAP_ROLE.put(ContributeurRole.ADMINISTRATOR, 10);
		MAP_ROLE.put(ContributeurRole.ASSOCIATION_MANAGER, 5);
		MAP_ROLE.put(ContributeurRole.ASSOCIATION_CONSTRIBUTOR, 4);
		MAP_ROLE.put(ContributeurRole.SUPPORT, 2);
		MAP_ROLE.put(ContributeurRole.JOURNALIST, 1);
	}
	
	
	public Boolean atLeast(ContributeurRole roleRequired, Contributeur contributeur) {
		Integer pos=Integer.MAX_VALUE;
		Integer use=Integer.MIN_VALUE;
		
		if (roleRequired!=null) {
			pos = MAP_ROLE.get(roleRequired);
		}
		if (contributeur!=null && contributeur.getRole()!=null) {
			use = MAP_ROLE.get(contributeur.getRole());
		}
		return use>=pos;
	}
	
	public Boolean exact(ContributeurRole roleRequired, Contributeur contributeur) {
		return roleRequired!=null && roleRequired.equals(contributeur.getRole());
	}
	
}
