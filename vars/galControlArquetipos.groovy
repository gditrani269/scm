import groovy.json.JsonSlurper

def call (String TECNOLOGIA, String AMBIENTE, String CONFIGURACION_MOCK) {
    def comandoFind = mensajeErrorNotFound = mensajeErrorTotalArchivos = responseData = url = ""
    def HttpURLConnection connection
    def endpointArquitectura = "http://localhost:3000/api/arquetipos/validararquetipo"
    
    println("Validacion de ambiente")

    if ("${AMBIENTE}" != "dev" && "${AMBIENTE}" != "int") {
        println("No hay analisis de arquetipo definido para el ambiente ${AMBIENTE}. Aplica solo para dev e int.")
        return true
    }

    println("Validacion de tecnologia")

    switch("${TECNOLOGIA}") {
        case "java":
            mensajeErrorNotFound = "No se encontro archivo POM.xml"
            mensajeErrorTotalArchivos = "Hay más de un archivo POM.xml en el proyecto"
            comandoFind = sh(returnStdout: true, script: 'find . -iname "pom.xml" -type f ! -path "*test*"')
        break
        case "netcore":
            mensajeErrorNotFound = "No se encontro archivo csproj"
            mensajeErrorTotalArchivos = "Hay más de un archivo csproj en el proyecto"
            comandoFind = sh(returnStdout: true, script: 'find . -iname "*.csproj" -type f ! -path "*test*"')
        break
        default:
            println("No hay analisis de arquetipo definido para la tecnologia ${TECNOLOGIA}. Aplica solo para java y netcore.")
            return true
    }
    
    if (comandoFind == '' || comandoFind.length() < 1) {
        println(mensajeErrorNotFound)
        error(mensajeErrorNotFound)
    }

    def listadoArchivos = comandoFind.tokenize()
    
    if (listadoArchivos.size() > 1) {
        println(mensajeErrorTotalArchivos)
        error(mensajeErrorTotalArchivos)
    }

    File file = new File("${env.WORKSPACE}/${listadoArchivos[0]}")
    String fileContent = file.text
    
    try {
        println("Ambiente: ${AMBIENTE}")
        println("Tecnologia: ${TECNOLOGIA}")
        println("Archivo a controlar: ${listadoArchivos[0]}")

        url = new URL("${endpointArquitectura}" + "?tecnologia=${TECNOLOGIA}&ambiente=${AMBIENTE}&configuracion=${CONFIGURACION_MOCK}")
        connection = url.openConnection()
        connection.requestMethod = "POST"
        connection.setDoOutput(true)
        connection.setRequestProperty("Content-Type", "application/xml")
        connection.getOutputStream().write(fileContent.getBytes("UTF-8"))
    } catch (ConnectException) {
        error("Error de conexion Api Arquitectura - No se pudo establecer la conexion con el servicio ${endpointArquitectura}. Codigo: 500")
    } finally {
        connection.disconnect()
    }

    switch(connection.getResponseCode()) {
        case 200:
            responseData = connection.getInputStream().getText()
            break
        default:
            responseData = connection.getErrorStream().getText()
            break
    }

    def Map responseData = new JsonSlurper().parseText("${responseData}")
    def statusApi = responseData.data.status
    def mensajeAPI = "Respuesta API: ${responseData.data}"

    println(mensajeAPI)

    if (responseData.data.status == "Error.") {        
        error(mensajeAPI)
    }
}
