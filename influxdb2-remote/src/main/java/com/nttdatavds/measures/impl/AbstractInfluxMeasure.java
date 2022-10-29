package com.nttdatavds.measures.impl;

import com.nttdatavds.influxdb.InfluxClient;

import java.util.Optional;

public abstract class AbstractInfluxMeasure {

    protected InfluxClient influxClient;

    protected String hostName;

    protected String testName;

    protected String runId;

    protected AbstractInfluxMeasure(InfluxClient influxClient) {
        this.influxClient = influxClient;
    }

    abstract static class AbstractInfluxMeasureBuilder<
            T extends AbstractInfluxMeasureBuilder<T, M>, M extends AbstractInfluxMeasure> {

        protected final M measure;

        protected AbstractInfluxMeasureBuilder(M measure) {
            this.measure = measure;
        }

        public T hostName(String hostName) {
            measure.hostName = safelyGet("host-name", hostName);
            return builder();
        }

        public T testName(String testName) {
            measure.testName = safelyGet("test-name", testName);
            return builder();
        }

        public T testRunId(String runId) {
            measure.runId = safelyGet("run-id", runId);
            return builder();
        }

        public M build() {
            return measure;
        }

        protected abstract T builder();

        protected String safelyGet(String propertyName, String value) {
            return Optional.ofNullable(value).orElse("dummy-" + propertyName);
        }

    }

}
