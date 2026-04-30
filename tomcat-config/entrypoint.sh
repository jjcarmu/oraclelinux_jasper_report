#!/bin/bash

# 1. Eliminar archivos que bloquean el arranque de Apache
rm -f /etc/httpd/conf.d/ssl.conf
rm -f /run/httpd/* /tmp/httpd*

# 2. Iniciar Apache en segundo plano
echo "Iniciando Apache..."
/usr/sbin/httpd -k start

# 3. Iniciar Tomcat
echo "Iniciando Tomcat..."
# Usamos 'run' para que el script no termine y el contenedor siga vivo
exec /usr/share/tomcat/bin/catalina.sh run