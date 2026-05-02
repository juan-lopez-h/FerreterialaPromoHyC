@echo off
echo Limpiando y empaquetando proyecto...
call .\mvnw clean package -DskipTests
echo.
if exist target\dist\FerreteriaLaPromoHC-1.0.0.exe (
    echo Instalador creado exitosamente en target\dist
    if not exist dist mkdir dist
    copy target\dist\FerreteriaLaPromoHC-1.0.0.exe dist\FerreteriaLaPromoHC-1.0.0.exe
    echo Copiado a carpeta dist\
) else (
    echo Error al crear el instalador.
)
pause