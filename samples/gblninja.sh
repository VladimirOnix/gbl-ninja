#!/bin/bash

# GBL Ninja - Wrapper script for gbl-ninja.jar
# Usage: gblninja [options]

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Get the directory where this script is located
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

# Path to the JAR file (assumes it's in the same directory as this script)
JAR_PATH="$SCRIPT_DIR/gbl-ninja.jar"

# Check if JAR file exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: gbl-ninja.jar not found at $JAR_PATH"
    echo "Please make sure gbl-ninja.jar is in the same directory as this script"
    exit 1
fi

# If no arguments provided, show help
if [ $# -eq 0 ]; then
    java -jar "$JAR_PATH"
    exit 0
fi

# Parse arguments and build command
ARGS=()
PREV_ARG=""

while [ $# -gt 0 ]; do
    ARG="$1"
    
    case "$ARG" in
        -i)
            ARGS+=(--gblinfo)
            ;;
        -c)
            ARGS+=(--gblcreate)
            ;;
        -f)
            ARGS+=(--file)
            ;;
        -F)
            ARGS+=(--format)
            ;;
        -t)
            ARGS+=(--type)
            ;;
        -v)
            ARGS+=(--version)
            ;;
        -m)
            ARGS+=(--metadata)
            ;;
        -a)
            ARGS+=(--address)
            ;;
        -s)
            ARGS+=(--size)
            ;;
        -d)
            ARGS+=(--data)
            ;;
        -n)
            ARGS+=(--nonce)
            ;;
        -r)
            ARGS+=(--r-value)
            ;;
        *)
            # Handle special cases: if previous arg was -i or -c and current doesn't start with -, treat as filename
            if [[ "$PREV_ARG" == "-i" && ! "$ARG" =~ ^- ]]; then
                ARGS+=(--file "$ARG")
            elif [[ "$PREV_ARG" == "-c" && ! "$ARG" =~ ^- ]]; then
                ARGS+=(--file "$ARG")
            else
                # Default: pass argument as-is
                ARGS+=("$ARG")
            fi
            ;;
    esac
    
    PREV_ARG="$ARG"
    shift
done

# Execute the Java command
echo "Executing: java -jar \"$JAR_PATH\" ${ARGS[*]}"
java -jar "$JAR_PATH" "${ARGS[@]}"

# Exit with the same code as the Java application
exit $?