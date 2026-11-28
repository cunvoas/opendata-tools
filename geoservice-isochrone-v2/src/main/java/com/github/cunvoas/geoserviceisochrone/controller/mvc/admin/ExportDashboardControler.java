package com.github.cunvoas.geoserviceisochrone.controller.mvc.admin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.admin.AccessScopeService;
import com.github.cunvoas.geoserviceisochrone.service.export.dashboard.ExportDashboard;
import com.github.cunvoas.geoserviceisochrone.service.export.dashboard.ExportDashboardService;

import lombok.extern.slf4j.Slf4j;

/**
 * Dashboard d'état des lieux des exports JSON/GeoJSON.
 * Le commit final est déclenché depuis cette page via /mvc/ajax/publish/api/commit.
 */
@Controller
@RequestMapping("/mvc/management/exports")
@Slf4j
public class ExportDashboardControler {

	private final ExportDashboardService dashboardService;
	private final TokenManagement tokenManagement;
	private final AccessScopeService accessScopeService;
	private final CommunauteCommuneRepository communauteCommuneRepository;
	private final ApplicationBusinessProperties applicationBusinessProperties;

	public ExportDashboardControler(ExportDashboardService dashboardService, TokenManagement tokenManagement,
			AccessScopeService accessScopeService, CommunauteCommuneRepository communauteCommuneRepository,
			ApplicationBusinessProperties applicationBusinessProperties) {
		this.dashboardService = dashboardService;
		this.tokenManagement = tokenManagement;
		this.accessScopeService = accessScopeService;
		this.communauteCommuneRepository = communauteCommuneRepository;
		this.applicationBusinessProperties = applicationBusinessProperties;
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ADMINISTRATOR','ASSO_MANAGER')")
	public String dashboard(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) Long epciId,
			@RequestParam(required = false) Integer year,
			Model model) {
		Contributeur contrib = accessScopeService.current();
		if (contrib == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Contributeur non identifié.");
		}
		Long scope = accessScopeService.scopedEpciId(contrib);
		if (accessScopeService.isManager(contrib) && scope == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun EPCI associé à ce compte gestionnaire.");
		}
		if (scope != null && epciId != null && !scope.equals(epciId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "EPCI hors périmètre.");
		}
		Long effectiveEpci = scope != null ? scope : epciId;
		Integer[] configuredYears = applicationBusinessProperties.getInseeAnnees();
		Integer effectiveYear = null;
		if (year != null) {
			boolean allowed = configuredYears != null
					&& Arrays.stream(configuredYears).anyMatch(year::equals);
			if (!allowed) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Année non configurée.");
			}
			effectiveYear = year;
		}

		int pageSize = (size == 10 || size == 50 || size == 100) ? size : 10;
		int safePage = Math.max(0, page);
		ExportDashboard dashboard = dashboardService.scan(effectiveEpci, safePage, pageSize, effectiveYear);
		long totalPages = dashboard.getTotalEpcis() == 0 ? 1
				: (dashboard.getTotalEpcis() + pageSize - 1) / pageSize;

		List<CommunauteCommune> epciOptions;
		if (scope != null) {
			epciOptions = communauteCommuneRepository.findById(scope).map(List::of).orElse(List.of());
		} else {
			epciOptions = communauteCommuneRepository.findAll().stream()
					.sorted(Comparator.comparing(CommunauteCommune::getName,
							Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
					.toList();
		}

		model.addAttribute("dashboard", dashboard);
		model.addAttribute("page", dashboard.getPage());
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("epciId", effectiveEpci);
		model.addAttribute("year", effectiveYear);
		model.addAttribute("epcis", epciOptions);
		model.addAttribute("years", configuredYears == null ? List.of() : Arrays.asList(configuredYears));
		model.addAttribute("token", tokenManagement.getValidToken());
		return "exportDashboard";
	}
}
