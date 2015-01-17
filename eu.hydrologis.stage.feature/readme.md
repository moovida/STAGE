This feature allows to deploy of STAGE plugins and dependencies.

More info and docs:

  - http://eclipse.org/equinox/documents/quickstart-framework.php
  - http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html
  
## Container

  - the container subdir contains the OSGi launcher
  - extracted from Eclipse-IDE install

## Deploy

  - copy container subdir to deploy/install dir
  - export eu.hydrologis.stagerap.feature from Eclipse into the deploy dir
  - Option -> Use class files compiled on workspace = true
