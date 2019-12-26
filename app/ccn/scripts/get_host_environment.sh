#!/bin/sh
###############################################################################################################################
# Script name   : get_host_environment.sh
# Description   : This script will set the environment based on the host server in which it is executing
#
#
# Created  : 10/14/2018 jxc517 CCN Project Team.....
# Modified : 
###############################################################################################################################

ccn_host_name=`hostname`

if [ "$ccn_host_name" = "cpovms05ccndrw" ]
then
    CCN_HOST_ENV="Development"
elif [ "$ccn_host_name" = "cpovms06ccntrw" ]
then
    CCN_HOST_ENV="Test"
elif [ "$ccn_host_name" = "cpovms05ccnqrw" ]
then
    CCN_HOST_ENV="QA"
elif [ "$ccn_host_name" = "stap3ccnphq" ]
then
    CCN_HOST_ENV="Production"
fi
