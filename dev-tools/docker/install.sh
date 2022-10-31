export JMETER_LIB=/jmeter/apache-jmeter-5.4.1/lib
export JMETER_SLAVE=jmeter-slave
export JMETER_MASTER=jmeter-master

docker stop $JMETER_SLAVE
docker stop $JMETER_MASTER
docker cp ../../influxdb2-remote/target/jmeter-influxdb2-remote-*.jar jmeter-master:$JMETER_LIB
docker cp ../../influxdb2-remote/target/jmeter-influxdb2-remote-*.jar jmeter-slave:$JMETER_LIB
docker start $JMETER_SLAVE
docker start $JMETER_MASTER