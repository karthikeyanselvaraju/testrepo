pipeline {
  agent any 
  stages {
  	//analysis  
    stage('SonarQube Analysis') {
      steps {
        script {
            try {
                def scannerHome = tool 'SONAR_SCANNER';
             
                withSonarQubeEnv('sonar') {				
                    	sh "${scannerHome}/bin/sonar-scanner -Dsonar.pullrequest.branch=${BRANCH_NAME} -Dsonar.pullrequest.key=5 -Dsonar.pullrequest.base=master -Dsonar.projectVersion=1.0 -Dsonar.language=java -Dsonar.projectKey=mylist -Dsonar.projectName=mylist -Dsonar.sources=${workspace} -Dsonar.java.binaries=. -Dsonar.analysis.mode=preview -Dsonar.dynamicAnalysis=reuseReports"					
                }
            } catch (Exception err) {                          
                throw err
            } 			
  
        } 
      }
    }								
  
}
    post {
	        success{  			
	            echo "completed"
            }
			failure{
			    echo "Failed"
			}
        }
}
