package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.tools.EmailToContributor;
import com.mailjet.client.errors.MailjetException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailSender {

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Value("${application.feature-flipping.sendEmail}")
	private boolean toogleFeatureEmail=false;
	
	@Autowired
	private MailjetSender mailjetSender;
	public void setMailjetSender(MailjetSender mailjetSender) {
		this.mailjetSender = mailjetSender;
	}

	public void sendPassword(String email, String prenomNom, String password) {
		if (!toogleFeatureEmail) {
			log.warn("skip email sendPassword");
			return;
		}
		try {
			Map<String, String> values = new HashMap<String, String>(4);
			values.put("@nom_prenom@", prenomNom);
			values.put("@password@", password);
			
			String tpl = readTemplate("password.htm");
			String msg = applyData(tpl, values);
			
			File f = ResourceUtils.getFile("classpath:mailjet/logo-autmel.png");
			String logoFile = Paths.get(f.getAbsolutePath()).toString();
			
			
			EmailToContributor toSend = new EmailToContributor();
			toSend.setEmail(email);
			toSend.setSubject("AUT'MEL Votre mot de passe");
			toSend.setMessage(msg);
			toSend.setLogoAutmel(logoFile);
			
			mailjetSender.send(toSend);
			
		} catch (FileNotFoundException e) {
			throw new ExceptionAdmin("logo not found");
		} catch (IOException e) {
			throw new ExceptionAdmin("logo inaccessible");
		} catch (MailjetException e) {
			throw new ExceptionAdmin("Mailjet error: "+e.getMessage());
		}
	}
	
	public void sendWelcome(String email, String prenomNom, String login) {
		if (!toogleFeatureEmail) {
			log.warn("skip email sendWelcome");
			return;
		}
		try {
			Map<String, String> values = new HashMap<String, String>(4);
			values.put("@nom_prenom@", prenomNom);
			values.put("@login@", login);
			values.put("@siteGestion@", applicationBusinessProperties.getSiteGestion());
			values.put("@sitePublic@", applicationBusinessProperties.getSitePublique());
			
			String tpl = readTemplate("welcome.htm");
			String msg = applyData(tpl, values);
			
			File f = ResourceUtils.getFile("classpath:mailjet/logo-autmel.png");
			String logoFile = Paths.get(f.getAbsolutePath()).toString();
			
			
			EmailToContributor toSend = new EmailToContributor();
			toSend.setEmail(email);
			toSend.setSubject("AUT'MEL Bienvenue sur l'outil isochrone");
			toSend.setMessage(msg);
			toSend.setLogoAutmel(logoFile);
			
			mailjetSender.send(toSend);
			
		} catch (FileNotFoundException e) {
			log.error("logo not found", e);
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		} catch (IOException e) {
			log.error("logo inaccessible", e);
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		} catch (MailjetException e) {
			log.error("Mailjet error", e.getMessage());
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		}
	}
	
	
	private String readTemplate(String tpl) {
		try {
			File f = ResourceUtils.getFile("classpath:mailjet/"+tpl);
			byte[] bytes = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
			return new String(bytes, Charset.forName("UTF8"));
			
		} catch (FileNotFoundException e) {
			log.error("template not found", e);
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		} catch (IOException e) {
			log.error("template not readable", e);
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		}
	}
	
	/**
	 * @param template
	 * @param values
	 * @return
	 * @deprecated
	 * @see https://stackoverflow.com/questions/25796111/java-ee-method-to-replace-dollar-sign-variables-with-custom-string
	 */
	private String applyData(String template, List<String> values) {
		String message=new String(template);
		message = MessageFormat.format(message, values.toArray());
		return message;
	}
	
	public String applyData(String template, Map<String, String> values) {
		String ret = new String(template);
		for (Entry<String, String> entry : values.entrySet()) {
			ret = ret.replaceAll(entry.getKey(), entry.getValue());
		}
		return ret;
	}
	
	

}
