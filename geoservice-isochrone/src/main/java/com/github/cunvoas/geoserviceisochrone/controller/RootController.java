package com.github.cunvoas.geoserviceisochrone.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class RootController {

	private static final Pattern PATTERN = Pattern.compile(
			"(^127\\.0\\.0\\.1)|(^10\\.)|(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^192\\.168\\.)");
	
	@RequestMapping("/")
	public String root() {
		return "redirect:/mvc/dashboard";
	}
	
	// Login form
	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	
	// Logout form
	@RequestMapping("/logout")
	public String logout() {
		return "login";
	}

	/** Error page. */
	@RequestMapping("/403.html")
	public String forbidden() {
		return "403";
	}

	@RequestMapping("/home")
	public ResponseEntity<String> index() {
		return new ResponseEntity<>("<html><body><p>AUT'MEL</p></body></html>", HttpStatus.OK);
	}

	/**
	 * Health check.
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