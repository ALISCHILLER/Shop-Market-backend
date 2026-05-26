@echo off
set APP_HOME=%~dp0
set WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if exist "%WRAPPER_JAR%" (
  java -jar "%WRAPPER_JAR%" %*
  exit /b %ERRORLEVEL%
)
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle wrapper jar is not present in this generated archive.
echo Install Gradle 9.1+ and run: gradle wrapper --gradle-version 9.1.0
echo Then run: gradlew clean bootRun
exit /b 1
