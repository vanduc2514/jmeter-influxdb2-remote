package org.ifisolution.plugins.samplers;

import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.TestResultMeasureManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InfluxSampleSenderTest {

    /**
     * The test subject
     */
    @Spy
    @InjectMocks
    private InfluxSampleSender sampleSender = new InfluxSampleSender(mock(RemoteSampleListener.class));

    @Mock
    private TestResultMeasureManager measureManager;

    @Mock
    private InfluxTestResultMeasure testResultMeasure;

    @Mock
    private SampleEvent mockSampleEvent;

    @Mock
    private SampleResult mockSampleResult;

    @BeforeEach
    public void setupBehaviorMockSampleEvent() {
        when(mockSampleEvent.getResult()).thenReturn(mockSampleResult);
        when(measureManager.getInfluxMeasure()).thenReturn(testResultMeasure);
    }

    @Test
    void shouldWriteTestResult_whenSampleOccurred() {
        sampleSender.sampleOccurred(mockSampleEvent);

        verify(testResultMeasure).writeTestResult(mockSampleEvent.getResult());
    }

    @Test
    void shouldWriteSubResult_whenEnableMeasureSubResult() {
        var mockSubResult = mock(SampleResult.class);
        var mockSubResultArray = Arrays
                .asList(mockSubResult, mockSubResult)
                .toArray(new SampleResult[2]);
        when(testResultMeasure.measureSubResult()).thenReturn(true);
        when(mockSampleResult.getSubResults()).thenReturn(mockSubResultArray);

        sampleSender.sampleOccurred(mockSampleEvent);

        verify(testResultMeasure, times(2)).writeTestResult(mockSubResult);
    }

    @Test
    void shouldCallSuper_afterWriteToInflux() {
        sampleSender.sampleOccurred(mockSampleEvent);

        verify(testResultMeasure).writeTestResult(mockSampleResult);
        verify(sampleSender).sampleOccurred(mockSampleEvent);
    }

    @Test
    void shouldCloseInflux_whenTestEnd() {
        sampleSender.testEnded("fake_host");
        verify(testResultMeasure).closeInfluxConnection();
    }

    @Test
    void shouldCallSuper_afterCloseCloseInfluxConnection() {
        var fake_host = "fake_host";
        sampleSender.testEnded(fake_host);

        verify(sampleSender).testEnded(fake_host);
        verify(testResultMeasure).closeInfluxConnection();
    }

}