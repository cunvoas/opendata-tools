package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
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

/**
 * Composant utilitaire pour l'envoi d'e-mails.
 * Permet notamment l'envoi de mots de passe et la gestion des modèles de messages.
 */
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
	
	/**
	 * Récupère le chemin d'un fichier, y compris depuis un JAR en production.
	 * @param file nom du fichier à récupérer
	 * @return chemin du fichier sous forme de Path
	 * @throws URISyntaxException si l'URI est invalide
	 * @throws IOException si une erreur d'accès survient
	 */
	protected Path getFilePath(String file) throws URISyntaxException, IOException {
		URI uri = getClass().getClassLoader().getResource(file).toURI();

		if("jar".equals(uri.getScheme())){
		    for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
		        if (provider.getScheme().equalsIgnoreCase("jar")) {
		            try {
		                provider.getFileSystem(uri);
		            } catch (FileSystemNotFoundException e) {
		                // in this case we need to initialize it first:
		                provider.newFileSystem(uri, Collections.emptyMap());
		            }
		        }
		    }
		}
		return  Paths.get(uri);
	}

	/**
	 * Envoie un e-mail contenant un mot de passe à un utilisateur.
	 * @param email adresse e-mail du destinataire
	 * @param prenomNom nom complet du destinataire
	 * @param password mot de passe à envoyer
	 * @param login identifiant de connexion
	 */
	public void sendPassword(String email, String prenomNom, String password, String login) {
		if (!toogleFeatureEmail) {
			log.warn("skip email sendPassword");
			return;
		}
		
		try {
			
			Map<String, String> values = new HashMap<String, String>(5);
			values.put("@nom_prenom@", prenomNom);
			values.put("@login@", login);
			values.put("@password@", password);
			values.put("@siteGestion@", applicationBusinessProperties.getSiteGestion());
			values.put("@sitePublic@", applicationBusinessProperties.getSitePublique());

			String tpl = readTemplate("passwordByAdmin.htm");
			String msg = applyData(tpl, values);
			
			File f = ResourceUtils.getFile(applicationBusinessProperties.getMailjetAttachementPath()+"mailjet/logo-autmel.png");
			String logoFile = Paths.get(f.getAbsolutePath()).toString();
			
			EmailToContributor toSend = new EmailToContributor();
			toSend.setEmail(email);
			toSend.setSubject("AUT'MEL Votre mot de passe a été réinitialisé");
			toSend.setMessage(msg);
			toSend.setLogoAutmel(logoFile);
			
			mailjetSender.send(toSend);
			
		} catch (FileNotFoundException e) {
			log.error("logo not found");
			throw new ExceptionAdmin("logo not found");
		} catch (IOException e) {
			log.error("logo not found");
			throw new ExceptionAdmin("logo not found");
		} catch (MailjetException e) {
			log.error("MailjetException", e);
			throw new ExceptionAdmin("Mailjet error: "+e.getMessage());
		}
	}
	
	public void sendPassword(String email, String prenomNom, String password) {
		if (!toogleFeatureEmail) {
			log.warn("skip email sendPassword");
			return;
		}
		try {
			
			// https://stackoverflow.com/questions/11913709/why-does-replaceall-fail-with-illegal-group-reference
			// FIXME trouble with $ in pass
			Map<String, String> values = new HashMap<String, String>(4);
			values.put("@nom_prenom@", prenomNom);
			values.put("@password@", password);
			
			String tpl = readTemplate("password.htm");
			String msg = applyData(tpl, values);
			
			File f = ResourceUtils.getFile(applicationBusinessProperties.getMailjetAttachementPath()+"mailjet/logo-autmel.png");
			String logoFile = Paths.get(f.getAbsolutePath()).toString();
			
			EmailToContributor toSend = new EmailToContributor();
			toSend.setEmail(email);
			toSend.setSubject("AUT'MEL Votre mot de passe");
			toSend.setMessage(msg);
			toSend.setLogoAutmel(logoFile);
			
			mailjetSender.send(toSend);
			
		} catch (FileNotFoundException e) {
			log.error("logo not found");
			throw new ExceptionAdmin("logo not found");
		} catch (IOException e) {
			log.error("logo not found");
			throw new ExceptionAdmin("logo not found");
		} catch (MailjetException e) {
			log.error("MailjetException", e);
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

			File f = ResourceUtils.getFile(applicationBusinessProperties.getMailjetAttachementPath()+"mailjet/logo-autmel.png");
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
		log.info("mailjet/"+tpl);
		try {
			Path theTpl = this.getFilePath("mailjet/"+tpl);
			
			byte[] bytes = Files.readAllBytes(theTpl);
			return new String(bytes, Charset.forName("UTF8"));
			
		} catch (FileNotFoundException e) {
			log.error("template not found", e);
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		} catch (IOException e) {
			log.error("template not readable", e);
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		} catch (URISyntaxException e) {
			log.error("template not readable", e);
			throw new ExceptionAdmin("ERR_EMAIL_ERROR");
		}
	}
	
	/**
	 * @param template
	 * @param values
	 * @return
	 */
	public String applyData(String template, Map<String, String> values) {
		// make a clone
		String ret = String.valueOf(template);
		for (Entry<String, String> entry : values.entrySet()) {
			ret = ret.replace(entry.getKey(), entry.getValue());
		}
		return ret;
	}
	
	

}