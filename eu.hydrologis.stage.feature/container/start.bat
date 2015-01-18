:: STAGE start script for win based Systems

:: DATA TO BE CONFIGURED
:: START
set INSTALLDIR=%cd%
set WORKSPACE=/home/hydrologis/TMP/STAGEWORKSPACE/
set PORT=10000
set MAXHEAP=4g
:: END



set VMARGS="-Djava.awt.headless=true -Xverify:none -server -XX:+TieredCompilation -Xmx%MAXHEAP% -XX:NewRatio=4 -XX:+UseG1GC -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 -XX:SoftRefLRUPolicyMSPerMB=1000"
set ARGS="-console -consolelog -registryMultiLanguage"
set LOGARGS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.eclipse.equinox.http.jetty.log.stderr.threshold=info"

set EXEC=stage32.exe
set JRE="%INSTALLDIR%/jreWin32/"
IF EXIST "%PROGRAMFILES(X86)%" (
:: 64bit
   set EXEC=stage64.exe
   set JRE="%INSTALLDIR%/jreWin64/"
)

rem "INSTALLDIR = %INSTALLDIR%"
rem "WORKSPACE = %WORKSPACE%"
rem "JAVA = %JRE%/bin/java"
START /B %EXEC% -vm %JRE%/bin/java %ARGS% -vmargs %VMARGS% -Dorg.osgi.service.http.port=%PORT% -Dstage.workspace=%WORKSPACE% %LOGARGS%
