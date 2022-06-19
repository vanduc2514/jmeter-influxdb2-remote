export JMETER_LIB=/jmeter/apache-jmeter-5.4.1/lib

docker cp ../../influxdb2-remote/target/jmeter-influxdb2-remote-*.jar jmeter-master:$JMETER_LIB
docker cp ../../influxdb2-remote/target/jmeter-influxdb2-remote-*.jar jmeter-slave:$JMETER_LIB