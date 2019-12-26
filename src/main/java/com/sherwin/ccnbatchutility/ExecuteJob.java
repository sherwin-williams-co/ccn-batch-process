package com.sherwin.ccnbatchutility;

import static java.lang.System.err;

import java.sql.Savepoint;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.log4j.Logger;

/**
 * <h1>ExecuteJob</h1>
 * The ExecuteJob program is master program that gets invoked from batch shell script with required parameters
 * Based on the environment, schema (all CCN applications reside on same database but multiple schemas per application) and job name
 * Property/Config files are read based on respective environment, so be very cautious on parameters being passed
 * For safe keeping in build script only respective property/config files are included in the jar - this avoids accidental connection into production
 * Schema names are used to identify the database that needs to be connected to execute specified job
 * Job names are used to get all the required information needed to perform a job 
 * <p>
 *
 * @author  Jaydeep Cheruku (jxc517)
 * @version 1.0
 * @since   02-Dec-2019
 */
public class ExecuteJob{
	private static Log log = LogFactory.getLog(ExecuteJob.class);
	
	public static void main(String[] args){
		/*
		 * Development COSTCNTR GNRT_STORE_BANK_CARD_SERIAL_FL
		 * Development COSTCNTR GNRT_STORE_BANK_CARD_MRCHNT_FL
		 * Development COSTCNTR RELEASE_TIMED_OUT_OBJECTS
		 * Development COSTCNTR CCN_HIERARCHY_FUTURE_TO_CURRENT
		 * Development CSTMR_DPSTS CSTMR_DEP_DAILY_LOAD
		 * Development CSTMR_DPSTS cstmr_dep_zero_net_close_dt_upd
		 * Development COSTCNTR delimitedCSVFileJobTesting
		 * Development STORDRFT ROYAL_BANK_PAIDS_UPDATE
		 * Development STORDRFT SUNTRUST_BANK_PAIDS_UPDATE
		 */
		if (args.length != 3 ||
				!args[0].matches("Development|Test|QA|Production") ||
				!args[1].matches("COSTCNTR|STORDRFT|FLDPRRPT|BANKING|CSTMR_DPSTS|CCN_UTILITY")) {
			err.format("Usage: java com.batches.ExecuteJob <execution-environmant> <app-db-user> <job-name> %n");
			err.format("<execution-environmant> (argument 1) should be Development (or) Test (or) QA (or) Production %n");
			err.format("<app-db-user>           (argument 2) should be COSTCNTR (or) STORDRFT (or) FLDPRRPT (or) BANKING (or) CSTMR_DPSTS (or) CCN_UTILITY %n");
			err.format("Example: java com.batches.ExecuteJob Development COSTCNTR CCN_HIERARCHY_FUTURE_TO_CURRENT %n");
			err.format("Example: java com.batches.ExecuteJob Development CSTMR_DPSTS cstmr_dep_zero_net_close_dt_upd %n");
			System.exit(-1);
		}else {
			try{
				ConfigPropertiesOperations.executingEnvironment=args[0];
				ConfigPropertiesOperations.executingDatabaseUser=args[1];
				ConfigPropertiesOperations.executingJob=args[2];
				ConfigPropertiesOperations.load();
				DatabaseOperations.startBatchJob(ConfigPropertiesOperations.executingJob);
				Properties prop = ConfigPropertiesOperations.prop;
				String executingDatabaseUser = ConfigPropertiesOperations.executingDatabaseUser;
				String dbUserName = prop.getProperty(executingDatabaseUser + ".dbuser");
				String dbConnection = prop.getProperty("dbconn");
				String dbPassword = ConfigPropertiesOperations.getPasswordFromVault(dbUserName, dbConnection.split(":")[0]);
				DatabaseOperations.setConnection(dbUserName, dbPassword, dbConnection);
				log.info("Setting autocommit to false");
				DatabaseOperations.conn.setAutoCommit(false);
				log.info("Establishing save point svpt1");
				Savepoint spt1 = DatabaseOperations.conn.setSavepoint("svpt1");
				try{
					if (DataLoadOperations.isDataloadNeeded.equalsIgnoreCase("Y")){
						log.info("Loading the file "+ DataLoadOperations.dataLoadFileName +" under "+ DataLoadOperations.dataLoadFilePath + " path into table : " + DataLoadOperations.dataLoadFileTable);
						DataLoadOperations.loadDataFile();
					}
					if(DatabaseOperations.dbCallRequired.equalsIgnoreCase("Y")) {
						if(DatabaseOperations.dbCallParamsExists.equalsIgnoreCase("Y")){
							if(DatabaseOperations.dbCallType.equalsIgnoreCase("1")){
								log.info("Executing DB Call Type 1 -> " + DatabaseOperations.dbCall + " with parameters : " + DatabaseOperations.dbCallParam1);
								DatabaseOperations.execProcStringIpClobOpFileNmOp(DatabaseOperations.dbCall, DatabaseOperations.dbCallParam1);
								log.info("Creating file on server location -> " + prop.getProperty("defaultDataPath")+FileOperations.fileName);
								FileOperations.writeToFile(prop.getProperty("defaultDataPath"));
							}else if(DatabaseOperations.dbCallType.equalsIgnoreCase("2")){
								log.info("Executing DB Call Type 2 -> " + DatabaseOperations.dbCall + " with parameters : " + DatabaseOperations.dbCallParam1);
								DatabaseOperations.execProcStringIpNoOp(DatabaseOperations.dbCall, DatabaseOperations.dbCallParam1);
							}
						}else if(DatabaseOperations.dbCallParamsExists.equalsIgnoreCase("N")){
							if(DatabaseOperations.dbCallType.equalsIgnoreCase("4")){
								log.info("Executing DB Call Type 4 -> " + DatabaseOperations.dbCall + " with out any parameters");
								DatabaseOperations.execProcNoIpClobOpFileNmOp(DatabaseOperations.dbCall);
								log.info("Creating file on server location -> " + prop.getProperty("defaultDataPath")+FileOperations.fileName);
								FileOperations.writeToFile(prop.getProperty("defaultDataPath"));
							}else if(DatabaseOperations.dbCallType.equalsIgnoreCase("3")){
								log.info("Executing DB Call Type 3 -> " + DatabaseOperations.dbCall + " with out any parameters");
								DatabaseOperations.executeProcedureNoIO(DatabaseOperations.dbCall);
							}
						}
					}
					log.info("Committing the database transaction");
					DatabaseOperations.conn.commit();
					if (EmailOperations.isMailNeeded.equalsIgnoreCase("Y")){
						log.info("Emailing the file as per category : " + EmailOperations.mailCategory);
						EmailOperations.execute();
					}
					if (FileOperations.isFTPNeeded.equalsIgnoreCase("Y")){
						log.info("FTP'ing the file as "+FileOperations.ftpDestFilePath+FileOperations.ftpDestFileName+" to "+FileOperations.ftpServerName+" server");
						FileOperations.transferFile(prop.getProperty("defaultDataPath"));
					}
					DatabaseOperations.completeBatchJob();
				}catch(Exception e){
//					e.printStackTrace();
//					System.err.println(e.getMessage());
					log.error(e.getMessage());
					DatabaseOperations.conn.rollback(spt1);
					DatabaseOperations.failBatchJob();
					System.exit(1);
				}
			} catch(Exception e) {
				log.error(e.getMessage());
				DatabaseOperations.failBatchJob();
				System.exit(2);
			}finally{
				try{
					DatabaseOperations.closeConnection();
					log.info("Closing the database connection");
				}catch(Exception e) {
					log.error(e.getMessage());
					DatabaseOperations.failBatchJob();
					System.exit(3);
				}
			}
		}
	}
}
