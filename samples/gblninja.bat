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
set "EXPECTING_VALUE=false"

:parse_loop
if "%~1"=="" goto execute_command

set "ARG=%~1"

REM Handle help options
if "%ARG%"=="--help" (
    set "ARGS=!ARGS! --help"
    goto next_arg
)
if "%ARG%"=="-h" (
    set "ARGS=!ARGS! --help"
    goto next_arg
)

REM Handle info/gblinfo options
if "%ARG%"=="-i" (
    set "ARGS=!ARGS! --gblinfo"
    goto next_arg
)
if "%ARG%"=="--gblinfo" (
    set "ARGS=!ARGS! --gblinfo"
    goto next_arg
)

REM Handle create options
if "%ARG%"=="-c" (
    set "ARGS=!ARGS! --gblcreate"
    goto next_arg
)
if "%ARG%"=="--gblcreate" (
    set "ARGS=!ARGS! --gblcreate"
    goto next_arg
)

REM Handle main commands (simplified - no automatic tag type extraction)
if "%ARG%"=="--pack" (
    set "ARGS=!ARGS! --pack"
    goto next_arg
)
if "%ARG%"=="--add" (
    set "ARGS=!ARGS! --add"
    goto next_arg
)
if "%ARG%"=="--remove" (
    set "ARGS=!ARGS! --remove"
    goto next_arg
)
if "%ARG%"=="--set" (
    set "ARGS=!ARGS! --set"
    goto next_arg
)
if "%ARG%"=="--create" (
    set "ARGS=!ARGS! --create"
    goto next_arg
)

REM Handle file options
if "%ARG%"=="-f" (
    set "ARGS=!ARGS! --file"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--file" (
    set "ARGS=!ARGS! --file"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle output options
if "%ARG%"=="-o" (
    set "ARGS=!ARGS! --output"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--output" (
    set "ARGS=!ARGS! --output"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle format options
if "%ARG%"=="-F" (
    set "ARGS=!ARGS! --format"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--format" (
    set "ARGS=!ARGS! --format"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle type options
if "%ARG%"=="-t" (
    set "ARGS=!ARGS! --type"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--type" (
    set "ARGS=!ARGS! --type"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle index options (unified)
if "%ARG%"=="--index" (
    set "ARGS=!ARGS! --index"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle version options
if "%ARG%"=="-v" (
    set "ARGS=!ARGS! --version"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--version" (
    set "ARGS=!ARGS! --version"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle metadata options
if "%ARG%"=="-m" (
    set "ARGS=!ARGS! --metadata"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--metadata" (
    set "ARGS=!ARGS! --metadata"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle address options
if "%ARG%"=="-a" (
    set "ARGS=!ARGS! --address"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--address" (
    set "ARGS=!ARGS! --address"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle size options
if "%ARG%"=="-s" (
    set "ARGS=!ARGS! --size"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--size" (
    set "ARGS=!ARGS! --size"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle data options
if "%ARG%"=="-d" (
    set "ARGS=!ARGS! --data"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--data" (
    set "ARGS=!ARGS! --data"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle nonce options
if "%ARG%"=="-n" (
    set "ARGS=!ARGS! --nonce"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--nonce" (
    set "ARGS=!ARGS! --nonce"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle r-value options
if "%ARG%"=="-r" (
    set "ARGS=!ARGS! --r-value"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--r-value" (
    set "ARGS=!ARGS! --r-value"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle s-value options
if "%ARG%"=="--s-value" (
    set "ARGS=!ARGS! --s-value"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle application-specific options
if "%ARG%"=="--app-type" (
    set "ARGS=!ARGS! --app-type"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--app-version" (
    set "ARGS=!ARGS! --app-version"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--capabilities" (
    set "ARGS=!ARGS! --capabilities"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--product-id" (
    set "ARGS=!ARGS! --product-id"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle encryption/compression options
if "%ARG%"=="--msg-len" (
    set "ARGS=!ARGS! --msg-len"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--dependency" (
    set "ARGS=!ARGS! --dependency"
    set "EXPECTING_VALUE=true"
    goto next_arg
)
if "%ARG%"=="--decompressed-size" (
    set "ARGS=!ARGS! --decompressed-size"
    set "EXPECTING_VALUE=true"
    goto next_arg
)

REM Handle legacy shortcuts for file specification
if "!PREV_ARG!"=="-i" (
    if not "!ARG:~0,1!"=="-" (
        set "ARGS=!ARGS! --file !ARG!"
        goto next_arg
    )
)

if "!PREV_ARG!"=="-c" (
    if not "!ARG:~0,1!"=="-" (
        set "ARGS=!ARGS! --file !ARG!"
        goto next_arg
    )
)

REM If we're expecting a value and this doesn't start with -, treat as value
if "!EXPECTING_VALUE!"=="true" (
    if not "!ARG:~0,1!"=="-" (
        set "ARGS=!ARGS! !ARG!"
        set "EXPECTING_VALUE=false"
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