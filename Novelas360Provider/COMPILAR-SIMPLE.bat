@echo off
:: Script de compilación con más información de debug
:: Creado: 2026-09-28

echo ========================================
echo  Compilador con Debug Info
echo ========================================
echo.

:: Buscar JDK
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

echo [ERROR] No se encontro el JDK
pause
exit /b 1

:compile
echo.
echo [2/3] Verificando Java...
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo [3/3] Compilando con informacion detallada...
echo.

call gradlew.bat make --stacktrace --info

if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo  ERROR - Revisa los logs arriba
    echo ========================================
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  EXITO!
echo ========================================
echo.
dir "build\*.cs3"
echo.
pause
