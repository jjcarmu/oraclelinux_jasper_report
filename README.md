# oraclelinux_jasper_report
Contenedor con oracle linux 10, que contiene reporteador de jaspersoft community

**Pasos Despliegue**
1. Se debe abrir el fichero [run-server-report.sh](run-server-report.sh) y modificar el valor de la variable ´path´ por la ruta en donde se descargo el repositorio.
---
2. Desde la raiz del repositorio, se debe ejecutar el fichero [run-server-report.sh](run-server-report.sh).
```bash
sh run-server-report.sh
```
---
3. Validar si el apache tomcat se encuentra activo, abrir desde el navegador

*   **Local:** [http://localhost:8880](http://localhost:8880)
---
4. Si el apache no se encuentra activo, se debe ingresar al contener y ejecutar el comando de start tomcat:
```bash
docker exec -it jasper_report bash -c "/usr/share/tomcat/bin/startup.sh"
```
5. Verificar el reporteador, abrir Postman e importar el fichero [univalle.postman_collection.json](univalle.postman_collection.json), verificar la url y puertos correspondan al [docker-compose.yml](docker-compose.yml).
---
6. Pasos para desplegar de nuevo Jasper Report
*  Se debe Ingresar al contenedor
```bash
docker exec -it jasper_report bash
```
*  Ingresar a la ruta /opt/jasper-servlet/ y ejecutar para compilar y crear el war
```bash
cp /opt/jasper-servlet
```
```bash
mvn clean package -DskipTests
```

*  Apagar servidor tomcat
```bash
/usr/share/tomcat/bin/shutdown.sh
```

*  Copiar el war generado en las aplicaciones de tomcat
```bash
cp /opt/jasper-servlet/target/reportes.war /usr/share/tomcat/webapps/reportesJasper.war
```
*  Se debe dar los permisos de user y group tomcat  
```bash
chown -R tomcat:tomcat /usr/share/tomcat/webapps/
```
* Levantar de nuevo el tomcat
```bash
/usr/share/tomcat/bin/startup.sh
```