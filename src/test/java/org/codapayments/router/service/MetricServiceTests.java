package org.codapayments.router.service;

import org.codapayments.router.TestConfig;
import org.codapayments.router.algorithm.RoutingAlgorithmType;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.statistics.MetricType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class MetricServiceTests {
    @Test
    public void testAddMetric() throws URISyntaxException {
        RoutingConfig config = TestConfig.getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        MetricService metricService = new MetricService(config);
        URI instanceURI = new URI("http://localhost:8080");

        // ACT
        metricService.addMetric(MetricType.SUCCESS_COUNT, instanceURI, 1D);
        metricService.addMetric(MetricType.SUCCESS_COUNT, instanceURI, 1D);
        metricService.addMetric(MetricType.SUCCESS_COUNT, instanceURI, 1D);

        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, 5D);
        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, 10D);
        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, 15D);

        // ASSERT
        assert metricService.getMetric(MetricType.SUCCESS_COUNT, instanceURI) == 3D;
        assert metricService.getMetric(MetricType.LATENCY_AVERAGE, instanceURI) == 10D;
    }

    @Test
    public void testSlidingWindowMetrics() throws URISyntaxException, InterruptedException {
        RoutingConfig config = TestConfig.getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        config.setMetricsWindowSizeInSeconds(5);
        MetricService metricService = new MetricService(config);
        URI instanceURI = new URI("http://localhost:8080");

        // ACT
        metricService.addMetric(MetricType.SUCCESS_COUNT, instanceURI, 1D);
        metricService.addMetric(MetricType.SUCCESS_COUNT, instanceURI, 1D);
        metricService.addMetric(MetricType.SUCCESS_COUNT, instanceURI, 1D);

        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, 5D);
        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, 10D);
        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, 15D);

        // ASSERT
        assert metricService.getMetric(MetricType.SUCCESS_COUNT, instanceURI) == 3D;
        assert metricService.getMetric(MetricType.LATENCY_AVERAGE, instanceURI) == 10D;

        // Sleep for the window size duration to wipe out all metrics.
        Thread.sleep((config.getMetricsWindowSizeInSeconds() + 2) * 1000L);

        // ASSERT
        assert metricService.getMetric(MetricType.SUCCESS_COUNT, instanceURI) == 0D;
        assert metricService.getMetric(MetricType.LATENCY_AVERAGE, instanceURI) == 0D;


    }
}
