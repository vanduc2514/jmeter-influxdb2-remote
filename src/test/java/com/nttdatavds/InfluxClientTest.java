//package com.github.vanduc2514;
//
//import com.influxdb.client.InfluxDBClient;
//import com.influxdb.client.InfluxDBClientFactory;
//import com.influxdb.client.domain.WritePrecision;
//import com.influxdb.client.write.Point;
//import com.github.vanduc2514.influxdb.InfluxClientException;
//import com.github.vanduc2514.influxdb.InfluxClientProxy;
//import com.github.vanduc2514.measures.MeasureHelper;
//import com.github.vanduc2514.measures.Measures;
//import com.github.vanduc2514.measures.TestResultMeasure;
//import com.github.vanduc2514.measures.TestStateMeasure;
//import com.github.vanduc2514.measures.impl.TestResultMeasureImpl;
//import com.github.vanduc2514.measures.impl.TestStateMeasureImpl;
//import com.github.vanduc2514.measures.metrics.TestStartEndMeasurement;
//import org.apache.jmeter.samplers.SampleResult;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//
//import java.util.Timer;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class InfluxClientTest {
//
//    /**
//     * Test subject
//     */
//    @InjectMocks
//    private InfluxClientProxy influxClientProxy;
//
//    @BeforeEach
//    public void setup() throws InfluxClientException {
//        influxClientProxy = InfluxClientProxy.builder()
//                .connectionUrl("http://localhost:8086")
//                .organization("ifisolution")
//                .bucket("jmeter")
//                .token("kdmvyTxSSnHTsX6xEFwH_5RnQJW_y1d68ximPKiQOzDvyFwAY0_fABkhJOTIfQ4cDm6zLptUwZYM165-DkgQoQ==")
//                .writeBatchSize(1000)
//                .writeFlushInterval(1000)
//                .writeBufferLimit(10000)
//                .build();
//    }
//
//    @Test
//    public void shouldConnectToInflux() throws InfluxClientException, InterruptedException {
//        AtomicInteger count = new AtomicInteger(1);
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
//        TestResultMeasure resultMeasure = Measures.testResultMeasureBuilder(influxClientProxy)
//                .hostName("")
//                .testName("unit-test")
//                .testRunId("unit-test")
//                .measureSubResult(false)
//                .saveErrorResponse(false)
//                .build();
//        executorService.scheduleAtFixedRate(() -> {
//            int current = count.getAndIncrement();
//            SampleResult sampleResult = new SampleResult();
//            sampleResult.setSampleLabel("unit-test" + count);
//            sampleResult.setLatency(0);
//            sampleResult.setConnectTime(0);
//            resultMeasure.writeTestResult(sampleResult);
//            System.out.println("Write Point: " + current);
//        }, 1, 1, TimeUnit.MILLISECONDS);
//        while (count.get() != 10) {
//        }
//        executorService.shutdown();
//        if (executorService.awaitTermination(30, TimeUnit.SECONDS)) {
//            System.out.println("Scheduler terminated!");
//            influxClientProxy.closeClient();
//            System.out.println("Influx closed!");
//        }
//    }
//
//    @Test
//    public void shouldBeTheSame() {
//        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(
//            "http:localhost:8080",
//            new char[]{'a', 'b'},
//                "organization",
//                "bucketName"
//        );
//        InfluxDBClient clone = InfluxDBClientFactory.create(
//                "http:localhost:8080",
//                new char[]{'a', 'b'},
//                "organization",
//                "bucketName"
//        );
//        assertTrue(influxDBClient.equals(clone));
//    }
//}
