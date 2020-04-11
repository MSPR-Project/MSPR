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
		stage('UnitTest'){
			sh "javac /app/src/test/java/com/example/mspr/ExampleUnitTest.java"
			sh "java ExampleUnitTest"
		}
	}
	finally{
		cleanWs()
	}
}
