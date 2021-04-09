pipeline {
    agent any
    environment {
        COMMIT_HASH="${sh(script:'git rev-parse --short HEAD', returnStdout: true).trim()}"
        AWS_LOGIN="aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 499898275313.dkr.ecr.us-east-2.amazonaws.com"
        AWS_ID="499898275313.dkr.ecr.us-east-2.amazonaws.com"
    }
    tools {
        maven 'Maven 3.6.3'
    }
    stages {

        stage('Package') {
            steps {
                echo 'Building..'

                script {
                    sh "git submodule init"
                    sh "cat .gitmodules"
                    sj "cat .git/config"
                    sh 'git submodule update'
                    sh "mvn clean package -DskipTests"
                }
            }
        }
        stage('Build') {
            steps {
                echo 'Deploying....' 
                sh "$AWS_LOGIN"
                sh "docker build -t utopia-account:$COMMIT_HASH ."
                sh 'docker images'
              
                sh "docker tag utopia-account:$COMMIT_HASH $AWS_ID/utopia-account:$COMMIT_HASH"

                sh "docker push $AWS_ID/utopia-account:$COMMIT_HASH"              
            }
        }

        // stage('Deploy') {
        //    steps {
        //        sh "touch ECSService.yml"
        //        sh "rm ECSService.yml"
        //        sh "wget https://raw.githubusercontent.com/SmoothstackUtopiaProject/CloudFormationTemplates/main/ECSService.yml"
        //        sh "aws cloudformation deploy --stack-name UtopiaFlightMS --template-file ./ECSService.yml --parameter-overrides ApplicationName=UtopiaFlightMS ECRepositoryUri=$AWS_ID/utopiaairlines/flightms:$COMMIT_HASH DBUsername=$DB_USERNAME DBPassword=$DB_PASSWORD SubnetID=$SUBNET_ID SecurityGroupID=$SECURITY_GROUP_ID TGArn=$UTOPIA_FLIGHTMS_TARGETGROUP --capabilities \"CAPABILITY_IAM\" \"CAPABILITY_NAMED_IAM\""
        //    }
        // }

        stage('Cleanup') {
            steps {
                sh "docker system prune -f"
                // sh "docker image prune -a"
            }
        }
    }
}
