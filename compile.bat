@echo off
echo Compiling GymTracker...
if not exist out mkdir out
javac -d out -sourcepath src src\gymtracker\Main.java
if %errorlevel% == 0 (
    echo.
    echo  Compilation successful! Run with:  run.bat
) else (
    echo.
    echo  Compilation FAILED. Check errors above.
)
pause
