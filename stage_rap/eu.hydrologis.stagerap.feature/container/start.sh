#!/bin/bash -v

# Simple start script
#   - set working dir the INSTALLDIR (allow eclipse exe to find runtime)
#   - find Java (hard wired)
#   - find the workspace (hard wired)
#   - define Java param and app params

INSTALLDIR=`dirname $0`
WORKSPACE=/home/falko/servers/virgo-nano-rap-3.7.0.CI-2014-11-24_03-37-00/
PORT=10000
JAVA_HOME=/home/falko/bin/jdk1.8

cd $INSTALLDIR

export VMARGS='-Djava.awt.headless=true -Xverify:none -server -XX:+TieredCompilation -Xmx1024m -XX:NewRatio=4 -XX:+UseG1GC -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 -XX:SoftRefLRUPolicyMSPerMB=1000'
export ARGS='-console -consolelog -registryMultiLanguage'
export LOGARGS='-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.eclipse.equinox.http.jetty.log.stderr.threshold=info'

echo $WORKSPACE
./eclipse -vm $JAVA_HOME/bin/java $ARGS -vmargs $VMARGS -Dorg.osgi.service.http.port=$PORT -Dstage.workspace=$WORKSPACE $LOGARGS