#!/bin/sh
###############################################################################################################################
# Script name   : CCN_HIERARCHY_FUTURE_TO_CURRENT.sh
# Description   : This script will release the locked objects from CCN
#
#
# Created  : 10/14/2018 jxc517 CCN Project Team.....
# Modified : 
###############################################################################################################################

/app/ccn/scripts/processJob.sh CCN_HIERARCHY_FUTURE_TO_CURRENT COSTCNTR

exitValue=$?
if [ $exitValue != 0 ] 
then 
    exit $exitValue
fi

exit 0

