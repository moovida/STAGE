#!/bin/bash

# STAGE start script for UNIX based Systems

# DATA TO BE CONFIGURED
# START
INSTALLDIR=`pwd`
WORKSPACE=$INSTALLDIR/STAGEWORKSPACE
PORT=10000
MAXHEAP=4g
# END



export VMARGS="-Djava.awt.headless=true -Xverify:none -server -XX:+TieredCompilation -Xmx$MAXHEAP -XX:NewRatio=4 -XX:+UseG1GC -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 -XX:SoftRefLRUPolicyMSPerMB=1000"
export ARGS="-console -consolelog -registryMultiLanguage"
export LOGARGS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.eclipse.equinox.http.jetty.log.stderr.threshold=info"


hardware=`uname -i`

EXEC=stage32
JRE=$INSTALLDIR/jre32/
if [ "$hardware" = "x86_64" ]
then
   JRE=$INSTALLDIR/jre64/
   EXEC=stage64
fi

echo "INSTALLDIR = $INSTALLDIR"
echo "WORKSPACE = $WORKSPACE"
echo "JAVA = $JRE/bin/java"
echo "./$EXEC -vm $JRE/bin/java $ARGS -vmargs $VMARGS -Dorg.osgi.service.http.port=$PORT -Dstage.workspace=$WORKSPACE $LOGARGS"
./$EXEC -vm $JRE/bin/java $ARGS -vmargs $VMARGS -Dorg.osgi.service.http.port=$PORT -Dstage.workspace=$WORKSPACE $LOGARGS