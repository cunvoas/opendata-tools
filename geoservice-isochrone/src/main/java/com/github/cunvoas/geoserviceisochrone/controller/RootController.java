package com.github.cunvoas.geoserviceisochrone.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Contrôleur principal pour la gestion des pages racines et des vérifications de santé.
 * Fournit les routes pour la page d'accueil, la connexion, la déconnexion, la page d'erreur et le healthcheck.
 */
@Controller
public class RootController {

	private static final Pattern PATTERN = Pattern.compile(
			"(^127\\.0\\.0\\.1)|(^10\\.)|(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^192\\.168\\.)");
	
	/**
	 * Redirige la racine vers le tableau de bord.
	 * @return redirection vers /mvc/dashboard
	 */
	@GetMapping("/")
	public String root() {
		return "redirect:/mvc/dashboard";
	}
	
	/** Login form. 
	 * Affiche le formulaire de connexion.
	 * @return page de connexion
	 */
	@GetMapping(value="/login")
	public String login() {
		return "login";
	}
	
	/**
	 * Logout form.
	 * Affiche le formulaire de déconnexion.
	 * @return page de connexion
	 */
	@GetMapping(value="/logout")
	public String logout() {
		return "login";
	}

	/** Error page. 
	 * Affiche la page d'erreur 403.
	 * @return page d'erreur 403
	 */
	@GetMapping("/403.html")
	public String forbidden() {
		return "403";
	}

	/**
	 * home page.
	 * Affiche la page d'accueil HTML simple.
	 * @return page HTML d'accueil
	 */
	@GetMapping("/home")
	public ResponseEntity<String> index() {
		return new ResponseEntity<>("<html><body><p>AUT'MEL</p></body></html>", HttpStatus.OK);
	}

	/**
	 * Health check.
	 * Endpoint de vérification de santé (health check).
	 * Retourne 200 OK si l'adresse IP est autorisée, sinon 403 Forbidden.
	 *
	 * @param request Requête HTTP
	 * @return code HTTP selon l'adresse IP
	 */
	@RequestMapping(value = "/awake", method = RequestMethod.HEAD)
	public ResponseEntity<Void> awake(HttpServletRequest request) {

		String address = request.getRemoteAddr();
		final String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader != null && !xfHeader.isEmpty()) {
			address = xfHeader.split(",")[0];
		}

		Matcher matcher = PATTERN.matcher(address);
		if (matcher.matches()) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}
	

}