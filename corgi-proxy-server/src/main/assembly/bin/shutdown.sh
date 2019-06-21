#!/bin/bash

cd $CORGI_HOME

source /etc/profile

#获取本机所有节点的PID
PID=`ps -ef | grep "com.github.registry.corgi.server.Main" | grep -v "grep" | awk '{print $2}'`
if [ "$PID" ];then
  echo "PID:"$PID
  #kill本机的所有corgi节点
  kill -15 $PID
  echo "corgi all stopped"
else
  echo "No corgi nodes"
fi
