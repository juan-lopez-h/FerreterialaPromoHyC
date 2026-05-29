#!/bin/bash
echo "Limpiando y compilando proyecto..."
./mvnw clean compile
echo ""
echo "Para ejecutar la aplicación, usa: ./mvnw javafx:run"
echo "Para generar el paquete (JAR con dependencias), usa: ./mvnw package"
