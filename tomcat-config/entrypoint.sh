#!/bin/bash

# 1. Eliminar archivos que bloquean el arranque de Apache
rm -f /etc/httpd/conf.d/ssl.conf
rm -f /run/httpd/* /tmp/httpd*

# 2. Permisos a la carpeta tomcat
chown -R tomcat:apache /usr/share/tomcat

# 3. Permisos a la carpte de reportes
chown -R tomcat:apache /vhosts/reportes

# 4. Iniciar Apache en segundo plano
echo "Iniciando Apache..."
/usr/sbin/httpd -k start

# 5. Iniciar Tomcat
# Usamos 'run' para que el script no termine y el contenedor siga vivo
#exec /usr/share/tomcat/bin/catalina.sh run
echo "Iniciando Tomcat como usuario tomcat..."
exec runuser -u tomcat -- /usr/share/tomcat/bin/catalina.sh run