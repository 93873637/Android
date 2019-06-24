@echo off 

rem adb install BusList_1.0.18.0917.1418.apk
rem adb install ImageTools_1.0.2.apk
rem adb install NumberReader.apk
rem adb install whasai_1.0.18.0921.1048.apk

set work_path=.\
cd %work_path% 
for /R %%s in (*.apk) do ( 
    echo install %%s ...
    adb install %%s
)
pause 
