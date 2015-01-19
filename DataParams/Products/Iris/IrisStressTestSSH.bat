echo off
rem set WORKDIR=%CD%
set WORKDIR=C:/Program Files (x86)/iZotope/TestAutomation
set JAVAEXE=%WORKDIR%/JRE/jre7/bin/java.exe
set JARS=%WORKDIR%/Jars
set RESULTSDIR=%WORKDIR%/Results
set RESULTSFILE=IrisTest_ResultLog.html
set RESULTS_LINK_ARG=""
set BUILD_ARCHIVE_TAG=%1

if "%BUILD_ARCHIVE_TAG%"=="" goto continue
set RESULTS_LINK_ARG=-resultsLink http://buildarchive.izotope.int/archive/RX_Synth/Build-installer-%BUILD_ARCHIVE_TAG%/%RESULTSFILE%
echo set results link to %RESULTS_LINK_ARG%
:continue

cd "%WORKDIR%"

rem Run the test
"%JAVAEXE%" -debug -classpath "%JARS%/iZomateRemoteServer.jar";"%JARS%/iZomateCore.jar";"%JARS%/sigar/*";"%JARS%/mail.jar" iZomateCore.TestCore.IrisTest -testbed 127.0.0.1 -testDuration 30 -hideAllWinAtStart -pitchMode "Radius RT" -presetPlayTime 0 -submitCrashReport -useListBox -quitWhenComplete -logDir "%WORKDIR%/Results" -sendEmailTo rxsynth@izotope.com %RESULTS_LINK_ARG% -app "C:/Program Files (x86)/iZotope/Iris/win32/iZotope Iris.exe"

rem Copy it up if a build number was supplied
echo build archive tag = %BUILD_ARCHIVE_TAG%
if "%BUILD_ARCHIVE_TAG%"=="" goto done
cd "%RESULTSDIR%"
scp %RESULTSFILE% iztestauto@buildarchive.izotope.int:/build_archive/RX_Synth/Build-installer-%BUILD_ARCHIVE_TAG%
ssh iztestauto@buildarchive.izotope.int chmod a+rw /build_archive/RX_Synth/Build-installer-%BUILD_ARCHIVE_TAG%/%RESULTSFILE%

:done
echo the test is done
rem pause