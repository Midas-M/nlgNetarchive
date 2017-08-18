echo Starting linux application: HarvestJobManagerApplication
cd /home/test/QUICKSTART
PIDS=$(ps -wwfe | grep dk.netarkivet.harvester.scheduler.HarvestJobManagerApplication | grep -v grep | grep /home/test/QUICKSTART/conf/settings_HarvestJobManagerApplication.xml | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    echo Application already running.
else
    export CLASSPATH=/home/test/QUICKSTART/lib/netarchivesuite-monitor-core.jar:/home/test/QUICKSTART/lib/dk.netarkivet.harvester.jar:/home/test/QUICKSTART/lib/dk.netarkivet.archive.jar:/home/test/QUICKSTART/lib/dk.netarkivet.monitor.jar:/home/test/QUICKSTART/lib/dk.netarkivet.wayback.jar:/home/test/QUICKSTART/lib/netarchivesuite-harvest-scheduler.jar:$CLASSPATH;
    java -Xmx1536m  -Ddk.netarkivet.settings.file=/home/test/QUICKSTART/conf/settings_HarvestJobManagerApplication.xml -Dlogback.configurationFile=/home/test/QUICKSTART/conf/logback_HarvestJobManagerApplication.xml dk.netarkivet.harvester.scheduler.HarvestJobManagerApplication < /dev/null > start_HarvestJobManagerApplication.log 2>&1 &
fi
