#!/bin/bash
cd /home/test/QUICKSTART/conf/
echo Starting external harvest database.
if [ -e ./start_external_harvest_database.sh ]; then
      ./start_external_harvest_database.sh &
      sleep 5
fi
echo Starting external admin database.
if [ -e ./start_external_admin_database.sh ]; then
      ./start_external_admin_database.sh &
      sleep 5
fi
echo Starting all applications on: 'localhost'
if [ -e ./start_GUIApplication.sh ]; then 
      ./start_GUIApplication.sh
fi
if [ -e ./start_ArcRepositoryApplication.sh ]; then 
      ./start_ArcRepositoryApplication.sh
fi
if [ -e ./start_BitarchiveMonitorApplication.sh ]; then 
      ./start_BitarchiveMonitorApplication.sh
fi
if [ -e ./start_BitarchiveApplication.sh ]; then 
      ./start_BitarchiveApplication.sh
fi
if [ -e ./start_HarvestControllerApplication_snapshot1.sh ]; then 
      ./start_HarvestControllerApplication_snapshot1.sh
fi
if [ -e ./start_HarvestControllerApplication_focused1.sh ]; then 
      ./start_HarvestControllerApplication_focused1.sh
fi
if [ -e ./start_IndexServerApplication.sh ]; then 
      ./start_IndexServerApplication.sh
fi
if [ -e ./start_ViewerProxyApplication.sh ]; then 
      ./start_ViewerProxyApplication.sh
fi
if [ -e ./start_HarvestJobManagerApplication.sh ]; then 
      ./start_HarvestJobManagerApplication.sh
fi
