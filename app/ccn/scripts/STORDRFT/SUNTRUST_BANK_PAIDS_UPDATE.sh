#!/bin/sh
###############################################################################################################################
# Script name   : SUNTRUST_BANK_PAIDS_UPDATE.sh
# Description   : This script will load suntrust paid details
#
#
# Created  : 10/14/2018 jxc517 CCN Project Team.....
# Modified : 
###############################################################################################################################

/app/ccn/scripts/processJob.sh SUNTRUST_BANK_PAIDS_UPDATE STORDRFT

exitValue=$?
if [ $exitValue != 0 ] 
then 
    exit $exitValue
fi

exit 0

