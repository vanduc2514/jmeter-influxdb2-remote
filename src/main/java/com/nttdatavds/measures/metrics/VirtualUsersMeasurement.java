package com.nttdatavds.measures.metrics;

public interface VirtualUsersMeasurement {

	String MEASUREMENT_NAME = "virtualUsers";

	interface Tags {

		String NODE_NAME = "nodeName";

		String TEST_NAME = "testName";

		String RUN_ID = "runId";
	}

	interface Fields {

		String ACTIVE_THREADS = "activeThreads";

		String STARTED_THREADS = "startedThreads";

		String FINISHED_THREADS = "finishedThreads";
	}
}
