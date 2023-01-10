#! /bin/sh
cd /data/mycode/trans_server/
git pull
mvn clean install
cd /root/service/server/trans_server/src
timestamp="`date +"%Y%m%d_%H%M%S"`"
mv ./TransServer-*-jar-with-dependencies.jar ./TransServer.jar_${timestamp}
cp /data/mycode/trans_server/target/TransServer-*-jar-with-dependencies.jar ./
cd /root/service/server/trans_server/
sh restart.sh
