@echo off
:: Script de compilación standalone para Novelas360Provider
:: Creado: 2026-09-28

echo ========================================
echo  Compilador Novelas360Provider v2.0
echo  (Modo Standalone)
echo ========================================
echo.

:: Buscar JDK en Android Studio
set "ANDROID_STUDIO=D:\Program Files\Android\Android Studio"

echo [1/3] Configurando Java...

if exist "%ANDROID_STUDIO%\jbr\bin\java.exe" (
    set "JAVA_HOME=%ANDROID_STUDIO%\jbr"
    echo [OK] JDK encontrado
    goto :compile
)

if exist "%ANDROID_STUDIO%\jre\bin\java.exe" (
    set "JAVA_HOME=%ANDROID_STUDIO%\jre"
    echo [OK] JDK encontrado
    goto :compile
)

set "USER_JDK=%LOCALAPPDATA%\Android\Sdk\jbr"
if exist "%USER_JDK%\bin\java.exe" (
    set "JAVA_HOME=%USER_JDK%"
    echo [OK] JDK encontrado
    goto :compile
)

echo [ERROR] No se encontro el JDK
pause
exit /b 1

:compile
echo.
echo [2/3] Verificando Java...
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo [3/3] Compilando plugin...
echo.
echo NOTA: La primera vez puede tardar 10-15 minutos
echo descargando dependencias. Ten paciencia!
echo.

call gradlew.bat make --no-daemon --console=plain

if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo  ERROR EN LA COMPILACION
    echo ========================================
    echo.
    echo Revisa los errores arriba.
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  COMPILACION EXITOSA!
echo ========================================
echo.
echo Archivo generado:
dir "build\*.cs3" 2>nul
echo.
echo Ubicacion completa:
echo %CD%\build\Novelas360Provider.cs3
echo.
echo Para instalar:
echo 1. Copia el archivo a tu Android:
echo    /sdcard/Cloudstream3/plugins/
echo.
echo 2. Abre CloudStream ^> Configuracion ^> Extensiones
echo.
pause
