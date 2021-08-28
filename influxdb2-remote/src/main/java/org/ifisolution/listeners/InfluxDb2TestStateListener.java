package org.ifisolution.listeners;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.md.jmeter.influxdb2.visualizer.InfluxDatabaseBackendListenerClient;

import java.util.List;

public class InfluxDb2TestStateListener extends InfluxDatabaseBackendListenerClient {

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        //NOOP
    }

    @Override
    public void setupTest(BackendListenerContext context) {

    }
}
