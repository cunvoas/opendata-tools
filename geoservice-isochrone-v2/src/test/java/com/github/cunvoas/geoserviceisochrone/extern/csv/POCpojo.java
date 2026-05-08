package com.github.cunvoas.geoserviceisochrone.extern.csv;

import lombok.Data;

@Data
public class POCpojo {
	
	private String id;
	private String nom;
	private String geom;
	
	public String toString() {
		return  id+" "+nom;
	}
	
	public String toSqlUpdateByNom() {
		boolean withId=false;
		StringBuilder sb = new StringBuilder();
		sb.append("update public.parc_jardin set contour=ST_GeomFromText('");
		sb.append(this.geom);
		sb.append("')");
		if (withId) {
			sb.append(", id_source='");
			sb.append(this.id.trim().replaceAll("'", "''"));
			sb.append("'");
		}
		sb.append(" where nom_parc='");
		sb.append(this.nom.replaceAll("'", "''"));
		sb.append("';");
		return sb.toString();
	}
	
	public String toSqlUpdateById() {
		StringBuilder sb = new StringBuilder();
		sb.append("update public.parc_jardin set contour=ST_GeomFromText('");
		sb.append(this.geom);
		sb.append("') where id_source='");
		sb.append(this.id.trim().replaceAll("'", "''"));
		sb.append("';");
		return sb.toString();
	}
}
