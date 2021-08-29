# InfluxDB2 Sample Sender

This plugin allows a jmeter remote server (slave machine) to send each SampleResult 
directly to InfluxDb2 Database

## Installation

### Prepare the artifact

Build the artifact from `influxdb2-samplesender` 

```bash
gradle fatJar
```

### Installation

**Note**: These steps are done in both master and slave machine

1. Copy the above artifact to `${JMETER_HOME}/lib/ext` 

2. Add these following properties to `user.properties` from `${JMETER_HOME}/bin`

Switch to Influx Sample Sender mode

```properties
mode=org.ifisolution.plugins.samplers.InfluxSampleSender
```

InfluxDb Connection Properties

```properties
influxdb.hostname=InfluxDb Host Name
influxdb.port=InfluxDb Port
influxdb.token=InfluxDd Access Token
influxdb.organization=InfluxDb Organization
influxdb.bucket=InfluxDb Bucket Name
```

3. Restart jmeter 

### Usage

This plugin comes with a `SampleSender` for sending Test Result directly to Influxdb 
from the slave machine and a `BackendListenerClient` for monitoring the Thread users,
Test start and end 

To monitor the Thread users counts, please add the `InfluxTestStateListener` to 
Backend Listener

![jmeter](https://github.com/vanduc2514/jmeter-influxdb2-remote/raw/master/image/jmeter.png)