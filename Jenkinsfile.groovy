<<<<<<< HEAD
=======
properties([
	pipelineTriggers([ 
		pollSCM('* * * * *')
	])
])

>>>>>>> 06d2bda4633bfe373aa7a883c69c28c16e21fc98
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
