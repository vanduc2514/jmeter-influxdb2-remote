package org.ifisolution.measures.impl;

public interface MeasureConfigurationProvider {

    String provideTestName();

    String provideRunId();

    String provideHostName();

    boolean provideSaveErrorResponseOption();

}
