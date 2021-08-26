# InfluxDB2 Sample Sender

This is a customized jmeter SampleSender which allows a jmeter remote server (slave machine)
to send each SampleResult directly to InfluxDb2 Database without transfer it back to
the jmeter master in order to reduce the network workload

## Installation

### Prepare the artifact

Build the artifact from `influxdb2-samplesender` 

```bash
gradle fatJar
```

### Install in Jmeter

**Note**: These steps are done in both master and slave machine

1. Copy the above artifact to `${JMETER_HOME}/lib/ext` 

2. Add the following properties to `user.properties` from `${JMETER_HOME}/bin`

```properties
mode=org.ifisolution.samplers.InfluxDB2SampleSender
```

or override the properties with `Jmode=org.ifisolution.samplers.InfluxDB2SampleSender` from the CLI

3. Restart jmeter 