FROM jjcarmu/oraclelinux_report:1.0

# 1. Instala las dependencias necesarias ( wget, tar, gzip, apache, nano)
RUN dnf update -y && \
    dnf install -y wget tar gzip httpd nano && \
    dnf clean all

# 2. Variable de entorno JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/temurin-17-jdk

# 3. Variable de entorno PATH
ENV PATH=$JAVA_HOME/bin:$PATH

# 4. Variable de entorno JRE_HOME
ENV JRE_HOME=/usr/lib/jvm/temurin-17-jdk

# 5. Variable de entorno JAVA_OPTS
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Xms512m -Xmx1024m"

# 6. Variable de entorno CATALINA_PID
ENV CATALINA_PID="$CATALINA_BASE/temp/tomcat.pid"

# 7. Variable de entorno CATALINA_OPTS
ENV CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# 8. Variable de entorno JASPER_HOME
ENV JASPER_HOME="/usr/share/tomcat"

# 9. Variable de entorno TOMCAT_USER
ENV TOMCAT_USER="tomcat"

# 10. Seguridad - Variable de entorno SECURITY_MANAGER
ENV SECURITY_MANAGER="false"

# 11. Copiamos variables de entorno
COPY ./tomcat-config/.bashrc /root/.bashrc

# 12. Copiamos variables de entorno para el tomcat
COPY ./tomcat-config/usr/share/tomcat/bin/setenv.sh /usr/share/tomcat/bin/setenv.sh

# 13.
#RUN httpd -k stop
#RUN mv /etc/httpd/conf.d/*.conf /tmp/

# 13. Copiamos archivo con virtualHosts para apache
COPY ./tomcat-config/etc/httpd/conf.d/reportes.conf /etc/httpd/conf.d/reportes.conf

# 14. Damos permiso de ejecución
RUN chmod +x /usr/share/tomcat/bin/setenv.sh

# 15. Ejecutamos las variables de entorno
RUN /usr/share/tomcat/bin/setenv.sh

# 16. Copiamos el código fuente desde el host hacia la imagen
COPY ./jasper-servlet /opt/jasper-servlet

# 17. Nos ubicamos en el directorio del código fuente
WORKDIR /opt/jasper-servlet

# 18. Compilamos el proyecto para generar el .war
RUN mvn clean package -DskipTests

# 19. Copiamos el WAR generado a la carpeta webapps de Tomcat con el nuevo nombre
RUN cp target/reportes.war /usr/share/tomcat/webapps/reportesJasper.war

# 20. Asignamos los permisos correctos al usuario tomcat
#RUN chown -R tomcat:tomcat /usr/share/tomcat/webapps/

# 21. Se crea el directorio para la generacion de los reportes del SIAAP
RUN mkdir -p /vhosts/reportes/siaap

# 22. Agrega el usuario apache al grupo tomcat
RUN usermod -a -G tomcat apache

# 23. Asignamos los permisos de usuario y grupo al directorio de la generacion de reportes
#RUN chown -R nginx:nginx /vhosts/reportes
#RUN chown -R apache:apache /vhosts/reportes
#RUN chown -R tomcat:tomcat /vhosts/reportes
#RUN chown -R tomcat:apache /vhosts/reportes

# 24. Asignamos los permisos al directorio donde se genera los reportes
RUN chmod -R 2755 /vhosts/reportes/

# Asignamos el puerto global de apache
#RUN sed -i 's/Listen 80/Listen 8080/' /etc/httpd/conf/httpd.conf

# 25. Se copia el Script para el arranque unificado en el directorio raiz
COPY ./tomcat-config/entrypoint.sh /entrypoint.sh

# 26. Ubicamos en el directorio Raiz
WORKDIR /

# 27. Le damos permisos de ejecucion a entrypoint.sh
RUN chmod +x /entrypoint.sh

# 28. Expone el puerto por defecto de Tomcat
EXPOSE 80 8080

# 29. Comando de arranque unificado
ENTRYPOINT ["/entrypoint.sh"]

# Otras forma de iniciar tomcat y apache
#RUN httpd -D FOREGROUND
#RUN httpd -k start
#CMD ["/usr/share/tomcat/bin/catalina.sh", "run"]
#CMD ["httpd", "-D", "FOREGROUND"]
# Comando de arranque apache
#CMD ["httpd", "-k", "start"]
