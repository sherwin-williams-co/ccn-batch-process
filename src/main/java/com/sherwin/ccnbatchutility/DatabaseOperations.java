package com.sherwin.ccnbatchutility;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.csvreader.CsvReader;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

/**
 * <h1>DatabaseOperations</h1>
 * The DatabaseOperations program is implemented to hold all database related activities
 * <p>
 *
 * @author  Jaydeep Cheruku (jxc517)
 * @version 1.0
 * @since   02-Dec-2019
 */
public class DatabaseOperations {
	private static Log log = LogFactory.getLog(DatabaseOperations.class);

	public static Connection conn = null;
	public static Connection utlConn = null;
	public static String dbCallRequired;
	public static String dbCall;
	public static String dbCallParamsExists;
	public static String dbCallType;
	public static String dbCallParam1;
	public static String dbCallParam2;
	public static int batchId;

	/**
	 * <h1>setConnection</h1>
	 * This method sets the connection to respective database based on parameters passed in
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void setConnection(String inUser, String inPwd, String inConn) throws SQLException{
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
//		log.info("Connecting to database : "+ inUser + "/" + inPwd + "@" + inConn);
		log.info("Connecting to database : "+ inUser + "@" + inConn);
		conn = DriverManager.getConnection("jdbc:oracle:thin:@"+inConn, inUser, inPwd);
	}
	/**
	 * <h1>setUtilityConnection</h1>
	 * This method sets the connection to utility schema of the database based on parameters passed in
	 * Its been duplicated to not overwrite existing database connections and close that accidentally
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void setUtilityConnection(String inUser, String inPwd, String inConn) throws SQLException{
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
//		log.info("Connecting to database : "+ inUser + "/" + inPwd + "@" + inConn);
		log.info("Connecting to database : "+ inUser + "@" + inConn);
		utlConn = DriverManager.getConnection("jdbc:oracle:thin:@"+inConn, inUser, inPwd);
	}
	/**
	 * <h1>closeConnection</h1>
	 * This method closes the respective database connection along with the utility database connection
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void closeConnection() throws SQLException{
		conn.close();
		utlConn.close();
	}
	/**
	 * <h1>loadDBDetails</h1>
	 * This method loads all the database call required details based on job configuration details
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadDBDetails(){
		String executingJob = ConfigPropertiesOperations.executingJob;
		Properties propJobs = ConfigPropertiesOperations.propJobs;
		dbCallRequired = propJobs.getProperty(executingJob+".db.call.required");
		if (dbCallRequired.equalsIgnoreCase("Y")){
			dbCall = propJobs.getProperty(executingJob+".db.call");
			dbCallType = propJobs.getProperty(executingJob+".db.callType");
			dbCallParamsExists = propJobs.getProperty(executingJob+".db.parameter.required");
			if (dbCallParamsExists.equalsIgnoreCase("Y")){
				String[] myParams = propJobs.getProperty(executingJob+".db.parameter.commaSeperatedInputParams").split(",");
				int i = 0;
				for (String s1: myParams) {
					if(i==0){
						dbCallParam1 = s1.replace("YYYYMMDDHHMISS",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
								.replace("YYYYMMDD",new SimpleDateFormat("yyyyMMdd").format(new Date()));
					}else if (i==1) {
						dbCallParam2 = s1.replace("YYYYMMDDHHMISS",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
								.replace("YYYYMMDD",new SimpleDateFormat("yyyyMMdd").format(new Date()));
					}
					i++;
				}
			}
		}
	}
	/**
	 * <h1>execProcStringIpClobOpFileNmOp</h1>
	 * This method makes a DB call that accepts CLOB as its only parameter
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void execProcStringIpClobOpFileNmOp(String methodCall, String inParam){
		CallableStatement pstmt = null;
		try {
			pstmt = conn.prepareCall("{call " + methodCall + "}");
			pstmt.setString(1, inParam);
			pstmt.registerOutParameter(2, Types.CLOB);
			pstmt.registerOutParameter(3, Types.VARCHAR);
			pstmt.execute();
			FileOperations.clobData = pstmt.getString(2);
			FileOperations.fileName = pstmt.getString(3);
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	/**
	 * <h1>execProcStringIpNoOp</h1>
	 * This method makes a DB call that accepts VARCHAR2 as its only parameter
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void execProcStringIpNoOp(String methodCall, String inParam){
		CallableStatement pstmt = null;
		try {
			pstmt = conn.prepareCall("{call " + methodCall + "}");
			pstmt.setString(1, inParam);
			pstmt.execute();
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	/**
	 * <h1>executeProcedureNoIO</h1>
	 * This method makes a DB call that do not accept any parameters nor returns anything back
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void executeProcedureNoIO(String methodCall){
		CallableStatement pstmt = null;
		try {
			pstmt = conn.prepareCall("{call " + methodCall + "}");
			pstmt.execute();
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	/**
	 * <h1>execProcNoIpClobOpFileNmOp</h1>
	 * This method makes a DB call that do not accept any parameters but returns CLOB and respective file name to store that CLOB data
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void execProcNoIpClobOpFileNmOp(String methodCall){
		CallableStatement pstmt = null;
		try {
			pstmt = conn.prepareCall("{call " + methodCall + "}");
			pstmt.registerOutParameter(1, Types.CLOB);
			pstmt.registerOutParameter(2, Types.VARCHAR);
			pstmt.execute();
			FileOperations.clobData = pstmt.getString(2);
			FileOperations.fileName = pstmt.getString(3);
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	/**
	 * <h1>loadDataFile</h1>
	 * This method loads the delimited variable length file data into respective table name passed in
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadDataFile(CsvReader table, String tableName) {
		PreparedStatement stmt = null;
		try {
			//			log.info(table.readHeaders());
			table.readHeaders();
			String queryIp = "";
			for (int i =0; i < table.getHeaderCount(); i++){
				queryIp = queryIp + "?,";
			}
			queryIp = queryIp.substring(0, queryIp.length()-1);
			//			log.info(queryIp);
			stmt = conn.prepareStatement("INSERT INTO "+ tableName +" VALUES ("+queryIp+")");
			final int batchSize = 1000;
			int count = 0;
			while (table.readRecord()) {
				for (int i =0; i < table.getHeaderCount(); i++){
					//					log.info(i+1+ "->" + table.getHeader(i));
					stmt.setString(i+1, table.get(table.getHeader(i)));
				}
				stmt.addBatch();
				if (++count % batchSize == 0) {
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
		} catch (Exception e) {
			log.error(e.getMessage());
			System.exit(0);
		} finally {
			table.close();
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
		log.info("Records created successfully");
	}
	/**
	 * <h1>getFixedLengthFileColumnSizes</h1>
	 * This method makes a DB call with the table name and gets respective field lengths which will later be used to load the fixed length file 
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static String getFixedLengthFileColumnSizes(String tableName) {
		Statement stmt = null;
		String fieldLengths = "";
		try {
			stmt = conn.createStatement();
			String sql = "SELECT DATA_LENGTH"+
			             "  FROM ALL_TAB_COLS"+
					     " WHERE TABLE_NAME = '"+tableName+"'"+
			             "   AND OWNER = (SELECT TABLE_OWNER"+
					     "                  FROM USER_SYNONYMS"+
			             "                 WHERE SYNONYM_NAME = '"+tableName+"')"+
					     " ORDER BY COLUMN_ID";
			ResultSet resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				fieldLengths = fieldLengths + resultSet.getInt("DATA_LENGTH") + ":";
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			System.exit(0);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
		return fieldLengths.substring(0, fieldLengths.length()-1);
	}
	/**
	 * <h1>loadDataFile</h1>
	 * This method loads the fixed length file data into respective table name passed in
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadDataFile(FixedWidthFile table, String tableName) {
		PreparedStatement stmt = null;
		try {
			String queryIp = "";
			for (List<String> s1: table.tokens()) {
				for (String s2: s1) {
					queryIp = queryIp + "?,";
				}
				break;
			}
			queryIp = queryIp.substring(0, queryIp.length()-1);
			//			log.info(queryIp);
			stmt = conn.prepareStatement("INSERT INTO "+ tableName +" VALUES ("+queryIp+")");
			final int batchSize = 1000;
			int count = 0;
			for (List<String> s1: table.tokens()) {
				int i = 0;
				for (String s2: s1) {
					stmt.setString(i+1, s2);
					i++;
				}
				stmt.addBatch();
				if (++count % batchSize == 0) {
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
		} catch (Exception e) {
			log.error(e.getMessage());
			System.exit(0);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
		log.info("Records created successfully");
	}
	/**
	 * <h1>setMailingInfo</h1>
	 * This method makes a DB call to get the maiilng details needed based on mail category passed in
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void setMailingInfo(String mailCategory){
		CallableStatement pstmt = null;
		try {
			pstmt = utlConn.prepareCall("{call MAIL_PKG.MAILING_DETAILS_SP(?,?)}");
			pstmt.setString(1, mailCategory);
			pstmt.registerOutParameter(2, OracleTypes.CURSOR);
			pstmt.execute();
			ResultSet rset =((OracleCallableStatement) pstmt).getCursor(2);
			while (rset.next()){
				EmailOperations.subject = rset.getString(3);
				EmailOperations.from = rset.getString(4);
				EmailOperations.body = rset.getString(5);
				EmailOperations.signature = rset.getString(6);
				EmailOperations.to = rset.getString(7);
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	/**
	 * <h1>startBatchJob</h1>
	 * This method makes a DB call to set the starting batch statistics (as in process) based on batch name passed in
	 * NOTE that it internally stores the batch id returned from DB call which is later used to maintain batch statistics
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void startBatchJob(String batchName){
		CallableStatement pstmt = null;
		try {
			pstmt = utlConn.prepareCall("{call CCN_BATCH_JOBS_STATS_PKG.INITIATE_BATCH_PROCESS(?,?)}");
			pstmt.setString(1, batchName);
			pstmt.registerOutParameter(2, Types.INTEGER);
			pstmt.execute();
			batchId = pstmt.getInt(2);
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	/**
	 * <h1>failBatchJob</h1>
	 * This method makes a DB call to set the batch status to failed
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void failBatchJob(){
		CallableStatement pstmt = null;
		try {
			pstmt = utlConn.prepareCall("{call CCN_BATCH_JOBS_STATS_PKG.FAIL_BATCH_PROCESS(?)}");
			pstmt.setInt(1, batchId);
			pstmt.execute();
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	/**
	 * <h1>completeBatchJob</h1>
	 * This method makes a DB call to set the batch status to completed
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void completeBatchJob(){
		CallableStatement pstmt = null;
		try {
			pstmt = utlConn.prepareCall("{call CCN_BATCH_JOBS_STATS_PKG.COMPLETE_BATCH_PROCESS(?)}");
			pstmt.setInt(1, batchId);
			pstmt.execute();
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}finally {

			}
		}
	}
	/**
	 * <h1>loadBatchJobInterfaceDetails</h1>
	 * This method makes a DB calls to set the batch statistics whenever there is file loaded/generated along with their respective paths on server
	 *
	 * @author  Jaydeep Cheruku (jxc517)
	 * @version 1.0
	 * @since   02-Dec-2019
	 */
	public static void loadBatchJobInterfaceDetails(String interfaceType, String fileData, String fileName){
		CallableStatement pstmt = null;
		try {
			pstmt = utlConn.prepareCall("{call CCN_BATCH_JOBS_STATS_PKG.LOAD_CCN_BATCH_JOBS_INTRFC_DTLS(?,?,?,?)}");
			pstmt.setInt(1, batchId);
			pstmt.setString(2, interfaceType);
			pstmt.setString(3, fileData);
			pstmt.setString(4, fileName);
			pstmt.execute();
		} catch (SQLException e) {
			log.error(e.getMessage());
		}finally{
			try{
				pstmt.close();
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
}
