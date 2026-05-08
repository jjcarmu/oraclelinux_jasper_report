package co.univalle.reportes;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet JasperReports - Universidad del Valle
 *
 * POST JSON:
 * {
 * "reporte": "/usr/share/tomcat/webapps/reportes/sifi/MiReporte.jrxml",
 * "salida": "/vhosts/reportes/sifi/MiReporte20260226.pdf",
 * "driver": "oracle.jdbc.driver.OracleDriver", (opcional)
 * "url": "jdbc:oracle:thin:@host:port:SID", (opcional)
 * "usuario": "USUARIO", (opcional)
 * "password": "PassWord", (opcional)
 * "sql": "SELECT ... FROM ...", (opcional)
 * "parametros": { "CLAVE": "valor", ... }
 * }
 *
 * Cuando se pasa "sql", se modifica el sql interno del jrxml y se compila nuevamente el jasper
 * Esto permite que PHP controle el SQL sin modificar los templates.
 */
public class GenerarReporteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(GenerarReporteServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        logger.info("[GenerarReporteServlet] Iniciando la generación del reporte...");

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Leer body JSON
            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            @SuppressWarnings("unchecked")
            Map<String, Object> datos = mapper.readValue(body, Map.class);

            String rutaReporte = (String) datos.get("reporte");
            String rutaSalida = (String) datos.get("salida");
            String driver = (String) datos.get("driver");
            String urlJdbc = (String) datos.get("url");
            String usuario = (String) datos.get("usuario");
            String password = (String) datos.get("password");
            String sql = (String) datos.get("sql");
            String formato = (String) datos.getOrDefault("formato", "pdf");

            @SuppressWarnings("unchecked")
            Map<String, Object> parametrosMapa = (Map<String, Object>) datos.getOrDefault("parametros", new HashMap<>());

            // Validaciones
            if (rutaReporte == null || rutaSalida == null) {
                jsonError(out, "Faltan campos 'reporte' o 'salida'");
                return;
            }

            File archivoJrxml = new File(rutaReporte);
            if (!archivoJrxml.exists()) {
                jsonError(out, "No existe el archivo: " + rutaReporte);
                return;
            }

            // Crear directorio de salida si no existe
            File dirSalida = new File(rutaSalida).getParentFile();
            if (dirSalida != null && !dirSalida.exists()) {
                dirSalida.mkdirs();
            }

            Map<String, Object> parametros = new HashMap<>(parametrosMapa);

            // Compilar .jrxml con cache
            String rutaJasper = rutaReporte.replace(".jrxml", ".jasper");
            File archivoJasper = new File(rutaJasper);

            JasperReport jasperReport;

            // if (!archivoJasper.exists() || archivoJrxml.lastModified() > archivoJasper.lastModified()) {
            //     jasperReport = JasperCompileManager.compileReport(rutaReporte);
            //     JasperCompileManager.compileReportToFile(rutaReporte, rutaJasper);
            // } else {
            //     jasperReport = (JasperReport) JRLoader.loadObject(archivoJasper);
            // }

            // Llenar el reporte
            JasperPrint jasperPrint;

            boolean tieneConexion = driver != null && urlJdbc != null && usuario != null && password != null;
            boolean tieneSql = sql != null && !sql.trim().isEmpty();

            // si se envia el sql voy a compilar el reporte siempre
            if (tieneConexion && tieneSql) {
                // 1. Cargar el diseño del archivo .jrxml
                JasperDesign jasperDesign = JRXmlLoader.load(rutaReporte);

                // 2. Crear o modificar la consulta SQL
                JRDesignQuery newQuery = new JRDesignQuery();
                newQuery.setText(sql);

                // 3. Inyectar la nueva consulta en el diseño
                jasperDesign.setQuery(newQuery);

                // 4. Ahora sí, compilar el diseño modificado
                jasperReport = JasperCompileManager.compileReport(jasperDesign);
                JasperCompileManager.compileReportToFile(rutaReporte, rutaJasper);
            } else {
                if (!archivoJasper.exists() || archivoJrxml.lastModified() > archivoJasper.lastModified()) {
                    jasperReport = JasperCompileManager.compileReport(rutaReporte);
                    JasperCompileManager.compileReportToFile(rutaReporte, rutaJasper);
                } else {
                    jasperReport = (JasperReport) JRLoader.loadObject(archivoJasper);
                }
            }

            // no voy a utilizar JRResultSetDataSource
            // if (tieneConexion && tieneSql) {
            //     // Ejecutar SQL externo manualmente -> JRResultSetDataSource
            //     // JasperReports usa este datasource y NO ejecuta el queryString del jrxml
            //     Class.forName(driver);
            //     conn = DriverManager.getConnection(urlJdbc, usuario, password);
            //     stmt = conn.createStatement(
            //             ResultSet.TYPE_SCROLL_INSENSITIVE,
            //             ResultSet.CONCUR_READ_ONLY);
            //     rs = stmt.executeQuery(sql);

            //     JRResultSetDataSource dataSource = new JRResultSetDataSource(rs);
            //     jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            // } else 

            if (tieneConexion) {
                // Sin SQL externo: JasperReports ejecuta su queryString interno
                Class.forName(driver);
                conn = DriverManager.getConnection(urlJdbc, usuario, password);
                jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, conn);
            } else {
                // Sin BD: solo parámetros
                jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, new JREmptyDataSource());
            }

            // Exportar PDF
            // JRPdfExporter exporter = new JRPdfExporter();
            // exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            // exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(rutaSalida));
            // exporter.exportReport();

            // Exportar según formato
            String formatoLower = formato.toLowerCase();
            switch (formatoLower) {
                case "pdf":
                    JRPdfExporter pdfExporter = new JRPdfExporter();
                    pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(rutaSalida));
                    pdfExporter.exportReport();
                    break;

                case "xls":
                    JRXlsExporter xlsExporter = new JRXlsExporter();
                    xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(rutaSalida));
                    SimpleXlsReportConfiguration xlsConfig = new SimpleXlsReportConfiguration();
                    xlsConfig.setOnePagePerSheet(false);      // evita una hoja por página
                    xlsConfig.setDetectCellType(true);        // detecta números/fechas
                    xlsConfig.setRemoveEmptySpaceBetweenRows(true);
                    xlsExporter.setConfiguration(xlsConfig);
                    xlsExporter.exportReport();
                    break;

                case "xlsx":
                    JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                    xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(rutaSalida));
                    SimpleXlsxReportConfiguration xlsxConfig = new SimpleXlsxReportConfiguration();
                    xlsxConfig.setOnePagePerSheet(false);
                    xlsxConfig.setDetectCellType(true);
                    xlsxConfig.setRemoveEmptySpaceBetweenRows(true);
                    xlsxExporter.setConfiguration(xlsxConfig);
                    xlsxExporter.exportReport();
                    break;

                default:
                    jsonError(out, "Formato no soportado: " + formato);
                    return;
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("archivo", rutaSalida);
            out.print(mapper.writeValueAsString(respuesta));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            jsonError(out, e.getMessage() + "\n" + sw.toString());

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("JasperReports Servlet OK - Univalle");
    }

    private void jsonError(PrintWriter out, String mensaje) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("ok", false);
        err.put("error", mensaje);
        out.print(mapper.writeValueAsString(err));
    }
}