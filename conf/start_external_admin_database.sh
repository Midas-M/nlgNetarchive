#!/bin/bash

java -Xmx1536m  -cp /home/midas/IdeaProjects/netarchivesuite/QUICKSTART/lib/db/derbynet.jar:/home/midas/IdeaProjects/netarchivesuite/QUICKSTART/lib/db/derby.jar org.apache.derby.drda.NetworkServerControl -p 48120 start < /dev/null > start_external_admin_database.log 2>&1 &
