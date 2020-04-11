properties([
	pipelineTriggers([ 
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
