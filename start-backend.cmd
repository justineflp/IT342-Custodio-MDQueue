@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-19"
cd /d "%~dp0"
call mvnw.cmd spring-boot:run
