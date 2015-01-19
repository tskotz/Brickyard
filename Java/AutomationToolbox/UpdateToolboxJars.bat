@echo off
REM Setting up Ant
REM Install Eclipse
REM Add the Ant that gets installed with eclipse to PATH:  
REM 	i.e. C:\Program Files\eclipse\plugins\org.apache.ant_1.8.3.v201301120609\bin
REM	Adde ANT_HOME env var.  Value:
REM	    C:\Program Files\eclipse\plugins\org.apache.ant_1.8.3.v201301120609
REM
REM Cloning TestAutomation
REM 	git clone ssh://monster/data/gitroot/misc/TestAutomation C:/TestAutomation

SET TESTAUTOREPO=C:\TestAutomation
SET AUTOTOOLBOX=C:\AutomationToolbox
SET PREFFILE=%AUTOTOOLBOX%\AutomationToolbox\Preferences\Prefs.xml
SET TEMPFILE=%AUTOTOOLBOX%\AutomationToolbox\Preferences\PrefsTemp.xml

echo.
echo ********************************************************
echo Updating Sources
echo ********************************************************
@echo on
cd %TESTAUTOREPO%
git reset --hard
@echo off
if %ERRORLEVEL% NEQ 0 GOTO EXIT0
@echo on
git clean -fdx
@echo off
if %ERRORLEVEL% NEQ 0 GOTO EXIT0
@echo on
git pull
@echo off
if %ERRORLEVEL% NEQ 0 GOTO EXIT0

echo.
echo.
echo ********************************************************
echo Building iZomateCore.jar
echo ********************************************************
call ant -buildfile %TESTAUTOREPO%\Java\iZomateCore\build_Core.xml
if %ERRORLEVEL% NEQ 0 GOTO EXIT0

echo.
echo.
echo ********************************************************
echo Building iZomateRemoteServer.jar
echo ********************************************************
call ant -buildfile %TESTAUTOREPO%\Java\iZomateRemoteServer\build_RemoteServer.xml
if %ERRORLEVEL% NEQ 0 GOTO EXIT0

echo.
echo.
echo ********************************************************
echo Building AutomationToolbox.jar
echo ********************************************************
call ant -buildfile %TESTAUTOREPO%\Java\AutomationToolbox\build_Toolbox.xml
if %ERRORLEVEL% NEQ 0 GOTO EXIT0

echo.
echo.
echo ********************************************************
echo Copying jars into place
echo ********************************************************

REM Create timestamp folder
For /f "tokens=2-4 delims=/ " %%a in ("%DATE%") do (
    SET YEAR=%%c
    SET MONTH=%%a
    SET DAY=%%b
)
For /f "tokens=1-4 delims=/:." %%a in ("%TIME%") do (
    SET HOUR=%%a
    SET MIN=%%b
    SET SEC=%%c
)
set TIMESTAMP=%YEAR%-%MONTH%-%DAY%_%HOUR%.%MIN%.%SEC%
set JARDIR=%AUTOTOOLBOX%\Jars-Archive\%TIMESTAMP: =0%

@echo on
mkdir %JARDIR%
cp -R %TESTAUTOREPO%/Java/Jars %JARDIR%
@echo off
if %ERRORLEVEL% NEQ 0 GOTO EXIT0

echo.
echo.
echo ********************************************************
echo Updating TestAutoImages
echo ********************************************************

@echo on
mkdir %AUTOTOOLBOX%\TestAutoImages
cp -R %TESTAUTOREPO%/Java/TestAutoImages/* %AUTOTOOLBOX%/TestAutoImages
@echo off
if %ERRORLEVEL% NEQ 0 GOTO EXIT0
@echo on

@echo off
echo.
echo.
echo ********************************************************
echo Updating Automation Toolbox Preferences file
echo ********************************************************

@echo on
sed "/<DefaultJars>/ c\    <DefaultJars>%JARDIR:\=\/%<\/DefaultJars>" %PREFFILE% > %TEMPFILE%
chmod 666 %TEMPFILE%
mv %TEMPFILE% %PREFFILE%

@echo off
echo.
echo ********************************************************
echo Update was Successfull
echo ********************************************************

@echo off
echo.
pause
:EXIT0