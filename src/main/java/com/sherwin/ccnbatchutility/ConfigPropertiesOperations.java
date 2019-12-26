package com.sherwin.ccnbatchutility;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sherwin.cyberarkvault.CyberArkCcpHttpRestAPI;
import com.sherwin.cyberarkvault.CyberArkCcpHttpRestAPIException;
import com.sherwin.cypher.CryptographUtil;

/**
 * <h1>ConfigPropertiesOperations</h1>
 * The ConfigPropertiesOperations program is implemented to maintain all configuration related details
 * <p>
 *
 * @author  Jaydeep Cheruku (jxc517)
 * @version 1.0
 * @since   02-Dec-2019
 */

public class ConfigPropertiesOperations {
	private static Log log = LogFactory.getLog(ConfigPropertiesOperations.class);

	public static Properties prop = new Properties();
	public static String executingEnvironment;
	public static String executingDatabaseUser;

	public static Properties propJobs = new Properties();
	public static String executingJob;

	public static String p12Password = "";
	public static String ksPassword = "";

	/**
	 * <h1>load</h1>
	 * This method is core for the entire process as it loads all required data to run a particular job using config/property files
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void load() throws IOException, CyberArkCcpHttpRestAPIException, SQLException{
		log.info("Configuring properties in ConfigFileProperties.load() -> "+"resources/"+executingEnvironment+".properties");
		//		prop.load(new FileInputStream("resources/"+executingEnvironment+".properties"));
		//		prop.load(ConfigPropertiesOperations.class.getResourceAsStream("/resources/"+executingEnvironment+".properties"));
		prop.load(ClassLoader.getSystemClassLoader().getResourceAsStream(executingEnvironment+".properties"));
		log.info("Configuring properties in ConfigFileProperties.load() -> "+"resources/"+executingEnvironment+"-job.details => "+executingJob);
		//		propJobs.load(new FileInputStream("resources/"+executingEnvironment+"-job.details"));
		//		propJobs.load(ConfigPropertiesOperations.class.getResourceAsStream("/resources/"+executingEnvironment+"-job.details"));
		propJobs.load(ClassLoader.getSystemClassLoader().getResourceAsStream(executingEnvironment+"-job.details"));
		log.info("Retrieving cyber ark cypher files credentials from respective servers");
		try {
			p12Password = CryptographUtil.decryptPassword(
					prop.getProperty("cyberArk.p12FilesPath")+prop.getProperty("cyberArk.p12FileData"),
					prop.getProperty("cyberArk.p12FilesPath")+prop.getProperty("cyberArk.p12FileKey"));
			ksPassword = CryptographUtil.decryptPassword(
					prop.getProperty("cyberArk.ksFilesPath")+prop.getProperty("cyberArk.ksFileData"),
					prop.getProperty("cyberArk.ksFilesPath")+prop.getProperty("cyberArk.ksFileKey"));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String dbUtilityUserName = prop.getProperty("CCN_UTILITY.dbuser");
		String dbConnection = prop.getProperty("dbconn");
		String dbPassword = getPasswordFromVault(dbUtilityUserName, dbConnection.split(":")[0]);
		DatabaseOperations.setUtilityConnection(dbUtilityUserName, dbPassword, dbConnection);
		log.info("Configuring database properties in DatabaseOperations.loadDBDetails()");
		DatabaseOperations.loadDBDetails();
		log.info("Configuring E-Mail properties in EmailOperations.loadMailingDetails()");
		EmailOperations.loadMailingDetails();
		log.info("Configuring FTP properties in FileOperations.loadFTPDetails()");
		FileOperations.loadFTPDetails();
		log.info("Configuring Dataload properties in DataLoadOperations.loadDataloadFileDetails()");
		DataLoadOperations.loadDataloadFileDetails();
	}

	/**
	 * <h1>getPasswordFromVault</h1>
	 * This method is critical in retreiving the password for provided database/servers that are stored inside CyberArk vault
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static String getPasswordFromVault(String userName, String address) throws CyberArkCcpHttpRestAPIException {
		return CyberArkCcpHttpRestAPI.getPasswordFromVault(
				prop.getProperty("cyberArk.ccpName"),
				prop.getProperty("cyberArk.vaultName"), 
				userName,
				address,
				prop.getProperty("cyberArk.p12FilesPath")+prop.getProperty("cyberArk.p12FileName"), 
				p12Password, 
				prop.getProperty("cyberArk.ksFilesPath")+prop.getProperty("cyberArk.ksFileName"),
				ksPassword 
				);		
	}
}