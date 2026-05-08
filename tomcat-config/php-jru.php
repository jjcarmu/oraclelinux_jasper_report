<?php
/**
 * php-jru.php - Cliente HTTP para el servlet JasperReports
 * Reemplaza php-jru.php original (JavaBridge) por llamadas HTTP curl.
 * Compatible con PHP 5.3+
 *
 * Universidad del Valle - Reportes
 */

if (!defined('JASPER_SERVLET_URL')) {
    require_once dirname(__FILE__) . '/Java.inc';
}


/**
 * Clase JRU — JasperReports Univalle.
 *
 * Interfaz idéntica a la clase original para minimizar cambios en los
 * scripts de reporte existentes.
 */
class JRU {

    /** Timeout para la generación del PDF en segundos */
    public $timeout = 120;

    /**
     * Genera un PDF a partir de un .jrxml y una consulta SQL,
     * guardándolo en $rutaSalida.
     *
     * @param string $rutaReporte Ruta absoluta al .jrxml en el servidor
     * @param string $rutaSalida Ruta absoluta donde guardar el PDF
     * @param JasperParametros $parametros Objeto con put()/get()
     * @param string $sql Consulta SQL para el datasource
     * @param JdbcConnection $conexion Objeto JdbcConnection->getConnection()
     * @return void
     * @throws RuntimeException si la generación falla
     */
    public function runPdfFromSql($rutaReporte, $rutaSalida, $parametros, $sql, $conexion, $formato) {

        $payload = array(
            'reporte' => $rutaReporte,
            'salida' => $rutaSalida,
            'driver' => $conexion->driver,
            'url' => $conexion->url,
            'usuario' => $conexion->usuario,
            'password' => $conexion->password,
            'sql' => $sql,
            'formato' => $formato,
            'parametros' => $parametros->toArray(),
        );

        $respuesta = $this->_llamarServlet($payload);

        if (!$respuesta['ok']) {
            throw new RuntimeException(
                'Error generando reporte: ' . $respuesta['error']
            );
        }
    }

    /**
     * Genera un PDF sin consulta SQL (solo parámetros, JREmptyDataSource).
     *
     * @param string $rutaReporte
     * @param string $rutaSalida
     * @param JasperParametros $parametros
     * @return void
     */
    public function runPdfNoSql($rutaReporte, $rutaSalida, $parametros) {

        $payload = array(
            'reporte' => $rutaReporte,
            'salida' => $rutaSalida,
            'parametros' => $parametros->toArray(),
        );

        $respuesta = $this->_llamarServlet($payload);

        if (!$respuesta['ok']) {
            throw new RuntimeException(
                'Error generando reporte: ' . $respuesta['error']
            );
        }
    }

    /**
     * Envía el payload al servlet vía HTTP POST.
     *
     * @param array $payload
     * @return array Respuesta decodificada { ok, archivo|error }
     */
    private function _llamarServlet($payload) {

        $json = json_encode($payload);

        error_log("JASPER URL: " . JASPER_SERVLET_URL);
        error_log("PAYLOAD: " . substr($json, 0, 200));

        $ch = curl_init(JASPER_SERVLET_URL);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_TIMEOUT, $this->timeout);
        curl_setopt($ch, CURLOPT_PROXY, '');
        curl_setopt($ch, CURLOPT_PROXYTYPE, CURLPROXY_HTTP);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            'Content-Type: application/json',
            'Content-Length: ' . strlen($json),
        ));

        $respuestaRaw = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $curlError = curl_error($ch);
        curl_close($ch);

        if ($respuestaRaw === false || $curlError) {
            return array('ok' => false, 'error' => 'curl error: ' . $curlError);
        }

        $respuesta = json_decode($respuestaRaw, true);

        if ($respuesta === null) {
            return array('ok' => false, 'error' => 'Respuesta no JSON: ' . $respuestaRaw);
        }

        return $respuesta;
    }
}
