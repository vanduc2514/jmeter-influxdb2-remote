package org.ifisolution.util;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContextService;
import org.ifisolution.measures.impl.AbstractInfluxMeasure;
import org.ifisolution.plugins.TestProperties;

public class TestUtil {

    private TestUtil() {
    }

    public static void setTestMetaDataToMeasure(AbstractInfluxMeasure influxMeasure) {
        Sampler currentSampler = JMeterContextService.getContext().getCurrentSampler();
        if (currentSampler != null) {
            String testName = currentSampler.getPropertyAsString(TestProperties.TEST_NAME, "Test-Name");
            String runId = currentSampler.getPropertyAsString(TestProperties.TEST_RUN_ID, "IFI-0000");
            influxMeasure.setTestName(testName);
            influxMeasure.setRunId(runId);
        }
    }

}
