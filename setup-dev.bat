@echo off
REM Development setup script for Windows

echo Setting up Water Level Monitoring Backend development environment...

REM Check if Java 21 is installed
java -version 2>nul
if %errorlevel% neq 0 (
    echo Error: Java 21 is required but not found in PATH
    exit /b 1
)

REM Check Gradle
gradlew --version
if %errorlevel% neq 0 (
    echo Error: Gradle wrapper not found
    exit /b 1
)

REM Set up Git hooks
echo Setting up Git hooks...
git config core.hooksPath .githooks
if exist .githooks\pre-commit (
    echo Git hooks configured successfully
) else (
    echo Warning: Pre-commit hook not found
)

REM Create logs directory
if not exist logs mkdir logs

REM Apply code formatting
echo Applying code formatting...
gradlew spotlessApply
if %errorlevel% neq 0 (
    echo Warning: Code formatting failed
)

REM Run initial tests
echo Running initial tests...
gradlew test
if %errorlevel% neq 0 (
    echo Warning: Some tests failed
)

echo.
echo Development environment setup complete!
echo.
echo Available commands:
echo   gradlew bootRun              - Start the application
echo   gradlew test                 - Run tests
echo   gradlew verify               - Run all quality checks
echo   gradlew spotlessApply        - Format code
echo   gradlew jacocoTestReport     - Generate coverage report
echo   gradlew sonar                - Run SonarQube analysis
echo.
echo Access points:
echo   Application: http://localhost:8080/api
echo   Swagger UI:  http://localhost:8080/api/swagger-ui.html
echo   H2 Console:  http://localhost:8080/api/h2-console
echo.
pause
