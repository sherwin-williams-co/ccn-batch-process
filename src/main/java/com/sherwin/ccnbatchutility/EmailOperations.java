package com.sherwin.ccnbatchutility;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h1>EmailOperations</h1>
 * The EmailOperations program is implemented to maintain/handle all mailing operations based on mailing category 
 * <p>
 *
 * @author  Jaydeep Cheruku (jxc517)
 * @version 1.0
 * @since   02-Dec-2019
 */
public class EmailOperations{
	private static Log log = LogFactory.getLog(EmailOperations.class);
	
	public static String isMailNeeded;
	public static String mailCategory;
	public static String from;
	public static String to;
	public static String subject;
	public static String body;
	public static String signature;

	/**
	 * <h1>loadMailingDetails</h1>
	 * This method loads all the mailing related details needed for the job based on job configuration details
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadMailingDetails(){
		String executingJob = ConfigPropertiesOperations.executingJob;
		Properties propJobs = ConfigPropertiesOperations.propJobs;
		isMailNeeded = propJobs.getProperty(executingJob+".mailing.required");
		if (isMailNeeded.equalsIgnoreCase("Y")){
			mailCategory = propJobs.getProperty(executingJob+".mailing.category");
			DatabaseOperations.setMailingInfo(mailCategory);
		}
	}
	/**
	 * <h1>execute</h1>
	 * This method is core in sending email to respetive parties
	 * Anticipate to expand this based on different kinds of emails that we are currently sending 
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void execute(){
		Properties configProps = ConfigPropertiesOperations.prop;
		String executingEnvironment = ConfigPropertiesOperations.executingEnvironment;
		Properties props = new Properties();
		//	      props.put("mail.smtp.auth", "true");
		//	      props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", configProps.getProperty("mailHost"));
		props.put("mail.smtp.port", configProps.getProperty("mailPort"));
		Session session = Session.getDefaultInstance(props);
		try {
			// Create a default MimeMessage object.
			Message message = new MimeMessage(session);
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));
			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			// Set Subject: header field
			message.setSubject(executingEnvironment+" : "+subject);
			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();
			// Now set the actual message
			messageBodyPart.setText(body+"\n\n"+signature);
			// Create a multipar message
			Multipart multipart = new MimeMultipart();
			// Set text message part
			multipart.addBodyPart(messageBodyPart);
			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			String filename = configProps.getProperty("defaultDataPath") + FileOperations.fileName;
			DataSource source = new FileDataSource(filename);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(FileOperations.fileName);
			multipart.addBodyPart(messageBodyPart);
			// Send the complete message parts
			message.setContent(multipart);
			// Send message
			Transport.send(message);
			log.info("Sent message successfully....");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}