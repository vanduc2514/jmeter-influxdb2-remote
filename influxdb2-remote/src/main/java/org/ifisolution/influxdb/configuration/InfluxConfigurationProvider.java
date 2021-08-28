package org.ifisolution.influxdb.configuration;

public interface InfluxConfigurationProvider {

    String provideToken();

    String provideOrganizationName();

    String provideBucketName();

    String provideConnectionUrl();

}
