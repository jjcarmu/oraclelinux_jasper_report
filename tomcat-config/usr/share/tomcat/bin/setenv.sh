export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk
export JRE_HOME=/usr/lib/jvm/temurin-17-jdk
export JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Xms512m -Xmx1024m"
export CATALINA_PID="$CATALINA_BASE/temp/tomcat.pid"
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
export JASPER_HOME="/usr/share/tomcat"
# Usuario que ejecuta Tomcat
export TOMCAT_USER="tomcat"
# Seguridad
export SECURITY_MANAGER="false"

export JDK_JAVA_OPTIONS="
  --add-opens=java.base/java.lang=ALL-UNNAMED
  --add-opens=java.base/java.io=ALL-UNNAMED
  --add-opens=java.base/java.util=ALL-UNNAMED
  --add-opens=java.base/java.util.concurrent=ALL-UNNAMED
  --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED
"