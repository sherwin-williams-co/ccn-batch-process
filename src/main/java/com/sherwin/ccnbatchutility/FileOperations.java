package com.sherwin.ccnbatchutility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.sherwin.cyberarkvault.CyberArkCcpHttpRestAPIException;

/**
 * <h1>FileOperations</h1>
 * The FileOperations program is implemented to maintain/handle all OS file related operations 
 * <p>
 *
 * @author  Jaydeep Cheruku (jxc517)
 * @version 1.0
 * @since   02-Dec-2019
 */
public class FileOperations {
	private static Log log = LogFactory.getLog(FileOperations.class);
	
	public static String isFTPNeeded;
	public static String ftpServerName;
	public static String ftpUserName;
	public static String ftpPassword;
	public static String ftpDestFilePath;
	public static String ftpDestFileName;
	public static String fileName = null;
	public static String clobData = null;

	/**
	 * <h1>loadFTPDetails</h1>
	 * This method loads all the FTP related details needed for the job based on job configuration details
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadFTPDetails() throws CyberArkCcpHttpRestAPIException{
		String executingJob = ConfigPropertiesOperations.executingJob;
		Properties propJobs = ConfigPropertiesOperations.propJobs;
		isFTPNeeded = propJobs.getProperty(executingJob+".ftp.required");
		if (isFTPNeeded.equalsIgnoreCase("Y")){
			ftpServerName = propJobs.getProperty(executingJob+".ftp.serverName");
			ftpUserName = propJobs.getProperty(executingJob+".ftp.user");
			ftpPassword = ConfigPropertiesOperations.getPasswordFromVault(ftpUserName, ftpServerName);
			ftpDestFilePath = propJobs.getProperty(executingJob+".ftp.path");
			ftpDestFileName = propJobs.getProperty(executingJob+".ftp.fileName")
					              .replace("YYYYMMDDHHMISS",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
					              .replace("YYYYMMDD",new SimpleDateFormat("yyyyMMdd").format(new Date()));
		}
	}
	/**
	 * <h1>transferFile</h1>
	 * This method transfers the file using sftp connection and respective credentials
	 * NOTE: Eventually we need to avoid using other systems password by setting up ssh connection between interactive servers and connect without passwords
	 * Please also note that this process internally connects to CyberArk to retreive the server credentials 
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void transferFile(String filePath){
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(ftpUserName, ftpServerName, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(ftpPassword);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.put(filePath+fileName, ftpDestFilePath+ftpDestFileName);
			sftpChannel.disconnect();
			session.disconnect();
		} catch (JSchException e) {
			log.error(e.getMessage());
		} catch (SftpException e) {
			log.error(e.getMessage());
		}
	}
	/**
	 * <h1>setLocalFileModficationTime</h1>
	 * This method can modify the file meta data and is not currently used but written anticipating a need for one
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void setLocalFileModficationTime(SftpATTRS attrs, String filePath) {
		SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
		try {
			Date modDate = (Date) format.parse(attrs.getMtimeString());
			File downloadedFile = new File(filePath);
			downloadedFile.setLastModified(modDate.getTime());
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
	}
	/**
	 * <h1>writeToFile</h1>
	 * This method writes the clob data into the requested file on server
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void writeToFile(String filePath) {
		Writer wrtr = null;
		try {
			File statText = new File(filePath+fileName);
			FileOutputStream inputStream = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(inputStream);
			wrtr = new BufferedWriter(osw);
			wrtr.write(clobData);
			DatabaseOperations.loadBatchJobInterfaceDetails("OUT-BOUND", clobData, filePath+fileName);
		} catch (Exception e) {
			log.error(e.getMessage());
		}finally{
			try {
				wrtr.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}

}
