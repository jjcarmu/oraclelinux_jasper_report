#!/bin/bash
# run-server-report.sh
path="/home/jhon/public_html/servidor-reportes"
cd "$path"

echo "=== Verificando puertos (No deben aparecer, si aparece alguno, se debe cambiar a uno disponible) ==="
ss -antlp | grep -E '8880|5505'

echo "=== iniciar contenedor ==="
docker compose up -d
sleep 8

echo "=== copiando variables de entorno (.bashrc) ==="
docker cp tomcat-config/.bashrc jasper_report:/root/.bashrc
#docker exec -it jasper_report bash -c "source /root/.bashrc"

echo "=== copiando variables de entorno (.setenv.sh) ==="
docker cp tomcat-config/usr/share/tomcat/bin/setenv.sh jasper_report:/usr/share/tomcat/bin/setenv.sh
docker exec -it jasper_report bash -c "chmod +x /usr/share/tomcat/bin/setenv.sh"
docker exec -it jasper_report bash -c "/usr/share/tomcat/bin/setenv.sh"

echo "=== start tomcap ==="
docker exec -it jasper_report bash -c "/usr/share/tomcat/bin/startup.sh"

#echo "=== ver log tomcap ==="
#docker exec -it jasper_report bash -c "tail /usr/share/tomcat/logs/catalina.out"

echo -e "\n=== Fin Despliegue Exitoso ==="