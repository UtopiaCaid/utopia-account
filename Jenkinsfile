pipeline {
    agent any
    environment {
        COMMIT_HASH = "${sh(script:'git rev-parse --short HEAD', returnStdout: true).trim()}"
        /* groovylint-disable-next-line LineLength */
        AWS_LOGIN = 'aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 499898275313.dkr.ecr.us-east-2.amazonaws.com'
        AWS_ID = '499898275313.dkr.ecr.us-east-2.amazonaws.com'
        AWS_ACCESS_KEY = "${sh(script:'echo $AWS_ACCESS_KEY', returnStdout: true)}"
        AWS_SECRET_MYSQL = credentials('dev/utopia/mysql')
        AWS_SECRET_TARGET_GROUPS = credentials('dev/utopia/target-groups')
        AWS_SECRET_VPC = credentials('dev/utopia/vpc')
        /* groovylint-disable-next-line LineLength */
        DB_USERNAME = "${sh(script:'echo $AWS_SECRET_MYSQL | jq -r \'. | .DB_USERNAME \'', returnStdout: true)}"
        DB_PASSWORD = "${sh(script:'echo $AWS_SECRET_MYSQL | jq -r \'. | .DB_PASSWORD \'', returnStdout: true)}"
        DB_URL = "${sh(script:'echo $AWS_SECRET_MYSQL | jq -r \'. | .DB_URL \'', returnStdout: true)}"
        /* groovylint-disable-next-line LineLength */
        EXECUTION_ROLE_ARN = "${sh(script:'echo $AWS_SECRET_TARGET_GROUPS | jq -r \'. | .EXECUTION_ROLE_ARN \'', returnStdout: true)}"
        /* groovylint-disable-next-line LineLength */
        TARGETGROUP_UTOPIA_ACCOUNT_DEV_ARN = "${sh(script:'echo $AWS_SECRET_TARGET_GROUPS | jq -r \'. | .TARGETGROUP_UTOPIA_ACCOUNT_DEV_ARN \'', returnStdout: true)}"
        /* groovylint-disable-next-line LineLength */
        TARGETGROUP_UTOPIA_ACCOUNT_PROD_ARN = "${sh(script:'echo $AWS_SECRET_TARGET_GROUPS | jq -r \'. | .TARGETGROUP_UTOPIA_ACCOUNT_PROD_ARN \'', returnStdout: true)}"
        UTOPIA_CLUSTER = "${sh(script:'echo $AWS_SECRET_VPC | jq -r \'. | .UTOPIA_CLUSTER \'', returnStdout: true)}"
        /* groovylint-disable-next-line LineLength */
        UTOPIA_PRIVATE_SUBNET_1 = "${sh(script:'echo $AWS_SECRET_VPC | jq -r \'. | .UTOPIA_PRIVATE_SUBNET_1 \'', returnStdout: true)}"
        /* groovylint-disable-next-line LineLength */
        UTOPIA_PUBLIC_VPC_ID = "${sh(script:'echo $AWS_SECRET_VPC | jq -r \'. | .UTOPIA_PUBLIC_VPC_ID \'', returnStdout: true)}"
        /* groovylint-disable-next-line LineLength */
        UTOPIA_APPLICATION_LB_DNS = "${sh(script:'echo $AWS_SECRET_VPC | jq -r \'. | .UTOPIA_APPLICATION_LB_DNS \'', returnStdout: true)}"

    }
    tools {
        maven 'Maven 3.6.3'
    }
    stages {
        stage('Package') {
            steps {
                echo 'Building..'

                script {
                    sh 'git submodule init'
                    /* groovylint-disable-next-line LineLength */
                    sh "sed -i 's,git@github.com:UtopiaCaid/utopia-entities.git,https://github.com/UtopiaCaid/utopia-entities.git,;' .gitmodules"
                    /* groovylint-disable-next-line LineLength */
                    sh "sed -i 's,git@github.com:UtopiaCaid/utopia-entities.git,https://github.com/UtopiaCaid/utopia-entities.git,' .git/config" 
                    sh 'cat .gitmodules'
                    sh 'cat .git/config'
                    sh 'git submodule update'
                    sh 'mvn clean package -DskipTests'
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

        stage('Deploy') {
            steps {
                sh 'chmod +x ./aws-cf-deploy.sh'
                sh './aws-cf-deploy.sh'
            }
        }

        stage('Cleanup') {
            steps {
                sh 'docker system prune -f'
            }
        }
    }
}
