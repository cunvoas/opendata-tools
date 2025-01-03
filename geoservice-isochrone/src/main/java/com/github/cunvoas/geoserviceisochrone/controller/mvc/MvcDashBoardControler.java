package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurAction;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardLive;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardLiveItem;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardSummary;
import com.github.cunvoas.geoserviceisochrone.service.admin.ContributeurService;
import com.github.cunvoas.geoserviceisochrone.service.dashboard.ActivityTrackerService;
import com.github.cunvoas.geoserviceisochrone.service.dashboard.DashboadService;


@Controller
@RequestMapping("/mvc")
public class MvcDashBoardControler {
	
	@Autowired
	private DashboadService dashboadService;
	
	@Autowired
    private ActivityTrackerService activityTrackerService;

	@Autowired
	private ContributeurService contributeurService;
	
	@GetMapping("/infos")
    public String infos(Model model, ModelAndView modelAndView) {
        return "infos";
    }

	@GetMapping("/release-note")
    public String releaseNote(Model model, ModelAndView modelAndView) {
        return "releaseNote";
    }
	
	@GetMapping("/infosante")
    public String infosante(Model model, ModelAndView modelAndView) {
        return "infosante";
    }
	
	
	@GetMapping("/dashboard")
    public String dashboard(Model model, ModelAndView modelAndView) {
		
		DashboardSummary data = dashboadService.getDashboard();
		model.addAttribute("dashboard", data);
		
        return "dashboard";
    }
	
	@GetMapping("/logs")
    public String logs(Model model, ModelAndView modelAndView) {
		
		DashboardLive dLogs =	activityTrackerService.getActivityOfDay();
		model.addAttribute("dLogs", dLogs);
		
		List<DashboardLiveItem> hLogs = activityTrackerService.getActivityByHour();
		model.addAttribute("hLogs", hLogs);
		
		List<ContributeurAction> uLogs = activityTrackerService.getActivity();
		for (ContributeurAction ca : uLogs) {
			ca.setNomContributeur(
					contributeurService.get(ca.getIdContributor()).getFullName()
			);
		}
		model.addAttribute("uLogs", uLogs);
		
        return "logs";
    }
}
