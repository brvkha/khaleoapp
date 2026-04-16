@echo off
setlocal enableextensions

set "BASE_DIR=%~dp0"
set "MAVEN_VERSION=3.9.9"
set "WRAPPER_DIR=%BASE_DIR%.mvn\wrapper"
set "MAVEN_HOME=%BASE_DIR%.mvn\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ZIP=%BASE_DIR%.mvn\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%" >nul 2>nul
if not exist "%BASE_DIR%.mvn" mkdir "%BASE_DIR%.mvn" >nul 2>nul

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo [mvnw] Downloading Apache Maven %MAVEN_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference = 'Stop';" ^
    "$zip = '%MAVEN_ZIP%';" ^
    "$dir = '%BASE_DIR%.mvn';" ^
    "$url = '%MAVEN_URL%';" ^
    "if (-not (Test-Path $zip)) { Invoke-WebRequest -Uri $url -OutFile $zip }" ^
    "if (Test-Path '%MAVEN_HOME%') { Remove-Item -Recurse -Force '%MAVEN_HOME%' }" ^
    "Expand-Archive -Path $zip -DestinationPath $dir -Force"
  if errorlevel 1 exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*

