@echo off
setlocal
set VERSION=9.5.1
set ROOT=%~dp0
set DIST=%ROOT%.gradle-dist\gradle-%VERSION%
set ZIP=%ROOT%.gradle-dist\gradle-%VERSION%-bin.zip
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%ROOT%.gradle-dist" mkdir "%ROOT%.gradle-dist"
  if not exist "%ZIP%" powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%ZIP%'"
  powershell -NoProfile -Command "Expand-Archive -Force '%ZIP%' '%ROOT%.gradle-dist'"
)
call "%DIST%\bin\gradle.bat" %*
