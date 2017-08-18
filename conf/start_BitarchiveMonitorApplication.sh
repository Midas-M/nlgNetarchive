echo Starting linux application: BitarchiveMonitorApplication
cd /home/test/QUICKSTART
PIDS=$(ps -wwfe | grep dk.netarkivet.archive.bitarchive.BitarchiveMonitorApplication | grep -v grep | grep /home/test/QUICKSTART/conf/settings_BitarchiveMonitorApplication.xml | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    echo Application already running.
else
    export CLASSPATH=/home/test/QUICKSTART/lib/netarchivesuite-monitor-core.jar:/home/test/QUICKSTART/lib/dk.netarkivet.harvester.jar:/home/test/QUICKSTART/lib/dk.netarkivet.archive.jar:/home/test/QUICKSTART/lib/dk.netarkivet.monitor.jar:/home/test/QUICKSTART/lib/dk.netarkivet.wayback.jar:/home/test/QUICKSTART/lib/netarchivesuite-archive-core.jar:$CLASSPATH;
    java -Xmx1536m  -Ddk.netarkivet.settings.file=/home/test/QUICKSTART/conf/settings_BitarchiveMonitorApplication.xml -Dlogback.configurationFile=/home/test/QUICKSTART/conf/logback_BitarchiveMonitorApplication.xml dk.netarkivet.archive.bitarchive.BitarchiveMonitorApplication < /dev/null > start_BitarchiveMonitorApplication.log 2>&1 &
fi
