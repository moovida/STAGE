Program args
-------------
-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl} -console -consolelog


VM Args
--------
-Declipse.ignoreApp=true 
-Dosgi.noShutdown=true 
-Dorg.eclipse.equinox.http.jetty.log.stderr.threshold=info 
-Dorg.eclipse.rap.workbenchAutostart=false 
-Dosgi.parentClassloader=ext 
-Dstage.workspace="your path"
-Dstage.javaexec="java exec path"