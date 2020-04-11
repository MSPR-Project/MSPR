properties([
	pipelineTriggers([
		[$class: 'GitHubPushTrigger'], 
		pollSCM('H/15 * * * *')
	])
])

node{
	cleanWs()
	try{
		stage('PremiereEtape'){
			sh "echo 'hello wordl'"
		}
	}
	finally{
		cleanWs()
	}
}
