@echo off 

set work_path=.\
cd %work_path% 
for /R %%s in (*.apk) do (
    echo. 
    echo install %%s ...
    adb install %%s
    echo.
)
pause 
