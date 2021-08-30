package org.ifisolution.util;

import org.apache.jmeter.util.JMeterUtils;
import org.ifisolution.configuration.JmeterTestProperties;
import org.ifisolution.measures.impl.AbstractInfluxMeasure;

public class TestUtil {

    private TestUtil() {
    }

    public static void setTestMetadata(AbstractInfluxMeasure influxMeasure) {
        influxMeasure.setTestName(JMeterUtils.getProperty(JmeterTestProperties.TEST_NAME));
        influxMeasure.setRunId(JMeterUtils.getProperty(JmeterTestProperties.TEST_RUN_ID));
    }
}
