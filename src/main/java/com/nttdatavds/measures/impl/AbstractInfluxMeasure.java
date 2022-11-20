package com.nttdatavds.measures.impl;

import com.nttdatavds.influxdb.InfluxClientProxy;

import java.util.Optional;

public abstract class AbstractInfluxMeasure {

    protected InfluxClientProxy influxClientProxy;

    protected String hostName;

    protected String testName;

    protected String runId;

    protected AbstractInfluxMeasure(InfluxClientProxy influxClientProxy) {
        this.influxClientProxy = influxClientProxy;
    }

    abstract static class AbstractInfluxMeasureBuilder<
            T extends AbstractInfluxMeasureBuilder<T, M>, M extends AbstractInfluxMeasure> {

        protected final M measure;

        protected AbstractInfluxMeasureBuilder(M measure) {
            this.measure = measure;
        }

        public T hostName(String hostName) {
            measure.hostName = hostName;
            return builder();
        }

        public T testName(String testName) {
            measure.testName = testName;
            return builder();
        }

        public T testRunId(String runId) {
            measure.runId = runId;
            return builder();
        }

        public M build() {
            return measure;
        }

        protected abstract T builder();

    }

}
