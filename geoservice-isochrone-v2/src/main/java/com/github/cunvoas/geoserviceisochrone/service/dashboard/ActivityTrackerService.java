package com.github.cunvoas.geoserviceisochrone.service.dashboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurAction;
import com.github.cunvoas.geoserviceisochrone.model.admin.Stats;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardLive;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardLiveItem;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurActionRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.StatsRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Use to have an image of the usage of the app.
 * @author cunvoas
 */
@Service
@Scope("singleton")
@Slf4j
public class ActivityTrackerService {
	private int HOUR_IN_DAY=24;
	
	@Autowired
	private StatsRepository statsRepository;
	@Autowired
	private ContributeurActionRepository contributeurActionRepository;
	
    private List<DashboardLiveItem> dayDashboardItems = new ArrayList<DashboardLiveItem>(HOUR_IN_DAY);
    protected Set<Long> uniqueUserId = new HashSet<Long>();
    
     
    /**
     * constructor.
     */
    public ActivityTrackerService() {
        for (int i = 0; i < HOUR_IN_DAY; i++) {
            dayDashboardItems.add(new DashboardLiveItem(i));
        }
        int rotateIdx = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Collections.rotate(dayDashboardItems, -rotateIdx);
    }
    
    /**
     * currentItem.
     * @return  DashboardLiveItem
     */
    private DashboardLiveItem currentItem() {
        return dayDashboardItems.get(0);
    }
    
    /**
     * changeHour.
     * executed every hour (H:00:00).
     */
    @Scheduled(cron="0 0 * * * *")
    public void changeHour() {
        Collections.rotate(dayDashboardItems, -1);
        currentItem().clear();
    }
    
    /**
     * changeDay.
     *  executed every day (0:00:01).
     */
    @Scheduled(cron="1 0 0 * * *")
    public void changeDay() {
        uniqueUserId.clear();
        for (DashboardLiveItem item : dayDashboardItems) {
            item.resetUniqueUser();
        }
    }

    /**
     * getActivityOfDay.
     * @return DashboardLive
     */
    public DashboardLive getActivityOfDay() {
        DashboardLive dayActivityDashboard = new DashboardLive();
        for (DashboardLiveItem item : dayDashboardItems) {
            dayActivityDashboard.add(item);
        }
        return dayActivityDashboard;
    }
    
    /**
     * getActivity.
     * @return list ContributeurAction
     */
    public List<ContributeurAction> getActivity() {
    	return contributeurActionRepository.findLastDays(10);
    }

    /**
     * getActivityByHour.
     * @return list DashboardLiveItem
     */
    public List<DashboardLiveItem> getActivityByHour() {
        return dayDashboardItems;
    }


    /**
     * incrementUniqueUser.
     * @param userId add user connected
     */
    public void incrementUniqueUser(Long userId) {
        if (!uniqueUserId.contains(userId)) {
            uniqueUserId.add(userId);
            currentItem().addNbUniqueUser();
        }
    }

    /**
     * incrementNbAdminActivity.
     * @param ca user action
     */
    public void incrementNbAdminActivity(ContributeurAction ca) {
        currentItem().addNbAdminActivity();
        this.addStat(Stats.EVT_ADMIN, ca);
    }
    /**
     * incrementNbEntranceActivity.
     * @param ca user action
     */
    public void incrementNbEntranceActivity(ContributeurAction ca) {
        currentItem().addNbEntranceActivity();
        this.addStat(Stats.EVT_ENTRANCE, ca);
    }
    /**
     * incrementNbParkActivity.
     * @param ca user action
     */
    public void incrementNbParkActivity(ContributeurAction ca) {
        currentItem().addNbParkActivity();
        this.addStat(Stats.EVT_PARK, ca);
    }
    /**
     * incrementNbIsochroneActivity.
     * @param ca user action
     */
    public void incrementNbIsochroneActivity(ContributeurAction ca) {
        currentItem().addNbIsochroneActivity();
        this.addStat(Stats.EVT_ISOCHRONE, ca);
    }
    
    /**
     * addStat.
     * @param action action
     * @param ca user action
     */
    private void addStat(String action, ContributeurAction ca) {
    	Long uid = getUserId();
    	if (uid!=null) {
    		Stats stat=new Stats(action);
    		stat.setUserId(uid);
        	statsRepository.save(stat);
        	
        	ContributeurAction dbCa=null;
        	Optional<ContributeurAction> opt = contributeurActionRepository.findById(uid);
        	if (opt.isPresent()) {
        		dbCa = opt.get();
        	} else {
        		dbCa = new ContributeurAction();
        		dbCa.setIdContributor(uid);
        		dbCa.setFirstDate(new Date());
        	}
        	dbCa.merge(ca);
        	contributeurActionRepository.save(dbCa);
        	
    	} else {
    		log.warn("user for found for tracking.");
    	}
    }
    
    /**
     * getUserId.
     * @return id user connectd
     */
    private Long getUserId() {
    	Long id=null;
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof Contributeur) {
			Contributeur contrib = (Contributeur) authentication.getPrincipal();
			id = contrib.getId();
		}
		return id; 
    }
    
}
