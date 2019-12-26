package com.sherwin.ccnbatchutility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.csvreader.CsvReader;

/**
 * <h1>DataLoadOperations</h1>
 * The DataLoadOperations program is implemented to maintain all database loading operations from files 
 * <p>
 *
 * @author  Jaydeep Cheruku (jxc517)
 * @version 1.0
 * @since   02-Dec-2019
 */
public class DataLoadOperations {
	private static Log log = LogFactory.getLog(DataLoadOperations.class);
	
	public static String isDataloadNeeded;
	public static String dataLoadFilePath;
	public static String dataLoadFileName;
	public static Character dataLoadFileDelimiter;
	public static String dataLoadFileTable;
	public static String[] columnWidthsList;

	/**
	 * <h1>loadDataloadFileDetails</h1>
	 * This method loads all the file loading required details based on job configuration details
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadDataloadFileDetails(){
		String executingJob = ConfigPropertiesOperations.executingJob;
		Properties propJobs = ConfigPropertiesOperations.propJobs;
		isDataloadNeeded = propJobs.getProperty(executingJob+".dataload.required");
		if (isDataloadNeeded.equalsIgnoreCase("Y")){
			dataLoadFileTable = propJobs.getProperty(executingJob+".dataload.table");
			dataLoadFileDelimiter = propJobs.getProperty(executingJob+".dataload.delimiter").charAt(0);
			dataLoadFilePath = propJobs.getProperty(executingJob+".dataload.path");
			dataLoadFileName = propJobs.getProperty(executingJob+".dataload.fileName")
					.replace("YYYYMMDDHHMISS",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
					.replace("YYYYMMDD",new SimpleDateFormat("yyyyMMdd").format(new Date()));
		}
	}
	/**
	 * <h1>loadDataloadFileDetails</h1>
	 * This method is a wrappert that navigates the flow to fixed/variable file load based on delimiter parameter set in job configuration file
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadDataFile() throws IOException {
		Character c1 = new Character('N');
		if(dataLoadFileDelimiter.equals(c1)) {
			loadFixedLengthFile();
		}else {
			loadDelimitedFile();
		}
		String clobData = "";
		clobData = new String(Files.readAllBytes(Paths.get(dataLoadFilePath+dataLoadFileName)));
		DatabaseOperations.loadBatchJobInterfaceDetails("IN-BOUND", clobData, dataLoadFilePath+dataLoadFileName);
	}
	/**
	 * <h1>loadDelimitedFile</h1>
	 * This method is the core process to load delimited variable length file
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadDelimitedFile() {
		CsvReader table = null;
		try {
			table = new CsvReader(dataLoadFilePath+dataLoadFileName, dataLoadFileDelimiter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DatabaseOperations.loadDataFile(table, dataLoadFileTable);
		table.close();
	}
	/**
	 * <h1>loadFixedLengthFile</h1>
	 * This method is the core process to load fixed length file
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadFixedLengthFile() {
//		String executingJob = ConfigPropertiesOperations.executingJob;
//		Properties propJobs = ConfigPropertiesOperations.propJobs;
//		log.info(DatabaseOperations.getFixedLengthFileColumnSizes(dataLoadFileTable));
//		log.info(propJobs.getProperty(executingJob+".dataload.colWidthList"));
//		String[] columnWidthsList = "5:7:2:2:4:4:11:2:6:6:3:1:4:4:1:9:2:3:1".split(":");
		columnWidthsList = DatabaseOperations.getFixedLengthFileColumnSizes(dataLoadFileTable).split(":");
		List<Integer> ncolumnWidths  = new ArrayList<>();
		for (String s1: columnWidthsList) {
			ncolumnWidths.add(Integer.parseInt(s1));
		}
		//		FixedWidthFile file = new FixedWidthFile("C://Users//jxc517//OneDrive - Sherwin-Williams//Desktop//git//STBD0601_PAID_D191104_T110500.TXT", ncolumnWidths);
		FixedWidthFile table = new FixedWidthFile(dataLoadFilePath+dataLoadFileName, ncolumnWidths);
		//		log.info(file.tokens());
		DatabaseOperations.loadDataFile(table, dataLoadFileTable);
	}
}
