#!/bin/bash
# ./run-server-report.sh
path="/home/jhon/public_html/servidor-reportes"
cd "$path"

echo "=== Verificando puertos (No deben aparecer, si aparece alguno, se debe cambiar a uno disponible) ==="
ss -antlp | grep -E '8880|5505'

echo "=== iniciar contenedor ==="
docker compose up -d --build
sleep 8

echo -e "\n=== Fin Despliegue Exitoso ==="