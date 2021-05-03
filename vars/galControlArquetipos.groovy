import groovy.json.JsonSlurper

pipeline {
    agent any
    stages {
        stage("Preparo el ambiente") {
            steps {
                script {
                    print("Configuro la libreria")
                    library identifier: 'local-arquetipo@master', retriever: modernSCM([$class: 'GitSCMSource', credentialsId: '', remote: '/Users/juancho/Desktop/Repositorios/arquetipo/.git/', traits: [gitBranchDiscovery()]])

                    println("Creo archivos pom.xml y netcore.csproj")
                    sh('cp -r /Users/juancho/Desktop/Repositorios/archivos_arquetipos/* .')
                    sh("ls -lR")
                }
            }
        }
        stage("Configuro prueba") {
            steps {
                script {
                    def userInput = input(message: 'Seleccionar parametros', ok: 'Confirmar', parameters: [choice(choices: ['netcore', 'java', 'nodejs', 'python', 'pato'], description: '', name: 'TECNOLOGIA'), choice(choices: ['dev', 'int', 'No anda'], description: '', name: 'AMBIENTE')])

                    env.TECNOLOGIA = userInput['TECNOLOGIA']
                    env.AMBIENTE = userInput['AMBIENTE']
                    // env.CONFIGURACION_MOCK = "Ok."
                    // env.CONFIGURACION_MOCK = "Error."
                    env.CONFIGURACION_MOCK = "Warning."

                    println("TECNOLOGIA: ${TECNOLOGIA}")
                    println("AMBIENTE: ${AMBIENTE}")
                }
            }
        }
        stage("Test Arquetipo") {
            steps {
                script {    
                    galControlArquetipos("${env.TECNOLOGIA}", "${env.AMBIENTE}", "${env.CONFIGURACION_MOCK}")
                }
            }
        }
        stage("Compile") {
            steps {
                println("Compilacion terminada")
            }
        }
    }
    post {
        always {
            println("Ejecucion finalizada")
        }
        failure {
            println("Error en la ejecucion")
        }
        success {
            println("Exito en la ejecucion")
        }
    }    
}
