#!/bin/sh
###############################################################################################################################
# Script name   : CSTMR_DEP_DAILY_LOAD.sh
# Description   : This script will load daily customer deposits data from POS
#
#
# Created  : 10/14/2018 jxc517 CCN Project Team.....
# Modified : 
###############################################################################################################################

/app/ccn/scripts/processJob.sh CSTMR_DEP_DAILY_LOAD CSTMR_DPSTS

exitValue=$?
if [ $exitValue != 0 ] 
then 
    exit $exitValue
fi

exit 0

