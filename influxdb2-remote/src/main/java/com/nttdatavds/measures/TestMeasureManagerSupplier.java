package com.nttdatavds.measures;

import com.nttdatavds.influxdb.InfluxClient;
import com.nttdatavds.measures.impl.TestResultMeasureImpl;
import com.nttdatavds.measures.impl.TestStateMeasureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class TestMeasureManagerSupplier {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMeasureManager.class.getSimpleName());

    private static TestMeasureManager INSTANCE;

    private String hostName;
    private String testName;
    private String runId;
    private boolean saveErrorResponse;
    private boolean measureSubResult;
    private int userMetricPoolSize;
    private int userMetricIntervalMilli;
    private InfluxClient influxClient;

    TestMeasureManagerSupplier() {
    }

    public TestMeasureManagerSupplier hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public TestMeasureManagerSupplier testName(String testName) {
        this.testName = testName;
        return this;
    }

    public TestMeasureManagerSupplier runId(String testRunId) {
        this.runId = testRunId;
        return this;
    }

    public TestMeasureManagerSupplier saveErrorResponse(boolean saveErrorResponse) {
        this.saveErrorResponse = saveErrorResponse;
        return this;
    }

    public TestMeasureManagerSupplier measureSubResult(boolean measureSubResult) {
        this.measureSubResult = measureSubResult;
        return this;
    }

    public TestMeasureManagerSupplier userMetricPoolSize(int poolSize) {
        this.userMetricPoolSize = poolSize;
        return this;
    }

    public TestMeasureManagerSupplier userMetricIntervalMilli(int milli) {
        this.userMetricPoolSize = milli;
        return this;
    }

    public TestMeasureManagerSupplier influxClient(InfluxClient influxClient) {
        this.influxClient = influxClient;
        return this;
    }

    public synchronized TestMeasureManager getManagerInstance() {
        if (INSTANCE == null) {
            TestResultMeasure testResult = new TestResultMeasureImpl(
                    hostName, testName, runId, saveErrorResponse, measureSubResult, influxClient);
            TestStateMeasure testState = new TestStateMeasureImpl(
                    hostName, testName, runId, influxClient
            );
            INSTANCE = new TestMeasureManager(influxClient, testResult, testState);
        }
        if (INSTANCE.getScheduler() == null || INSTANCE.getScheduler().isTerminated()) {
            //TODO: Extract pool size to parameter
            INSTANCE.setScheduler(Executors.newScheduledThreadPool(userMetricPoolSize));
        }
        return INSTANCE;
    }
}
