package com.github.cunvoas.geoserviceisochrone.service.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkEntranceDetail;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurAction;
import com.github.cunvoas.geoserviceisochrone.service.dashboard.ActivityTrackerService;

/**
 * Aspect for tracking of administrator save activity.
 * @author CUNVOAS
 */
@Aspect
@Component
public class AopSaveTracker {

	@Autowired
    private ActivityTrackerService activityTrackerService;
    
    @After("execution( * com.github.cunvoas.geoserviceisochrone.controller.mvc.admin.*.save*(..) )")
    public void trackMethodAfterAdmin(JoinPoint joinPoint) {
    	ContributeurAction ca = extractArg(joinPoint);
        activityTrackerService.incrementNbAdminActivity(ca);
    }
    
    @After("execution( * com.github.cunvoas.geoserviceisochrone.controller.mvc.park.*.save*(..) )")
    public void trackMethodAfterPark(JoinPoint joinPoint) {
    	ContributeurAction ca = extractArg(joinPoint);
        activityTrackerService.incrementNbParkActivity(ca);
    }
    
    @After("execution( * com.github.cunvoas.geoserviceisochrone.controller.mvc.entrance.EntranceControler.saveEntrance(..) )")
    public void trackMethodAfterEntrance(JoinPoint joinPoint) {
    	ContributeurAction ca = extractArg(joinPoint);
        activityTrackerService.incrementNbEntranceActivity(ca);
    }

    @After("execution( * com.github.cunvoas.geoserviceisochrone.controller.mvc.*.*Controler.mergeIsochrone(..) )")
    public void trackMethodAfterIsochrone(JoinPoint joinPoint) {
    	ContributeurAction ca = extractArg(joinPoint);
        activityTrackerService.incrementNbIsochroneActivity(ca);;
    }
    
    private ContributeurAction extractArg(JoinPoint joinPoint) {
    	int pos=0;
    	ContributeurAction ca =new ContributeurAction();
    	
    	if (!Arrays.isNullOrEmpty(joinPoint.getArgs())
    		&& joinPoint.getArgs()[pos] instanceof FormParkEntranceDetail) {
    		
    			FormParkEntranceDetail frm = (FormParkEntranceDetail)joinPoint.getArgs()[pos] ;
    			if (frm.getEntranceId()!=null) {
    				ca.setNbEntranceUpd(1L);
    			} else {
    				ca.setNbEntranceAdd(1L);
    			}
    		
    	}
    	return ca;
    }

}
