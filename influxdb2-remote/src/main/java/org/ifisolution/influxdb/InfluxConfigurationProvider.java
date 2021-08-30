package org.ifisolution.influxdb;

public interface InfluxConfigurationProvider {

    String provideToken();

    String provideOrganizationName();

    String provideBucketName();

    String provideConnectionUrl();

}
