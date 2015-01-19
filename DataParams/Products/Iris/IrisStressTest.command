WORKDIR=`dirname $0`
JARS="$WORKDIR/Jars"
java -debug -classpath "$JARS/iZomateRemoteServer.jar":"$JARS/iZomateCore.jar":"$JARS/sigar/*" iZomateCore.TestCore.IrisTest -testbed 127.0.0.1 -app "C:/Program Files (x86)/iZotope/Iris/win32/iZotope Iris.exe" -logDir "$WORKDIR/Results" -testDuration 30 -xhideAllWinAtStart -presetPlayTime 0 -pitchMode "Radius RT" -useListBox -xquitWhenComplete -xsubmitCrashReport -xsendEmailTo jdoe@izotope.com -xresultsLink http://my/link.htm
pause

