node {
    try{
        sshCommand = 'ssh -o StrictHostKeyChecking=no -l ccn stap3ccndwv'
        //plugin : mattermost
        //mattermostSend color: 'good', message: "<${env.BUILD_URL}|${env.JOB_NAME} ${env.BUILD_NUMBER}> started"
        stage('Checkout'){
            dir('playground'){
                git branch: 'master', credentialsId: 'jenkins-sherwin', url: 'https://github.sherwin.com/jxc517/ccnBatchHandling.git'
            }
        }
        //Manage Jenkins > Global Tools Configurtion
        //def gradleHome = tool 'jenkins-gradle'
        stage('Build'){
        //withEnv(["JAVA_HOME=${ tool 'jenkins-java8' }"]) {
            dir('playground'){
                //sh "${gradleHome}/bin/gradle -Penv=dev clean build"
                sh "./gradlew -Penv=Development clean build"
                //Parameters can be defined in Jeknins for respective environment build
            }
        //}
        }
        /*stage('Deploy'){
            dir('playground/build/libs') {
                sshagent(['xxxx']) {
                    sh 'scp -o StrictHostKeyChecking=no CCNBatchHandling.jar ccn@stap3ccndwv:/app/ccn/CCNBatchHandling.jar'
                }
            }
        }*/
        //mattermostSend color: 'good', message: "<${env.BUILD_URL}|${env.JOB_NAME} ${env.BUILD_NUMBER}> completed"
    }catch(err){
        //mattermostSend color: 'danger', message: "<${env.BUILD_URL}|${env.JOB_NAME} ${env.BUILD_NUMBER}> failed with error:\n${err}"
        echo "${err}"
        currentBuild.result = 'FAILURE'
    }finally{
        notifyBuild(currentBuild.result)
    }
}
def notifyBuild(String buildStatus = 'STARTED'){
    buildStatus =  buildStatus ?: 'SUCCESSFUL'
    def colorName = 'RED'
    def colorCode = '#FF0000'
    def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
    def summary = "${subject} (${env.BUILD_URL})"
    def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p><p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>"""
    if (buildStatus == 'STARTED'){
        color = 'YELLOW'
        colorCode = '#FFFF00'
    }else if (buildStatus == 'SUCCESSFUL'){
        color = 'GREEN'
        colorCode = '#00FF00'
    }else{
        color = 'RED'
        colorCode = '#FF0000'
    }
    emailext(
        subject: subject,body: details,
        recipientProviders: [culprits(), developers(), requestor()],
        to: 'jaydeep.cheruku@sherwin.com'
    )
}
