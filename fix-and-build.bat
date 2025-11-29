@echo off
echo Applying Spotless code formatting...
gradlew spotlessApply

if %errorlevel% equ 0 (
    echo Spotless formatting applied successfully!
) else (
    echo Spotless formatting failed!
    exit /b 1
)

echo Running build after formatting...
gradlew clean build

if %errorlevel% equ 0 (
    echo Build completed successfully!
) else (
    echo Build failed!
    exit /b 1
)

echo All tasks completed!
pause
