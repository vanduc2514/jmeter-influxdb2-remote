## Jmeter InfluxDB2 Remote Development Tools

### Docker

This project tries to reproduce the distributed testing mode 
in Jmeter with remote machines using Docker. 

To run the containers, simply run this command in `./docker`

```shell
docker-compose up
```

To verify the up and running containers, open a new terminal session
and run this command

```shell
docker ps -a
```

These containers contain of one Influx Database, one Jmeter master
container and one Jmeter slave container.

### Binary Installation

Make a new binary by executing this command in `../influxdb2-remote`

```shell
mvn clean install
```

**Note**: This command requires Maven to be installed first. See https://maven.apache.org/install.html

Run this command to copy the binary file to the running containers

```shell
install.sh
```

### Influx Database

Influx Database is a real-time database to track the test result, also the
user metric from Jmeter.

Connect to the database admin console via Chrome by accessing this URL http://localhost:8086

Create new organization and bucket in InfluxDB

see https://docs.influxdata.com/influxdb/v2.2/organizations/create-org/

see https://docs.influxdata.com/influxdb/v2.2/organizations/buckets/create-bucket/

The connection configuration can be set in `./test-plan/example.properties` as test plan specific. 
Or can be set as global in the `user.properties` file in each Jmeter containers. 
See https://jmeter.apache.org/usermanual/properties_reference.html#introduction

### Test Plan

Copy the test plan to `./test-plan`

Open a new shell in the `jmeter-master` Docker container by executing this command

```shell
docker exec -ti jmeter-master bash
```

From the current folder, execute the test plan via the command line. 
See https://jmeter.apache.org/usermanual/get-started.html#running