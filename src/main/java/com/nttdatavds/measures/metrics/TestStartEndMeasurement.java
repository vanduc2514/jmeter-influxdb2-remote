package com.github.vanduc2514.measures.metrics;

public interface TestStartEndMeasurement {

	/**
	 * Measurement name.
	 */
	String MEASUREMENT_NAME = "testStartEnd";

	interface Tags {
		/**
		 * Start or End type tag.
		 */
		String TYPE = "type";

		/**
		 * Node name field.
		 */
		String NODE_NAME = "nodeName";

                /** 
                 * tag use for a unique id for this particular execution (aka 'run') of a load test.
                 */  
                String RUN_ID = "runId";

                /** 
                 * Test name field.
                 */  
                String TEST_NAME = "testName";
	}
	
	interface Fields {
		/**
		 * Test name field.
		 */
		String PLACEHOLDER = "placeholder";
	}

	interface Values {
		/**
		 * Finished.
		 */

		String FINISHED = "finished";
		/**
		 * Started.
		 */
		String STARTED = "started";
	}
}
