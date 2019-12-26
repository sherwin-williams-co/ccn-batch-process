#!/bin/sh
###############################################################################################################################
# Script name   : RELEASE_TIMED_OUT_OBJECTS.sh
# Description   : This script will release the locked objects from CCN
#
#
# Created  : 10/14/2018 jxc517 CCN Project Team.....
# Modified : 
###############################################################################################################################

/app/ccn/scripts/processJob.sh RELEASE_TIMED_OUT_OBJECTS COSTCNTR

exitValue=$?
if [ $exitValue != 0 ] 
then 
    exit $exitValue
fi

exit 0

