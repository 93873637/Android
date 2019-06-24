@echo off

echo ***install apk...
adb install -r TestCamera.apk

echo ***run test...
adb shell am start -n com.liz.testcamera/com.liz.testcamera.activity.MainActivity

echo ***wait a while for testing over...
ping -n 8 -w 500 127.1>nul

echo ***press any key to stop apk...
pause
adb shell am force-stop com.liz.testcamera

echo ***pull pictures to show...
set WORK_DIR=%~dp0
call %WORK_DIR%pull_camera_pics.bat
