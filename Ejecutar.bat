@echo off
echo =========================================
echo 🚀 Levantando EmpujeComunitario completo
echo =========================================

REM --- 1. Levantar Docker (MySQL + Kafka + Zookeeper) ---
echo 🐳 Iniciando Docker containers (infra/kafka)...
cd infra\kafka
docker-compose up -d
if errorlevel 1 (
    echo ❌ Error al levantar docker-compose
    pause
    exit /b
)
cd ..\..   REM volver a la raíz del repo

REM --- 2. Levantar servidor Python gRPC ---
echo 🐍 Iniciando servidor gRPC en Python...
start cmd /k "cd /d ServerPython && .venv\Scripts\activate && python server.py"

REM --- 3. Levantar cliente Spring Boot ---
echo 🌱 Iniciando cliente Spring Boot...
start cmd /k "cd /d ClienteSpring && mvn spring-boot:run"

echo =========================================
echo ✅ Todo levantado: Docker + Python gRPC + Spring Boot
echo =========================================
pause
