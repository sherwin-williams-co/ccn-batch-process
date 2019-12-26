#!/bin/sh
###############################################################################################################################
# Script name   : cstmr_dep_zero_net_close_dt_upd.sh
# Description   : This script will close the customer deposits with zero net amount
#
#
# Created  : 10/14/2018 jxc517 CCN Project Team.....
# Modified : 
###############################################################################################################################

/app/ccn/scripts/processJob.sh cstmr_dep_zero_net_close_dt_upd CSTMR_DPSTS

exitValue=$?
if [ $exitValue != 0 ] 
then 
    exit $exitValue
fi

exit 0

