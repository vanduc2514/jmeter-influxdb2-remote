//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ifisolution.configuration;

public class MeasureSettings {

    private final String hostName;
    private final String testName;
    private final String testRunId;
    private final boolean saveErrorResponse;
    private final boolean measureSubResult;

    MeasureSettings(String hostName, String testName, String testRunId, boolean saveErrorResponse, boolean measureSubResult) {
        this.hostName = hostName;
        this.testName = testName;
        this.testRunId = testRunId;
        this.saveErrorResponse = saveErrorResponse;
        this.measureSubResult = measureSubResult;
    }

    public static MeasureSettingsBuilder builder() {
        return new MeasureSettingsBuilder();
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getTestName() {
        return this.testName;
    }

    public String getTestRunId() {
        return this.testRunId;
    }

    public boolean isSaveErrorResponse() {
        return this.saveErrorResponse;
    }

    public boolean isMeasureSubResult() {
        return this.measureSubResult;
    }

    public static class MeasureSettingsBuilder {
        private String hostName;
        private String testName;
        private String testRunId;
        private boolean saveErrorResponse;
        private boolean measureSubResult;

        MeasureSettingsBuilder() {
        }

        public MeasureSettingsBuilder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public MeasureSettingsBuilder testName(String testName) {
            this.testName = testName;
            return this;
        }

        public MeasureSettingsBuilder testRunId(String testRunId) {
            this.testRunId = testRunId;
            return this;
        }

        public MeasureSettingsBuilder saveErrorResponse(boolean saveErrorResponse) {
            this.saveErrorResponse = saveErrorResponse;
            return this;
        }

        public MeasureSettingsBuilder measureSubResult(boolean measureSubResult) {
            this.measureSubResult = measureSubResult;
            return this;
        }

        public MeasureSettings build() {
            return new MeasureSettings(this.hostName, this.testName, this.testRunId, this.saveErrorResponse, this.measureSubResult);
        }

        public String toString() {
            return "MeasureSettings.MeasureSettingsBuilder(hostName=" + this.hostName + ", testName=" + this.testName + ", testRunId=" + this.testRunId + ", saveErrorResponse=" + this.saveErrorResponse + ", measureSubResult=" + this.measureSubResult + ")";
        }
    }
}
