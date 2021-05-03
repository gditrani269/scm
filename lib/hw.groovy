def call (String TECNOLOGIA, String AMBIENTE, String CONFIGURACION_MOCK) {
    def comandoFind = mensajeErrorNotFound = mensajeErrorTotalArchivos = responseData = url = ""
    def HttpURLConnection connection
    def endpointArquitectura = "http://localhost:3000/api/arquetipos/validararquetipo"
    
    println("Validacion de ambiente")

    if ("${AMBIENTE}" != "dev" && "${AMBIENTE}" != "int") {
        println("No hay analisis de arquetipo definido para el ambiente ${AMBIENTE}. Aplica solo para dev e int.")
        return true
    }
