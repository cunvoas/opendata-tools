package com.github.cunvoas.geoserviceisochrone.model.dashboard;

import lombok.Data;

@Data
public class DashboardLive {
    
    // unique user 0:00 > 23:59
	private long nbUniqueUser;
	 
    private long nbParcActivity;
    private long nbEntranceActivity;
    private long nbIsochroneActivity;
    private long nbAdminActivity;

    
    public void add(DashboardLiveItem item) {
        this.nbUniqueUser += item.getNbUniqueUser();
        
        this.nbAdminActivity += item.getNbAdminActivity();
        this.nbParcActivity += item.getNbParcActivity();
        this.nbIsochroneActivity += item.getNbIsochroneActivity();
        this.nbEntranceActivity += item.getNbEntranceActivity();
    }

}
    
