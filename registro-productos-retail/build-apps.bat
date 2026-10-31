@echo off
rem Genera las 2 apps de Windows (con Java incluido) en la carpeta dist\
rem   dist\GestionProductos\GestionProductos.exe  -> Login + CRUD de productos
rem   dist\SistemaCaja\SistemaCaja.exe            -> Sistema de caja
rem Uso: doble clic, o "build-apps.bat accesos" para crear tambien los accesos directos en el escritorio.
setlocal
cd /d "%~dp0"

rem Busca el JDK (necesita jpackage, incluido desde Java 14)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JDK=%JAVA_HOME%\bin"
if not defined JDK for /d %%D in ("C:\Program Files\Java\jdk-*") do if exist "%%D\bin\jpackage.exe" set "JDK=%%D\bin"
if not defined JDK (
    echo No se encontro un JDK con jpackage. Instale JDK 21 o defina JAVA_HOME.
    pause
    exit /b 1
)
echo Usando JDK: %JDK%

echo.
echo [1/4] Limpiando build anterior...
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build\classes build\input

echo [2/4] Compilando...
"%JDK%\javac" -encoding UTF-8 -d build\classes Conexion.java Login.java Moneda.java RegistroProductosRetail.java SistemaCaja.java || goto :error
"%JDK%\jar" --create --file build\input\retail.jar -C build\classes . || goto :error
copy /y lib\*.jar build\input >nul || goto :error

echo [3/4] Empaquetando GestionProductos...
"%JDK%\jpackage" --type app-image --dest dist --input build\input --main-jar retail.jar ^
    --main-class Login --name GestionProductos --app-version 1.0 ^
    --description "Login y gestion de productos" || goto :error

echo [4/4] Empaquetando SistemaCaja...
"%JDK%\jpackage" --type app-image --dest dist --input build\input --main-jar retail.jar ^
    --main-class SistemaCaja --name SistemaCaja --app-version 1.0 ^
    --description "Sistema de caja" || goto :error

if /i "%~1"=="accesos" call :accesos

echo.
echo Listo. Apps generadas en:
echo   %CD%\dist\GestionProductos\GestionProductos.exe
echo   %CD%\dist\SistemaCaja\SistemaCaja.exe
if /i not "%~1"=="accesos" pause
exit /b 0

:accesos
echo Creando accesos directos en el escritorio...
powershell -NoProfile -Command ^
  "$d=[Environment]::GetFolderPath('Desktop'); $s=New-Object -ComObject WScript.Shell;" ^
  "foreach($n in 'GestionProductos','SistemaCaja'){ $l=$s.CreateShortcut(\"$d\$n.lnk\"); $l.TargetPath=\"%CD%\dist\$n\$n.exe\"; $l.WorkingDirectory=\"%CD%\dist\$n\"; $l.Save() }"
exit /b 0

:error
echo.
echo ERROR: fallo la generacion de las apps.
pause
exit /b 1
