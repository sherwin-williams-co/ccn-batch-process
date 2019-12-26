#!/bin/sh
###############################################################################################################################
# Script name   : processJob.sh
# Description   : This script will execute a job, generates files, send emails, ftp the files
#
#
# Created  : 10/14/2018 jxc517 CCN Project Team.....
# Modified : 
###############################################################################################################################
cd /app/ccn/scripts
source ./get_host_environment.sh

export HOME=/app/ccn
CLASSHOME="$HOME/CCNBatchHandling"

JOB_NAME="$1"
APP_DB_NAME="$2"

PROC_NAME="processJob.sh"
DATE=$(date +"%Y-%m-%d")
LOGDIR="$CLASSHOME/log/$APP_DB_NAME"
LOGFILE="processJob_${DATE}.log"
TimeStamp=`date '+%Y%m%d%H%M%S'`

TIME=$(date +"%H:%M:%S")
echo "$PROC_NAME --> Call to the JAVA class started with parameters ${CCN_HOST_ENV} ${APP_DB_NAME} ${JOB_NAME} at $DATE : $TIME "  >> $LOGDIR/$LOGFILE

cd "$CLASSHOME" || exit
#ExecutionLog=$(java -cp ".:./*" com.batches.ExecuteJob "$CCN_HOST_ENV" "$APP_DB_NAME" "$JOB_NAME")
ExecutionLog=$(java -jar CCNBatchHandling.jar "$CCN_HOST_ENV" "$APP_DB_NAME" "$JOB_NAME")
exitValue=$?
if [ $exitValue != 0 ] 
then 
    echo "$ExecutionLog" >> $LOGDIR/$LOGFILE
    TIME=$(date +"%H:%M:%S")
    echo "$PROC_NAME --> processing failed with exit code $exitValue at $DATE : $TIME "  >> $LOGDIR/$LOGFILE
    echo "*************************************************************************************" >> $LOGDIR/$LOGFILE
    exit $exitValue
fi
echo "$ExecutionLog" >> $LOGDIR/$LOGFILE

TIME=$(date +"%H:%M:%S")
echo "$PROC_NAME --> processing completed at $DATE : $TIME "  >> $LOGDIR/$LOGFILE
echo "*************************************************************************************" >> $LOGDIR/$LOGFILE

exit 0
