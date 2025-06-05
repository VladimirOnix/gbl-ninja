@echo off
setlocal enabledelayedexpansion

REM GBL Ninja - Wrapper script for gbl-ninja.jar
REM Usage: gblninja [options]

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    exit /b 1
)

REM Get the directory where this batch file is located
set "SCRIPT_DIR=%~dp0"

REM Path to the JAR file (assumes it's in the same directory as this script)
set "JAR_PATH=%SCRIPT_DIR%gbl-ninja.jar"

REM Check if JAR file exists
if not exist "%JAR_PATH%" (
    echo Error: gbl-ninja.jar not found at %JAR_PATH%
    echo Please make sure gbl-ninja.jar is in the same directory as this script
    exit /b 1
)

REM If no arguments provided, show help
if "%~1"=="" (
    java -jar "%JAR_PATH%"
    exit /b 0
)

REM Parse arguments and build command
set "ARGS="
set "PREV_ARG="

:parse_loop
if "%~1"=="" goto execute_command

set "ARG=%~1"

REM Handle short options
if "%ARG%"=="-i" (
    set "ARGS=!ARGS! --gblinfo"
    goto next_arg
)
if "%ARG%"=="-c" (
    set "ARGS=!ARGS! --gblcreate"
    goto next_arg
)
if "%ARG%"=="-f" (
    set "ARGS=!ARGS! --file"
    goto next_arg
)
if "%ARG%"=="-F" (
    set "ARGS=!ARGS! --format"
    goto next_arg
)
if "%ARG%"=="-t" (
    set "ARGS=!ARGS! --type"
    goto next_arg
)
if "%ARG%"=="-v" (
    set "ARGS=!ARGS! --version"
    goto next_arg
)
if "%ARG%"=="-m" (
    set "ARGS=!ARGS! --metadata"
    goto next_arg
)
if "%ARG%"=="-a" (
    set "ARGS=!ARGS! --address"
    goto next_arg
)
if "%ARG%"=="-s" (
    set "ARGS=!ARGS! --size"
    goto next_arg
)
if "%ARG%"=="-d" (
    set "ARGS=!ARGS! --data"
    goto next_arg
)
if "%ARG%"=="-n" (
    set "ARGS=!ARGS! --nonce"
    goto next_arg
)
if "%ARG%"=="-r" (
    set "ARGS=!ARGS! --r-value"
    goto next_arg
)

REM Handle special case: if previous arg was -i and current doesn't start with -, treat as filename
if "!PREV_ARG!"=="-i" (
    if not "!ARG:~0,1!"=="-" (
        set "ARGS=!ARGS! --file !ARG!"
        goto next_arg
    )
)

REM Handle special case: if previous arg was -c and current doesn't start with -, treat as filename  
if "!PREV_ARG!"=="-c" (
    if not "!ARG:~0,1!"=="-" (
        set "ARGS=!ARGS! --file !ARG!"
        goto next_arg
    )
)

REM Default: pass argument as-is
set "ARGS=!ARGS! !ARG!"

:next_arg
set "PREV_ARG=%ARG%"
shift
goto parse_loop

:execute_command
REM Execute the Java command
echo Executing: java -jar "%JAR_PATH%" !ARGS!
java -jar "%JAR_PATH%" !ARGS!

REM Exit with the same code as the Java application
exit /b %errorlevel%