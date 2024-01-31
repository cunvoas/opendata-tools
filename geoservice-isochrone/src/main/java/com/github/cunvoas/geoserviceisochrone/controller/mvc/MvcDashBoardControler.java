package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardSummary;
import com.github.cunvoas.geoserviceisochrone.service.dashboard.DashboadService;


@Controller
@RequestMapping("/mvc/dashboard")
public class MvcDashBoardControler {
	
	@Autowired
	private DashboadService dashboadService;
	
	@GetMapping
    public String dashboard(Model model, ModelAndView modelAndView) {
		
		DashboardSummary data = dashboadService.getDashboard();
		model.addAttribute("dashboard", data);
		
        return "dashboard";
    }
}
