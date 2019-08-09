#!/bin/bash

cd $CORGI_HOME

#如果nohup.out文件存在则删除
NOHUP_PATH=nohup.out
if [ -f "$NOHUP_PATH" ];then
  echo "Deleting $NOHUP_PATH"
  rm -drf $NOHUP_PATH
fi

if [ -z "$CORGI_HOME" ] ; then
  ## resolve links - $0 may be a link to maven's home
  PRG="$0"

  # need this for relative symlinks

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done

  saveddir=`pwd`

  CORGI_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  CORGI_HOME=`cd "$CORGI_HOME" && pwd`

  cd "$saveddir"
fi

export CORGI_HOME
cd $CORGI_HOME

JAVA_OPS="-Dversion=0.0.1 -Dio.netty.leakDetectionLevel=SIMPLE -Xms2g -Xmx2g -Xmn768m -Xss256k -XX:+UseParallelOldGC -XX:ParallelGCThreads=3 -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:logs/gc.log -Djava.ext.dirs=libs -classpath conf com.github.registry.corgi.server.Main"

nohup java -server $JAVA_OPS &

sleep 2
tail -100f $NOHUP_PATH
