package org.ifisolution.plugins.listeners;

import org.apache.jmeter.testelement.TestStateListener;
import org.ifisolution.measures.impl.InfluxTestResultMeasureImpl;

public class InfluxTestStateListener implements TestStateListener {

    private InfluxTestResultMeasureImpl influxManager;

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
