package com.github.cunvoas.geoserviceisochrone.model.dashboard;

import lombok.Data;

@Data
public class DashboardLiveItem {

    // monitor segment (hour)
    private int segment;
    
    private long nbUniqueUser;
    
    private long nbParcActivity;
    private long nbEntranceActivity;
    private long nbIsochroneActivity;
    private long nbAdminActivity;
    
    public DashboardLiveItem(int segment) {
        this.segment=segment;
    }
    
    public void resetUniqueUser() {
        nbUniqueUser=0;
    }
    
    public void clear() {
    	nbUniqueUser=0;
    	nbParcActivity=0;
    	nbEntranceActivity=0;
    	nbIsochroneActivity=0;
        nbAdminActivity=0;
    }

    public void addNbUniqueUser() {
        this.nbUniqueUser++;
    }
    public void addNbParkActivity() {
        this.nbParcActivity++;
    }
    
    public void addNbEntranceActivity() {
        this.nbEntranceActivity++;
    }
    
    public void addNbIsochroneActivity() {
        this.nbIsochroneActivity++;
    }
    
    public void addNbAdminActivity() {
        this.nbAdminActivity++;
    }
    

   
    
}
