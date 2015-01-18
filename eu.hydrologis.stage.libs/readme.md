Libs folders explained
======================

libs-geotools
---------------

Containes the geotools distribution necessary both by the plugins and the modules of STAGE.
This has to be aligned between STAGE and JGrasstools.

spatialtoolbox
------------------

Contains basically all jars that can supply modules to the spatial toolbox.
STAGE will look into this folder to load modules jars. These jar are **NOT** added to the classpath of the STAGE plugin itself and are not supposed to be necessary to the STAGE application internals.

libs
----------

Contains those parts of JGrasstools that are used internally by STAGE (ex. Geopaparazzi libs).
These will **NOT** be browsed by the STAGE modules loader.

