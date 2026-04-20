@echo off
if not exist out (
    echo Build first: compile.bat
    pause
    exit /b 1
)
java -cp out gymtracker.Main
