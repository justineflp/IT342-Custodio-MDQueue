@echo off
title MDQueue Mobile Launcher
color 0B
echo ==================================================
echo             MDQUEUE MOBILE LAUNCHER
echo ==================================================
echo.

set ADB_PATH="C:\Users\Justine Filip\AppData\Local\Android\Sdk\platform-tools\adb.exe"
set GRADLE_PATH="C:\Users\Justine Filip\.gradle\wrapper\dists\gradle-9.3.1-bin\23ovyewtku6u96viwx3xl3oks\gradle-9.3.1\bin\gradle.bat"
set APK_PATH="C:\Users\Justine Filip\mdqueue\mobile\app\build\outputs\apk\debug\app-debug.apk"

echo [1/4] Connecting to BlueStacks on Port 5556...
%ADB_PATH% connect localhost:5556
echo.

echo [2/4] Waiting 2 seconds for ADB connection to stabilize...
ping 127.0.0.1 -n 3 >nul
echo.

echo [3/4] Verifying active devices...
%ADB_PATH% devices
echo.

echo [4/4] Compiling and Building Debug APK...
cd /d "C:\Users\Justine Filip\mdqueue\mobile"
call %GRADLE_PATH% assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Gradle Build Failed! Please check the logs.
    pause
    exit /b %ERRORLEVEL%
)
echo.

echo [5/5] Installing and Launching MDQueue on BlueStacks...
%ADB_PATH% -s localhost:5556 install -r %APK_PATH%
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ⚠️ Specific BlueStacks target failed, trying default installation...
    %ADB_PATH% install -r %APK_PATH%
    if %ERRORLEVEL% NEQ 0 (
        echo ❌ Installation failed! Please make sure your BlueStacks has ADB enabled.
        pause
        exit /b %ERRORLEVEL%
    )
)

%ADB_PATH% -s localhost:5556 shell am start -n edu.cit.custodio.mdqueue/edu.cit.custodio.mdqueue.features.auth.view.LoginActivity
if %ERRORLEVEL% NEQ 0 (
    %ADB_PATH% shell am start -n edu.cit.custodio.mdqueue/edu.cit.custodio.mdqueue.features.auth.view.LoginActivity
)

echo.
echo 🎉 App started successfully!
echo.
timeout /t 5
