#! /bin/sh

if [ -f pid ];then
 cat pid | xargs kill -9
 echo 'app has been killed.'
fi

nohup java -cp ./logserver-es5.5.jar:/libs/*:./conf/* com.inforefiner.cloud.log.LogServer --server.port=8878 --server.servlet.contextPath=/api/logs &

echo $! > pid

if [ -f nohup.out ];then
 tail -f nohup.out
fi