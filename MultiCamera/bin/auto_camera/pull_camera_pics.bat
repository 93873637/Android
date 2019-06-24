@echo off

set LOCAL_PATH=D:\temp
set CAMERA_PIC_PATH=/sdcard/DCIM/Camera/
set CAMERA_DUMP_PATH=/data/misc/camera/

CALL:GetTimeFormat
set LOCAL_PICS_PATH=%LOCAL_PATH%\camera_pics_%currentTimeFormat%
set LOCAL_DUMP_PATH=%LOCAL_PATH%\camera_pics_%currentTimeFormat%\dump
echo Pull camera pics from device to "%LOCAL_PICS_PATH%"...

CALL:RootDevice
echo.
echo remount device...
adb remount
adb wait-for-device
echo ok.

echo.
echo File Number of %CAMERA_PIC_PATH%:
adb shell ls -l %CAMERA_PIC_PATH% | wc -l

echo.
echo pull %CAMERA_PIC_PATH% %LOCAL_PICS_PATH%
adb pull %CAMERA_PIC_PATH% %LOCAL_PICS_PATH%

echo.
echo pull %CAMERA_DUMP_PATH% %LOCAL_DUMP_PATH%
adb pull %CAMERA_DUMP_PATH% %LOCAL_DUMP_PATH%

start %LOCAL_PICS_PATH%

rem #echo. 
rem #pause
GOTO:EOF

REM ########################################################################
REM #COMMON FUNCS

REM ##
REM #GetTimeFormat: get current time string, format as YY.MMDD.hhmmss
REM #Result saved in variable currentTimeFormat
REM ##
:GetTimeFormat
  FOR /F "tokens=1-3 delims=/ " %%i IN ("%date:~2,10%") DO SET d=%%i.%%j%%k
  set t=%time:~0,2%%time:~3,2%%time:~6,2%
  if "%t:~0,1%"==" " set t=0%time:~1,1%%time:~3,2%%time:~6,2%
  set currentTimeFormat=%d%.%t%
GOTO:EOF

REM ##
REM #RootDevice: root device, and wait for device ready
REM ##
:RootDevice
  echo.
  echo Root device...
  adb root
  adb wait-for-device
  :check_root
    adb shell getprop|findstr "sys.usb.config" >nul
    if %errorlevel% equ 0 (
      echo OK.
    ) else (
      ping /n 2 127.1>nul
      goto check_root
    )
GOTO:EOF

REM ########################################################################
