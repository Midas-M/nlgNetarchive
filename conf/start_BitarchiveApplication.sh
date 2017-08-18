echo Starting linux application: BitarchiveApplication
cd /home/test/QUICKSTART
PIDS=$(ps -wwfe | grep dk.netarkivet.archive.bitarchive.BitarchiveApplication | grep -v grep | grep /home/test/QUICKSTART/conf/settings_BitarchiveApplication.xml | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    echo Application already running.
else
    export CLASSPATH=/home/test/QUICKSTART/lib/netarchivesuite-monitor-core.jar:/home/test/QUICKSTART/lib/dk.netarkivet.harvester.jar:/home/test/QUICKSTART/lib/dk.netarkivet.archive.jar:/home/test/QUICKSTART/lib/dk.netarkivet.monitor.jar:/home/test/QUICKSTART/lib/dk.netarkivet.wayback.jar:/home/test/QUICKSTART/lib/netarchivesuite-archive-core.jar:$CLASSPATH;
    java -Xmx1536m  -Ddk.netarkivet.settings.file=/home/test/QUICKSTART/conf/settings_BitarchiveApplication.xml -Dlogback.configurationFile=/home/test/QUICKSTART/conf/logback_BitarchiveApplication.xml -Djava.security.manager -Djava.security.policy=/home/test/QUICKSTART/conf/security.policy dk.netarkivet.archive.bitarchive.BitarchiveApplication < /dev/null > start_BitarchiveApplication.log 2>&1 &
fi
