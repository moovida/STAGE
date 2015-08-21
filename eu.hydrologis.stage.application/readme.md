Program args
===============================

> -os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl} -console -consolelog


VM Args
===============================

> -Declipse.ignoreApp=true 
> -Dosgi.noShutdown=true 
> -Dorg.eclipse.equinox.http.jetty.log.stderr.threshold=info 
> -Dorg.eclipse.rap.workbenchAutostart=false 
> -Dosgi.parentClassloader=ext 

STAGE VM ARGS
===============================

> -Dstage.workspace="your path" **Path to a custom workspace**
> -Dstage.javaexec="java exec path"  **Path to the java executable to use**
> -Dstage.islocal=true **If set to true, some features for local use are enabled**

Override workspace structure
--------------------------------

By setting the following paths, the app overrides the user based structure.
This can be useful in local mode.

> -Dstage.geopaparazzifolder="geopaparazzi projects folder"  **Path to a custom geopaparazzi folder**
> -Dstage.datafolder="data folder" **Path to a custom data folder**
> -Dstage.scriptsfolder="scripts folder" **Path to a custom scripts folder**