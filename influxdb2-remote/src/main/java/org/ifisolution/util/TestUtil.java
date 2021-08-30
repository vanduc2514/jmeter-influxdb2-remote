package org.ifisolution.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.ifisolution.configuration.JmeterTestProperties;
import org.ifisolution.measures.impl.AbstractInfluxMeasure;

public class TestUtil {

    private TestUtil() {
    }

    public static void setTestMetadata(AbstractInfluxMeasure influxMeasure) {
        String testName = JMeterUtils.getProperty(JmeterTestProperties.TEST_NAME);
        if (testName != null && !StringUtils.isEmpty(testName)) {
            influxMeasure.setTestName(testName);
        }
        String runId = JMeterUtils.getProperty(JmeterTestProperties.TEST_RUN_ID);
        if (runId != null && !StringUtils.isEmpty(runId)) {
            influxMeasure.setRunId(runId);
        }
    }
}
