echo Starting linux application: ViewerProxyApplication
cd /home/test/QUICKSTART
PIDS=$(ps -wwfe | grep dk.netarkivet.viewerproxy.ViewerProxyApplication | grep -v grep | grep /home/test/QUICKSTART/conf/settings_ViewerProxyApplication.xml | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    echo Application already running.
else
    export CLASSPATH=/home/test/QUICKSTART/lib/netarchivesuite-monitor-core.jar:/home/test/QUICKSTART/lib/dk.netarkivet.harvester.jar:/home/test/QUICKSTART/lib/dk.netarkivet.archive.jar:/home/test/QUICKSTART/lib/dk.netarkivet.monitor.jar:/home/test/QUICKSTART/lib/dk.netarkivet.wayback.jar:/home/test/QUICKSTART/lib/netarchivesuite-harvest-scheduler.jar:/home/test/QUICKSTART/lib/netarchivesuite-archive-core.jar:$CLASSPATH;
    java -Xmx1536m  -Ddk.netarkivet.settings.file=/home/test/QUICKSTART/conf/settings_ViewerProxyApplication.xml -Dlogback.configurationFile=/home/test/QUICKSTART/conf/logback_ViewerProxyApplication.xml dk.netarkivet.viewerproxy.ViewerProxyApplication < /dev/null > start_ViewerProxyApplication.log 2>&1 &
fi
