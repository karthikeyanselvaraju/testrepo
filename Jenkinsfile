pipeline {
  agent any 
  stages {
  	  
    stage('SonarQube Analysis') {
      steps {
        script {
            try {
                def scannerHome = tool 'SONAR_SCANNER';
             
                withSonarQubeEnv('sonar') {				
                    	sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectVersion=1.0 -Dsonar.language=java -Dsonar.projectKey=mylist -Dsonar.projectName=mylist -Dsonar.sources=${workspace} -Dsonar.java.binaries=. -Dsonar.dynamicAnalysis=reuseReports"					
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
