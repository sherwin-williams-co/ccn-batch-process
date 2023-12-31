# This repository contains core components to execute CCN jobs across all the CCN applications

**Key reasons for this implementation:**
1. Avoid usage of database server to create files
2. Avoid usage of database server to load data into database
3. Avoids multiple accounts on database server
4. Avoids hard coded passwords
5. Avoids sending emails from database
6. Maintains single point of entry and maintainability to self-document all the CCN jobs

> gradle build scripts will take care of generating the jar file
> Jenkinsfile will take care of invoking the gradle script to generate the jar file and save it on application server at respective path which then will be used by the shell scripts

## Server folder structure requirements:

### Folders to maintain log files generated by log4j
```
1. /app/ccn/CCNBatchHandling
1.1. /app/ccn/CCNBatchHandling/log
1.1.1. /app/ccn/CCNBatchHandling/log/BANKING
1.1.2. /app/ccn/CCNBatchHandling/log/COSTCNTR
1.1.3. /app/ccn/CCNBatchHandling/log/CSTMR_DPSTS
1.1.4. /app/ccn/CCNBatchHandling/log/FLDPRRPT
1.1.5. /app/ccn/CCNBatchHandling/log/STORDRFT
```
### Folders to maintain shell scripts
```
1. /app/ccn/CCNBatchHandling
1.1. /app/ccn/CCNBatchHandling/log
1.1.1. /app/ccn/scripts/BANKING
1.1.2. /app/ccn/scripts/COSTCNTR
1.1.3. /app/ccn/scripts/CSTMR_DPSTS
1.1.4. /app/ccn/scripts/FLDPRRPT
1.1.5. /app/ccn/scripts/STORDRFT
```
## Dependencies/Pre-requisites:
1. CyberArk vault should be active with all the credentials stored inside it for respective environments
2. Both .p12 and .ks files are available under /app/ccn/CCNBatchHandling folder for DBA's to generate below files
```
/app/ccn/CCNBatchHandling/p12_publickey
/app/ccn/CCNBatchHandling/p12_privatekey
/app/ccn/CCNBatchHandling/p12_encrypteddata
/app/ccn/CCNBatchHandling/ks_publickey
/app/ccn/CCNBatchHandling/ks_privatekey
/app/ccn/CCNBatchHandling/ks_encrypteddata
```
3. Permissions on folder structures mentioend above should be availabe for ctmusr when invoked from BMC Control-M

### Below are repositories that contains the code that is used by this API

[ccn-cyber-ark-certs](https://github.sherwin.com/jxc517/ccn-cyber-ark-certs) : Maintain raw certs & shell scripts to handle their credentials

[cryptography-utility](https://github.sherwin.com/jxc517/cryptography-utility) : Contains code to decrypt encrypted credentials

[cyber-ark-http-interface](https://github.sherwin.com/jxc517/cyber-ark-http-interface) : Contains code to make HTTP requests to CyberArk using CCP
