properties([
	pipelineTriggers([
		[$class: 'GitHubPushTrigger'], 
		pollSCM('* * * * *')
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
