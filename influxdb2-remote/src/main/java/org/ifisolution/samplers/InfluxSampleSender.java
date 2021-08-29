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

package org.ifisolution.samplers;

import org.apache.jmeter.samplers.*;
import org.ifisolution.measures.impl.InfluxTestResultMeasureImpl;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class InfluxSampleSender extends BatchSampleSender implements Serializable, Runnable {

    private static final long serialVersionUID = 3371144997364645511L;

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxSampleSender.class);

    private InfluxTestResultMeasure defaultResultMeasure;

    /**
     * This constructor is invoked through reflection found in {@link SampleSenderFactory}
     */
    public InfluxSampleSender(RemoteSampleListener listener) {
        super(listener);
    }

    @Override
    public void testEnded(String host) {
        if (defaultResultMeasure != null) {
            defaultResultMeasure.close();
        }
        LOGGER.info("Test ended on : " + host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        defaultResultMeasure.writeTestResult(e);
        super.sampleOccurred(e);
//        JMeterContextService.ThreadCounts tc = JMeterContextService.getThreadCounts();
//        System.out.println("Started Thread: " + tc.startedThreads);
//        System.out.println(tc.finishedThreads);
//        LOGGER.info("Sample Event occurred on " + InfluxDB2SampleSender.class.getName());
//
//        JMeterUtils.getPropDefault("influxdb_hostname", null);
    }

    @Override
    public void run() {
        System.out.println("Running Job");
    }

    private Object readResolve() throws ObjectStreamException {
        if (isClientConfigured()) {
            defaultResultMeasure = InfluxTestResultMeasureImpl.getInstance();
        } else {
            defaultResultMeasure = InfluxTestResultMeasureImpl.getInstance();
        }
        return this;
    }
}
