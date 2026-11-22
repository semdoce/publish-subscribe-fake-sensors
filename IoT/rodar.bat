@echo off
setlocal enabledelayedexpansion
chcp 65001 > nul

where java >nul 2>nul
if %errorlevel% equ 0 (
    set JAVA_CMD=java
) else (
    set JAVA_CMD="C:\Users\Witch\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.12.v20260826-1216\jre\bin\java.exe"
)

set CP=bin;lib\*

:menu
cls
echo ==================================================
echo         SISTEMA HOSPITALAR IOT - EXECUCAO
echo ==================================================
echo  [1] Iniciar Sistema (Sensores + Mediator - paciente1)
echo  [2] Iniciar Sistema com ID personalizado
echo  [3] Subscriber: Painel Enfermagem (fila.enfermagem)
echo  [4] Subscriber: Painel Medico (fila.medico.paciente1)
echo  [5] Subscriber: App Emergencia (fila.alerta)
echo  [6] Iniciar Apenas um Sensor Individual
echo  [7] Recompilar projeto
echo  [0] Sair
echo ==================================================
set /p opcao="Escolha uma opcao: "

if "%opcao%"=="1" (
    echo.
    echo Iniciando Sensores e Mediator para paciente1...
    %JAVA_CMD% -cp "%CP%" main.Main paciente1
    pause
    goto menu
)
if "%opcao%"=="2" (
    set /p pac="Digite o ID do paciente (ex: paciente2): "
    echo.
    echo Iniciando Sensores e Mediator para !pac!...
    %JAVA_CMD% -cp "%CP%" main.Main !pac!
    pause
    goto menu
)
if "%opcao%"=="3" (
    echo.
    echo Iniciando Painel Enfermagem...
    %JAVA_CMD% -cp "%CP%" subscriber.Subscriber enfermagem
    pause
    goto menu
)
if "%opcao%"=="4" (
    set /p pac="Digite o ID do paciente [padrao: paciente1]: "
    if "!pac!"=="" set pac=paciente1
    echo.
    echo Iniciando Painel Medico para !pac!...
    %JAVA_CMD% -cp "%CP%" subscriber.Subscriber medico !pac!
    pause
    goto menu
)
if "%opcao%"=="5" (
    set /p pac="Filtrar por paciente especifico? (deixe vazio para todos): "
    echo.
    if "!pac!"=="" (
        echo Iniciando App Emergencia para todos os pacientes...
        %JAVA_CMD% -cp "%CP%" subscriber.Subscriber emergencia
    ) else (
        echo Iniciando App Emergencia para !pac!...
        %JAVA_CMD% -cp "%CP%" subscriber.Subscriber emergencia !pac!
    )
    pause
    goto menu
)
if "%opcao%"=="6" (
    set /p pac="Digite o ID do paciente [padrao: paciente1]: "
    if "!pac!"=="" set pac=paciente1
    echo Tipos disponiveis: cardiaco, pressao, saturacao, temperatura
    set /p tipo="Digite o tipo do sensor: "
    echo.
    echo Iniciando Sensor !tipo! para !pac!...
    %JAVA_CMD% -cp "%CP%" sensors.PublicadorSensores !pac! !tipo!
    pause
    goto menu
)
if "%opcao%"=="7" (
    call compilar.bat
    pause
    goto menu
)
if "%opcao%"=="0" exit /b 0

echo Opcao invalida.
timeout /t 2 >nul
goto menu