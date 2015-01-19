WORKDIR=`dirname $0`
JARS="$WORKDIR/../Jars"
java -debug -classpath "$JARS/iZomateRemoteServer.jar":"$JARS/iZomateCore.jar":"$JARS/sigar/*" iZomateRemoteServer.RemoteServerMain 