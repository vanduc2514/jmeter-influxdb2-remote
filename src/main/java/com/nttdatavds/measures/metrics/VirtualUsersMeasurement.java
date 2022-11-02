package com.nttdatavds.measures.metrics;

public interface VirtualUsersMeasurement {

	/**
	 * Measurement name.
	 */
	String MEASUREMENT_NAME = "virtualUsers";

	interface Tags {
		/**
		 * Node name field
		 */
		String NODE_NAME = "nodeName";

		String TEST_NAME = "testName";

		String RUN_ID = "runId";
	}

	interface Fields {
		/**
		 * Minimum active threads field.
		 */
		String MIN_ACTIVE_THREADS = "minActiveThreads";

		/**
		 * Maximum active threads field.
		 */
		String MAX_ACTIVE_THREADS = "maxActiveThreads";

		/**
		 * Mean active threads field.
		 */
		String MEAN_ACTIVE_THREADS = "meanActiveThreads";

		/**
		 * Started threads field.
		 */
		String STARTED_THREADS = "startedThreads";

		/**
		 * Finished threads field.
		 */
		String FINISHED_THREADS = "finishedThreads";
	}
}
