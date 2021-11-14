package org.ifisolution.measures.metrics;

public interface RequestMeasurement {

	String MEASUREMENT_NAME = "requestsRaw";

	interface Tags {
		/**
		 * Request name tag.
		 */
		String REQUEST_NAME = "requestName";

		/**
		 * Influx DB tag for a unique identifier for each execution(aka 'run') of a load test.
		 */
		String RUN_ID = "runId";

		/**
		 * Test name field.
		 */
		String TEST_NAME = "testName";

		/**
		 * Node name field.
		 */
		String NODE_NAME = "nodeName";

		/**
		 * Response code field.
		 */
		String RESULT_CODE = "responseCode";

		/**
		 * Error message.
		 */
		String ERROR_MSG = "errorMessage";

		/**
		 * Error response body.
		 */
		String ERROR_RESPONSE_BODY = "errorResponseBody";
	}

	interface Fields {
		/**
		 * Response time field.
		 */
		String RESPONSE_TIME = "responseTime";

		/**
		 * Error count field.
		 */
		String ERROR_COUNT = "errorCount";

		/**
		 * Error count field.
		 */
		String REQUEST_COUNT = "count";


		/**
		 * Sent Bytes field.
		 */
		String SENT_BYTES = "sentBytes";

		/**
		 * Received Bytes field.
		 */
		String RECEIVED_BYTES = "receivedBytes";

		/**
		 * Latency field.
		 */
		String LATENCY = "latency";

		/**
		 * Connect Time field.
		 */
		String CONNECT_TIME = "connectTime";

		/**
		 * Processing Time field.
		 */
		String PROCESSING_TIME = "processingTime";
	}
}
