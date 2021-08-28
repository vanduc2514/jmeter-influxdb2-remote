package org.ifisolution.listeners;

import org.apache.jmeter.testelement.TestStateListener;
import org.ifisolution.measures.InfluxMeasureImpl;

public class InfluxTestStateListener implements TestStateListener {

    private InfluxMeasureImpl influxManager;

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String host) {

    }

    @Override
    public void testEnded() {

    }

    @Override
    public void testEnded(String host) {

    }
}
