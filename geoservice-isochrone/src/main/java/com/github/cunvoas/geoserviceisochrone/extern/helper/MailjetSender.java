package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.tools.EmailToContributor;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.Attachment;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TrackClicks;
import com.mailjet.client.transactional.TrackOpens;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailsResponse;

import okhttp3.OkHttpClient;


/**
 * @author cunvoas
 * @see https://github.com/mailjet/mailjet-apiv3-java
 * @see https://dev.mailjet.com/email/guides/send-api-v31/
 * @see https://github.com/mailjet/mailjet-apiv3-java#simple-post-request
 */
@Component
public class MailjetSender {

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	public EmailToContributor send(EmailToContributor mailToContrib) throws MailjetException, IOException {
        
        OkHttpClient customHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
		
		 ClientOptions options = ClientOptions.builder()
				.apiKey(applicationBusinessProperties.getMailjetToken())
				.apiSecretKey(applicationBusinessProperties.getMailjetSecret())
				.okHttpClient(customHttpClient)
                .build();

        MailjetClient client = new MailjetClient(options);
	    
        
        Attachment logo = Attachment.fromFile(mailToContrib.getLogoAutmel());
        logo.setContentID("logo");
        
        TransactionalEmail message1 = TransactionalEmail
                .builder()
                .from(new SendContact(applicationBusinessProperties.getMailjetSenderEmail(), applicationBusinessProperties.getMailjetSenderName()))
                .to(new SendContact(mailToContrib.getEmail(), mailToContrib.getName()))
                .subject(mailToContrib.getSubject())
                .htmlPart(mailToContrib.getMessage())
                // never track our supporters
                .trackOpens(TrackOpens.DISABLED)
                .trackClicks(TrackClicks.DISABLED)
                .inlinedAttachment(logo)
//                .attachment(Attachment.fromFile(mailToContrib.getLogoAutmel()))
//                .header("test-header-key", "test-value")
//                .customID("custom-id-value")
                .build();

        SendEmailsRequest request = SendEmailsRequest
                .builder()
                .message(message1) // you can add up to 50 messages per request
                .build();

        // act
        SendEmailsResponse response = request.sendWith(client);
        mailToContrib.setStatus(response.getMessages()[0].getStatus().name());
        
        return mailToContrib;
	}
	
	
	/**
	 * put the file attached.
	 * @param filePath path to file
	 * @return json structure
	 * @throws JSONException ex
	 * @throws IOException ex
	 */
	private static JSONObject getAttachement(String filePath) throws JSONException, IOException {
		JSONObject attachement=null;
		File file = new File(filePath);
		if (file.exists()) {
			attachement=new JSONObject();
			attachement.put("ContentType", getContentType(file));
			attachement.put("Filename", file.getName());
			attachement.put("Base64Content", encoder(file));
		}
		return attachement;
	}
	
	
	/**
	 * Get file content type.
	 * @param file to analyse
	 * @return content type
	 * @throws IOException
	 */
	private static String getContentType(File file) throws IOException {		
		Path path = file.toPath();
		String contentType = Files.probeContentType(path);		
		return contentType;
	}
	
	/**
	 * tranform binary file to base64.
	 * @param file to encode
	 * @return base64 file content
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static String encoder(File file) throws FileNotFoundException, IOException  {
		String base64File = "";
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a file from file system
        	int len = (int)file.length();
            byte fileData[] = new byte[len];
            imageInFile.read(fileData);
            base64File = Base64.getEncoder().encodeToString(fileData);
        }
        return base64File;
	}
	
}
