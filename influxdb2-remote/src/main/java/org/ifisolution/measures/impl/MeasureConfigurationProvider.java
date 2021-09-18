package org.ifisolution.measures.impl;

public interface MeasureConfigurationProvider {

    /**
     * Return the test name property
     *
     * @return the test name
     */
    String provideTestName();

    /**
     * Return the run id property
     *
     * @return the run id
     */
    String provideRunId();

    /**
     * Return the name of the machine currently running test
     *
     * @return the machine name
     */
    String provideHostName();

    /**
     * Return the save error response property
     *
     * @return false if the property is empty or not given
     */
    boolean provideSaveErrorResponseOption();

    /**
     * Return the run with standalone property option
     *
     * @return false if property is empty or not given
     */
    boolean isStandalone();

    /**
     * Return the measure sub result property
     *
     * @return true if this property is empty or not given
     */
    boolean measureSubResult();

}
