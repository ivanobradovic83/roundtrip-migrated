#!/usr/bin/env groovy


pipeline {
    agent any
    environment {
        // This sets both ARTIFACTORY_USR and ARTIFACTORY_PSW environment variables.
        // Thanks, Groovy :-(
        ARTIFACTORY = credentials('artifactory-k8s')
    }
    stages {
        stage('init') {
            steps {
                script {
                    def sbtHome = tool 'sbt'
                    env.sbtinit= "mkdir -p ~/.sbt/0.13 && echo 'credentials += Credentials(\"Artifactory Realm\", \"artifactory.k8s.awssdu.nl\", \"${env.ARTIFACTORY_USR}\", \"${env.ARTIFACTORY_PSW}\")' >~/.sbt/0.13/credentials.sbt"
                    env.sbt= "${sbtHome}/bin/sbt -batch -DsduTeam=cwc"
                }
                sh "$sbtinit"
            }
        }
        stage('PR') {
            when {
                expression { BRANCH_NAME ==~ /^PR-\d+$/ }
            }
            steps {
                ansiColor('xterm') {
                    sh "$sbt clean scalafmt::test test:scalafmt::test test"
                }
            }
            post {
                always {
                    junit "target/test-reports/*.xml"
                }
            }
        }
        stage('QA') {
            when {
                branch 'qa'
            }
            steps {
                ansiColor('xterm') {
                    sh "$sbt update clean publish"
                }
            }
        }
        stage('Master') {
            when {
                branch 'master'
            }
            steps {
                ansiColor('xterm') {
                    sh "$sbt clean scalafmt::test test:scalafmt::test coverage test coverageReport"
                }
            }
            post {
                always {
                    junit "target/test-reports/*.xml"
                    step([$class: 'CoberturaPublisher', autoUpdateHealth: false, autoUpdateStability: false,
                          coberturaReportFile: 'target/scala-2.12/coverage-report/cobertura.xml', failNoReports: false,
                          failUnhealthy: false, failUnstable: false, maxNumberOfBuilds: 0, onlyStable: false,
                          sourceEncoding: 'ASCII', zoomCoverageChart: false])
                }
            }
        }
    }
}
