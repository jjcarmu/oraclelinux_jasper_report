<?php
/**
 * Java.inc - Capa de abstracción HTTP para JasperReports
 * Reemplaza JavaBridge sin cambiar la interfaz de llamada.
 * Compatible con PHP 5.3+
 */

define('JASPER_SERVLET_URL', 'http://192.168.37.42/reportes/GenerarReporte');

/**
 * Clase base que simula java.util.HashMap
 */
class JasperParametros {
    private $datos = array();

    public function put($clave, $valor) {
        $this->datos[$clave] = $valor;
    }

    public function get($clave) {
        return isset($this->datos[$clave]) ? $this->datos[$clave] : null;
    }

    public function toArray() {
        return $this->datos;
    }
}

/**
 * Clase "java" — para que "new java('java.util.HashMap')" funcione igual que antes.
 */
class java extends JasperParametros {
    public function __construct($claseJava = null) {
        // clase Java ignorada, siempre actúa como HashMap
    }
}

/**
 * Función java() — para compatibilidad con código que llame java(...) sin new.
 */
function java($clase) {
    return new JasperParametros();
}

/**
 * Clase JdbcConnection — almacena parámetros de conexión JDBC.
 */
class JdbcConnection {
    public $driver;
    public $url;
    public $usuario;
    public $password;

    public function __construct($driver, $url, $usuario, $password) {
        $this->driver = $driver;
        $this->url = $url;
        $this->usuario = $usuario;
        $this->password = $password;
    }

    public function getConnection() {
        return $this;
    }
}
