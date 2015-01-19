set WORKDIR=%CD%/..
set JAVAEXE=%WORKDIR%/JRE/jre7/bin/java.exe
set JARS=%WORKDIR%/Jars
"%JAVAEXE%" -debug -classpath "%JARS%/iZomateRemoteServer.jar";"%JARS%/iZomateCore.jar";"%JARS%/sigar/*" iZomateRemoteServer.RemoteServerMain 
pause