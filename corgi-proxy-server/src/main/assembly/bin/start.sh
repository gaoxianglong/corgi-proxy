#!/bin/bash

cd $CORGI_HOME

#如果nohup.out文件存在则删除
NOHUP_PATH=nohup.out
if [ -f "$NOHUP_PATH" ];then
  echo "Deleting $NOHUP_PATH"
  rm -drf $NOHUP_PATH
fi

JAVA_OPS="-Dversion=0.0.1 -Dio.netty.leakDetectionLevel=SIMPLE -Xms2g -Xmx2g -Xmn768m -Xss256k -XX:+UseParallelOldGC -XX:ParallelGCThreads=3 -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:logs/gc.log -Djava.ext.dirs=libs -classpath conf com.github.registry.corgi.server.Main"

nohup java -server $JAVA_OPS &

sleep 2
tail -100f $NOHUP_PATH
