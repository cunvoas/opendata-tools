package com.github.cunvoas.geoserviceisochrone.service.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.extern.helper.EmailSender;
import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ContributeurService {
	
	@Autowired
	private ContributeurRepository contributeurRepository;
	@Autowired
	private PasswordService passwordService;

	@Autowired
	private EmailSender emailSender;
	
	public List<Contributeur> findAll() {
		return contributeurRepository.findAll();
	}
	
	public List<Contributeur> findByAssociation(Association asso) {
		if (asso!=null) {
			return contributeurRepository.findByAssociation(asso);
		} else {
			return new ArrayList<>(0);
		}
	}
	
	
	
	public Contributeur get(Long id) {
		Optional<Contributeur> oUser = contributeurRepository.findById(id);
		if (oUser.isPresent()) {
			return oUser.get();
		}
		return null;
	}
	

	public Contributeur getByEmail(String email) {
		Optional<Contributeur> oUser = contributeurRepository.findByEmail(email);
		if (oUser.isPresent()) {
			return oUser.get();
		}
		return null;
	}

	public Contributeur getByLogin(String login) {
		Optional<Contributeur> oUser = contributeurRepository.findByLogin(login);
		if (oUser.isPresent()) {
			return oUser.get();
		}
		return null;
	}
	
	public Contributeur save(Contributeur contributeur, boolean pwdGenNeeded) {
		boolean newAccount=false;
		boolean newPassword=false;
		String myPassword="";
		
		Contributeur toBeSaved = null;
		Optional<Contributeur> oUser = null;
		if (contributeur.getId()!=null) {
			oUser = contributeurRepository.findById(contributeur.getId());
		}
		
		if (oUser!=null && oUser.isPresent()) {
			toBeSaved = oUser.get();
			
			// change the password here
			if (StringUtils.isNotEmpty(contributeur.getPassword())) {
				toBeSaved.setPassword(
						passwordService.securizePassword(contributeur.getPassword())
					);
			}
			
		} else {
			newAccount=true;
			
			toBeSaved = contributeur;
			if (contributeurRepository.existsByEmail(toBeSaved.getEmail())) {
				throw new ExceptionAdmin(ExceptionAdmin.RG_EMAIL_EXISTS);
			}
			if (contributeurRepository.existsByLogin(toBeSaved.getLogin())) {
				throw new ExceptionAdmin(ExceptionAdmin.RG_LOGIN_EXISTS);
			}

			// force clean ID to ensure new one
			toBeSaved.setId(null);
			toBeSaved.setCreationDate(new Date());
			
			
			// generate only in creation
			if (!pwdGenNeeded) {
				
				if (!passwordService.isSafe(contributeur.getPassword())) {
					throw new ExceptionAdmin(ExceptionAdmin.RG_PWD_NOT_SAFE);
				}
				
				toBeSaved.setPassword(
						passwordService.securizePassword(contributeur.getPassword())
					);
			}
		}
		
		// generate in reset case
		if (pwdGenNeeded) {
			newPassword=true;
			String newPass = passwordService.generatePassword(20);
			myPassword = newPass;
			toBeSaved.setPassword(
					passwordService.securizePassword(newPass)
				);
		}
		
		toBeSaved.setUpdateDate(new Date());
		toBeSaved.setNom(contributeur.getNom());
		toBeSaved.setPrenom(contributeur.getPrenom());
		toBeSaved.setLogin(contributeur.getLogin());
		if (contributeur.getRole()!=null) {
			toBeSaved.setRole(contributeur.getRole());
		} else {
			toBeSaved.setRole(ContributeurRole.ASSOCIATION_CONSTRIBUTOR);
		}
		toBeSaved.setEmail(contributeur.getEmail());
		toBeSaved.setAssociation(contributeur.getAssociation());
		
		toBeSaved = contributeurRepository.save(toBeSaved);
		
		if (newAccount) {
			emailSender.sendWelcome(toBeSaved.getEmail(), toBeSaved.getFullName(), toBeSaved.getLogin());
		}
		
		if (newPassword) {
			log.error("send email with password");
			emailSender.sendPassword(toBeSaved.getEmail(), toBeSaved.getFullName(), myPassword);
		}
		
		// IMPORTANT clear password on memory
		// not works with open-in-view
		// toBeSaved.setPassword(null);
		return toBeSaved;
	}

}