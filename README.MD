# Influx Jmeter Remote Test

Jmeter has a [sender mode](https://jmeter.apache.org/usermanual/remote-test.html#sendermode) which allows loading 
customized SampleSender. This plugin makes use of that feature and enables the Jmeter remote server (slave machine) 
to send the Test Result directly to Influx Database, in order to provide real-time test data. After sending to Influx, 
Test Result will be sent back to the master machine using Batch Mode.

![jmeter_remote](https://github.com/vanduc2514/jmeter-influxdb2-remote/raw/master/image/jmeter_slave.png)

## Installation

Install the plugin to `${JMETER_HOME}/lib/ext` on master and each slave machine

## Configuration

> Make sure to enable [distributed testing](https://jmeter.apache.org/usermanual/jmeter_distributed_testing_step_by_step.html)
> before continue

```properties
# Remote properties
# Specific the SampleSender class to let remote machine directly send to Influx Database
# Ignore this property if the test is not running in distributed mode
mode=org.ifisolution.plugins.samplers.InfluxSampleSender

# Plugin properties
# Setting this to true to let the master send test result to Influx Database
master_send_result=

# Test Specific properties
# Set the option to save error response to true/false
# By default, this plugin never save error response to reduce workload
save_error_response=

# Test Specific properties
# Set the option to save sub result from sample result 
# By default, this property is set to true
save_measure_sub_result=

# Test Specific properties
# The test name that appears in Influx
test.name=

# Test Specific properties
# The run id that appears in Influx
test.runId=

# Influx properties
# The host name of Influx database, whether it is a resolved name or an Ip address
# Example: localhost
influxdb.hostname=

# Influx properties
# The port of Influx database
# Example: 8086
influxdb.port=

# Influx properties
# Raw bucket token string generated from Influx
influxdb.token=

# Influx properties
# The organization name of Influx database
influxdb.organization=

# Influx properties
# The bucket name from Influx database
influxdb.bucket=

# Influx properties
# Enable ssl (https) for connection to Influx
# Setting this to blank or ignore it will use the default http connection
influxdb.ssl.enable=

```

This plugin can be configured from the master machine or configured on master and each slave machine

### Configure from the master machine

Create a new `.properties` file with the above properties and execute the test plan from the master machine with
`-q path_to_created_property_file` and `-G path_to_created_property_file` option. For more information, refers
to [Override Jmeter Properties](https://jmeter.apache.org/usermanual/get-started.html#override).

For example:

```shell
jmeter -n -t Example-Apache-Jmeter-Plan.jmx -G influx.properties -q influx.properties -r

```

> Properties file should be the same. This file will apply the configuration for both master and slave machine(s)

### Or Configure on each machine

Add the above properties to `user.properties` (found in `${JMETER_HOME}/bin`) for master and each slave machine
then restart Jmeter.

## Usage

This plugin comes with a `InfluxSampleSender` for sending Test Result directly to Influxdb from the slave machine 
and a BackendListenerClient for monitoring the Virtual users, Test-start and Test-end from the master machine. 
To do so, select the `InfluxTestStateListener` for Backend Listener implementation from the drop-down list.

![jmeter](https://github.com/vanduc2514/jmeter-influxdb2-remote/raw/master/image/jmeter.png)

After that, execute the test then verify it in Influx.

### Development

This plugin is an extension of the existing Batch mode found in Jmeter Remote testing. It is developed based on Custom 
implementation of [Sender mode](https://jmeter.apache.org/usermanual/remote-test.html#sendermode).

![InfluxTestStateListener](./image/InfluxTestStateListener_setupTest._sequence.png)

At test start, master Jmeter machine invokes ``setupTest()`` from ``InfluxTestStateListener`` to write **Start State** 
Point to Influx using ``InfluxTestStateMeasure``. Then it creates a ``ScheduledExecutorService`` to gradually write the 
**User Metric** Point to Influx. After that, the test plan is sent to remote Jmeter machines for execution.

![InfluxSampleSender](./image/InfluxSampleSender_sampleOccurred_sequence.png)

When remote Jmeter machine receives a sample from the test case, it invokes ``sampleOcurred()`` from the custom implementation
``InfluxSampleSender``. Then``InfluxTestResultMeasure`` writes **Test Result** Point to Influx. If ``measure sub result`` 
is enabled, this sender will populate the sub result from the sample, and also writes the result to Influx.

After that this ``InfluxSampleSender`` invokes ``sampleOcurred()`` in the super ``BatchModeSender`` to send sample back
to master Jmeter machine. If ``master_send_result`` is enabled, then the master will send a write request to Influx 
using the same ``InfluxTestResultMeasure`` above.

![InfluxTestStateListener](./image/InfluxTestStateListener_teardownTest_sequence.png)

At test end, master Jmeter machine invokes ``teardownTest()`` from ``InfluxTestStateListener`` and writes **Finish State**
Point to Influx.

See:
[InfluxDB Java client 2](https://github.com/influxdata/influxdb-client-java)

Thanks [@mderevyankoaqa](https://github.com/mderevyankoaqa), the author of
[jmeter-influxdb2-listener-plugin](https://github.com/mderevyankoaqa/jmeter-influxdb2-listener-plugin),
you inspired me developing this.

Thanks [@Vanco](https://github.com/Vanco) for the cool [Sequence Diagram Plugin](https://github.com/Vanco/SequencePlugin)