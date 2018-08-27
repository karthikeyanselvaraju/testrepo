pipeline {
  agent any 
  /*Branches enabled with CICD currently
    master, hotfix, release* 
  */
  stages {
  	  
    stage('SonarQube Analysis') {
		when {
	       expression{
	          BRANCH_NAME == 'master' || BRANCH_NAME ==~ /release.*/ || BRANCH_NAME ==~ /hotfix.*/  || BRANCH_NAME ==~ /devops.*/ 
	        }
	    }
      steps {
        script {
            try {
                def scannerHome = tool 'SONAR_SCANNER';
                
                withSonarQubeEnv('sonar') {
			sh "./activator jacoco:cover"					
                    	sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectVersion=1.0 -Dsonar.projectKey=mylist -Dsonar.projectName=mylist -Dsonar.sources=app -Dsonar.java.binaries=target/scala-2.11/classes -Dsonar.java.coveragePlugin=jacoco -Dsonar.dynamicAnalysis=reuseReports -Dsonar.jacoco.reportPath=target/scala-2.11/jacoco/jacoco.exec"					
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
