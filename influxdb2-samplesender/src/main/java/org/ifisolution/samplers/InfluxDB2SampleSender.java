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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class InfluxDB2SampleSender extends AbstractSampleSender implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDB2SampleSender.class);

    /**
     * This constructor is invoked through reflection found in {@link SampleSenderFactory}
     */
    InfluxDB2SampleSender(RemoteSampleListener listener) {
        LOGGER.info("entering constructor with param: " + RemoteSampleListener.class.getName());
    }

    @Override
    public void testEnded(String host) {
        LOGGER.info("Test ended on : " + host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        LOGGER.info("Sample Event occurred on " + InfluxDB2SampleSender.class.getName());
    }

}
