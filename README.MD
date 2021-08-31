# Influx Jmeter Remote Test

Jmeter has a [sender mode](https://jmeter.apache.org/usermanual/remote-test.html#sendermode) which allows loading 
customized SampleSender. This plugin makes use of that feature and enables the Jmeter remote server (slave machine) 
to send the Test Result directly to Influx Database, which provides real-time test data. After sending to Influx, 
Test Result will be sent to the master machine using Batch Mode.

![jmeter_remote](https://github.com/vanduc2514/jmeter-influxdb2-remote/raw/master/image/jmeter_slave.png)

**Notice:** This plugin will overload the Influx Database if there are a lot of slave machines run in parallel.
Since each slave machine is a client connects to Influx and constantly push data to it

## Installation

Install the plugin to `${JMETER_HOME}/lib/ext` 

> This plugin has to be installed on both master and slave machine in order to work properly

## Configuration

> Make sure to enable [distributed testing](https://jmeter.apache.org/usermanual/jmeter_distributed_testing_step_by_step.html)
> before continuing 

```properties
# Influx properties
# The host name of Influx Database, whether it is a resolved name or an Ip address
# Example: localhost
influxdb.hostname=

# Influx properties
# The port of Influx Database
# Example: 8086
influxdb.port=

# Influx properties
# The token of bucket from Influx Database, raw string generated from Influx Database
influxdb.token=

# Influx properties
# The organization name of Influx Database
influxdb.organization=

# Influx properties
# The bucket name from Influx Database
influxdb.bucket=

# Influx properties
# Enable ssl (https) for connection to Influx Database
# Setting this to blank or ignore it will use the default http connection
influxdb.ssl.enable=

# Test Specific properties
# The test name that appears in Influx
test.name=

# Test Specific properties
# The run id that appears in Influx
test.runId=

# Remote properties
# Specific SampleSender class to let the remote machine directly send to Influx
# Don't change this value or the plugin will not work
mode=org.ifisolution.plugins.samplers.InfluxSampleSender
```

This plugin can be configured from the master machine or configured on each machine

### Configure from the master machine

Create a new `.properties` file with the above properties and execute the test plan from the master machine with
`-q path_to_created_property_file` and `-G path_to_created_property_file` option. For more information, refers
to this [Jmeter User Manual](https://jmeter.apache.org/usermanual/get-started.html#override).

For example:

```shell
jmeter -n -t Example-Apache-Jmeter-Plan.jmx -G influx.properties -q influx.properties -r

```

> The properties should be the same one, which applies the configuration for both slave and master machine

### Or Configure on each machine

Add the above properties to `user.properties` (found in `${JMETER_HOME}/bin`) for each slave machine
and for the master machine then restart Jmeter. After that, execute the test normally.

## Usage

This plugin comes with a `InfluxSampleSender` for sending Test Result directly to Influxdb from the slave machine 
and a BackendListenerClient for monitoring the Virtual users, Test start and Test end. To do so, select the 
`InfluxTestStateListener` to Backend Listener implementation from the drop-down list.

![jmeter](https://github.com/vanduc2514/jmeter-influxdb2-remote/raw/master/image/jmeter.png)

After that, execute the test then verify it in Influx.

### Development

..._To be update_

Thanks [@mderevyankoaqa](https://github.com/mderevyankoaqa) for developing the
[jmeter-influxdb2-listener-plugin](https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin),
which inspired me for developing this 😁.