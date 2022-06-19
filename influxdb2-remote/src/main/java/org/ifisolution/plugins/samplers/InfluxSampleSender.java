/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ifisolution.plugins.samplers;

import org.apache.jmeter.samplers.*;
import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.TestResultMeasureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;

public class InfluxSampleSender extends BatchSampleSender {

    private static final long serialVersionUID = 3371144997364645511L;

    private transient TestResultMeasureManager measureManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxSampleSender.class.getSimpleName());

    /**
     * This constructor is invoked through reflection found in {@link SampleSenderFactory}
     */
    public InfluxSampleSender(RemoteSampleListener listener) {
        super(listener);
    }

    @Override
    public void testEnded(String host) {
        InfluxTestResultMeasure testResultMeasure = measureManager.getInfluxMeasure(new InfluxPropertiesProvider());
        if (testResultMeasure != null) {
            testResultMeasure.closeInfluxConnection();
        }
        super.testEnded(host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        // The Jmeter properties sent from master is propagated at this method.
        InfluxTestResultMeasure testResultMeasure = measureManager.getInfluxMeasure(new InfluxPropertiesProvider());
        if (testResultMeasure != null) {
            testResultMeasure.writeTestResult(e.getResult());
            if (testResultMeasure.measureSubResult()) {
                SampleResult[] subResults = e.getResult().getSubResults();
                for (SampleResult subResult : subResults) {
                    testResultMeasure.writeTestResult(subResult);
                }
            }
        } else {
            LOGGER.warn("No {} is configured. This remote machine does not send Test Result Point to Influx",
                    InfluxTestResultMeasure.class.getSimpleName());
        }
        super.sampleOccurred(e);
    }

    /**
     * Internal method called by RMI. This method acts as testStart()
     */
    private Object readResolve() throws ObjectStreamException {
        measureManager = TestResultMeasureManager.makeInstance();
        return this;
    }

}
