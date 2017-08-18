echo Starting linux application: HarvestControllerApplication_focused1
cd /home/test/QUICKSTART
PIDS=$(ps -wwfe | grep dk.netarkivet.harvester.heritrix3.HarvestControllerApplication | grep -v grep | grep /home/test/QUICKSTART/conf/settings_HarvestControllerApplication_focused1.xml | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    echo Application already running.
else
    export CLASSPATH=/home/test/QUICKSTART/lib/netarchivesuite-monitor-core.jar:/home/test/QUICKSTART/lib/dk.netarkivet.harvester.jar:/home/test/QUICKSTART/lib/dk.netarkivet.archive.jar:/home/test/QUICKSTART/lib/dk.netarkivet.monitor.jar:/home/test/QUICKSTART/lib/dk.netarkivet.wayback.jar:/home/test/QUICKSTART/lib/netarchivesuite-heritrix3-controller.jar:$CLASSPATH;
    java -Xmx1536m  -Ddk.netarkivet.settings.file=/home/test/QUICKSTART/conf/settings_HarvestControllerApplication_focused1.xml -Dlogback.configurationFile=/home/test/QUICKSTART/conf/logback_HarvestControllerApplication_focused1.xml dk.netarkivet.harvester.heritrix3.HarvestControllerApplication < /dev/null > start_HarvestControllerApplication_focused1.log 2>&1 &
fi
