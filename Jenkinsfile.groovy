node{
	cleanWs()
	try{
		stage('PremiereEtape'){
			sh "echo 'hello wordl'"
		}
		stage('deuxiemeEtape'){
			sh "echo 'hello wordl'"
		}
		stage('troisiemeEtape'){
			sh "echo 'hello world'"
		}
	}
	finally{
		cleanWs()
	}
}
