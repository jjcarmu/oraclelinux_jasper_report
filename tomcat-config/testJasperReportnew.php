<?php

//require_once dirname(__FILE__) . '/../reportes/Java.inc';
//require_once dirname(__FILE__) . '/../reportes/php-jru.php';
require_once dirname(__FILE__) . '/reportesprueba/Java.inc';
require_once dirname(__FILE__) . '/reportesprueba/php-jru.php';

$dbOracle = "PRUEBASFPL";
$hostOracle = "192.168.37.2";
$usuarioOracle = "SIFI";
$passOracle = "SifiSymf0ny";
$puerto = "1526";

$rutaReporte = "/usr/local/apache-tomcat-7.0/webapps/reportes/sifi/";
$rutaSalida = "/vhosts/reportes/sifi/";

$connectionString = "jdbc:oracle:thin:@$hostOracle:$puerto:$dbOracle";
$ConexionOracle = new JdbcConnection("oracle.jdbc.driver.OracleDriver", $connectionString, $usuarioOracle, $passOracle);

$jru = new JRU();

$identificador = "07";
$dependencia = "01";
$fondo_fuente = "03";
$agno = "2026";

$userId_fecha = date("YmdHis");
$nombre_reporte = "SaldoEnTransito";
$formato = "pdf";

$sql = "SELECT SUM(SAPPAPDE-SAPPCEDI) TOTAL FROM SALDPRPE WHERE SAPPEMPR = '01' AND SAPPPERI = '2026' AND SAPPCUPR LIKE '70103___________'";

$Reporte = $rutaReporte . 'SaldoEnTransito.jrxml';
$SalidaReporte = $rutaSalida . $nombre_reporte . $userId_fecha . '.' . $formato;

$Parametro = new java('java.util.HashMap');

$Parametro->put("IMAGEN_DIR", $rutaReporte);
$Parametro->put("Dependencia", "01-Rectoria");
$Parametro->put("nombreDepartamento", "01-Rectoria");
$Parametro->put("CdEspera", number_format("10000"));
$Parametro->put("AjDebitoEspera", number_format("10000"));
$Parametro->put("CreditoCdEspera", number_format("10000"));
$Parametro->put("CertificadoInterfinanzas", number_format("10000"));
$Parametro->put("SaldoFinanAcum", number_format("10000"));
$Parametro->put("RNDLapso", number_format("10000"));
$Parametro->put("DebitoRdRND", number_format("10000"));
$Parametro->put("CreditoRdRND", number_format("10000"));
$Parametro->put("DisponibleRegistro", number_format("10000"));
$Parametro->put("Mes", "Febrerock");
$Parametro->put("subNivel_2", "Fuente");
$Parametro->put("subNivel_2_value", "03-Fondo Comun Recursos Propios");
$Parametro->put("subNivel_3", null);
$Parametro->put("subNivel_3_value", null);
$Parametro->put("subNivel_4", null);
$Parametro->put("subNivel_4_value", null);
$Parametro->put("subNivel_5", null);
$Parametro->put("subNivel_5_value", null);
$Parametro->put("SaldoFinanzas", number_format("10000"));

$jru->runPdfFromSql($Reporte, $SalidaReporte, $Parametro, $sql, $ConexionOracle->getConnection());

//$rutaDescarga = "https://reportdesaweb.univalle.edu.co/reportes/sifi/";
$rutaDescarga = "http://192.168.37.42/reportes/sifi/";
$filePath = $rutaDescarga . $nombre_reporte . $userId_fecha . '.' . $formato;

header("Content-Type: application/pdf");
header("Content-Disposition: attachment; filename=\"".basename($filePath)."\"");
// header("Content-Disposition: attachment; filename=\"".$nombre_reporte.'.'.$formato."\"");
header("Content-Description: File Transfer");
header("Content-Transfer-Encoding: binary");

ob_clean();
flush();
readfile($filePath);



?>
