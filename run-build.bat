@echo off
echo Applying Spotless formatting...
gradlew spotlessApply

echo Running clean build...
gradlew clean build

echo Build complete!
pause
