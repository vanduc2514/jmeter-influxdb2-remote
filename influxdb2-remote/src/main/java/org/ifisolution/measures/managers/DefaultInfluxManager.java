package org.ifisolution.measures.managers;

import org.ifisolution.configuration.JmeterPropertiesProvider;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.md.jmeter.influxdb2.visualizer.result.SampleResultPointProvider;

public class DefaultInfluxManager {

    private static DefaultInfluxManager manager;

    private final InfluxClient influxClient;

    private DefaultInfluxManager(InfluxClient influxClient) {
        this.influxClient = influxClient;
    }

    /**
     * Singleton Default Manager
     *
     * @return the {@link DefaultInfluxManager}
     */
    public static DefaultInfluxManager getInstance() {
        if (manager == null) {
            JmeterPropertiesProvider jmeterPropertiesProvider = new JmeterPropertiesProvider();
            InfluxClient influxClient = InfluxClient.buildClient(
                    new InfluxClientConfiguration(jmeterPropertiesProvider)
            );
            manager = new DefaultInfluxManager(influxClient);
        }
        return manager;
    }

    public void writeTestResult(SampleResultPointProvider resultPointProvider) {
        this.influxClient.writeValues(resultPointProvider.getPoint());
    }

}
