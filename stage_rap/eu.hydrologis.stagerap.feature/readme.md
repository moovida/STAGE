This feature allows to deploy of STAGE plugins and dependencies.

More info and docs:

  - http://eclipse.org/equinox/documents/quickstart-framework.php
  
## Container

  - the container subdir contains the OSGi launcher
  - extracted from Eclipse-IDE install
  - currently: Linux-32bit and Win-32bit 

## Deploy

  - copy container subdir to deploy/install dir
  - export eu.hydrologis.stagerap.feature from Eclipse into the deploy dir
  - Option -> Use class files compiled on workspace = true
