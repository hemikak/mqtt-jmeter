@echo OFF

set JMETER_HOME=%1
set HOME=%cd%
set MQTT_JAR=mqtt-jmeter.jar
set JMETER_JAR=ApacheJMeter_core.jar
set JMETER_JAR_EXT_FOLDER=%JMETER_HOME%\lib\ext

if "%JMETER_HOME%"=="" (
    echo USAGE: "installer.bat C:\path\to\jmeter"
    exit /B 1
)

if not exist %JMETER_HOME%\NUL ( 
    echo Directory "%JMETER_HOME%" does not exists.
    exit /B 1
)

if not exist %MQTT_JAR% (
    echo Distribution file "%MQTT_JAR%" not exists.
    exit /B 1
)

if not exist %JMETER_JAR_EXT_FOLDER%\%JMETER_JAR% (
    echo File "%JMETER_JAR_EXT_FOLDER%\%JMETER_JAR%" does not exists. Reinstall JMETER.
    exit /B 1
)

echo Copying mqtt-jmeter jars..
copy %HOME%\%MQTT_JAR% %JMETER_JAR_EXT_FOLDER%

set JMETER_JAR_BACKUP=%JMETER_JAR%.backup
if not exist %JMETER_JAR_EXT_FOLDER%\%JMETER_JAR_BACKUP% (
    echo Creating backup of "%JMETER_JAR%"..
    copy %JMETER_JAR_EXT_FOLDER%\%JMETER_JAR% %JMETER_JAR_EXT_FOLDER%\%JMETER_JAR_BACKUP%
) else (
    echo Backup of "%JMETER_JAR%" exist, don't need to create..
)

set MESSAGES=org\apache\jmeter\resources\messages.properties
set MESSAGES_TMP=MESSAGES.tmp

echo Exctract "%MESSAGES%"..
jar -xfv %JMETER_JAR_EXT_FOLDER%\%JMETER_JAR% %MESSAGES%

move %MESSAGES% %MESSAGES_TMP%

::echo "convert to unix format.."
::dos2unix $MESSAGES_TMP

echo Adding values for mqtt-jmeter to "messages.properties"..
type %HOME%\messages.properties >> %MESSAGES_TMP%

echo Sorting "messages.properties"..
sort %MESSAGES_TMP% > %MESSAGES%

echo Saving "messages.properties" to "%JMETER_JAR_EXT_FOLDER%\%JMETER_JAR%"..
jar -uvf %JMETER_JAR_EXT_FOLDER%\%JMETER_JAR% %MESSAGES%

echo Removing work files..
del /Q org\

echo Done.