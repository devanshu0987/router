package org.codapayments.router.service;

import org.codapayments.router.TestConfig;
import org.codapayments.router.algorithm.RoutingAlgorithmType;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.statistics.MetricType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class CircuitBreakerServiceTests {
    @Test
    public void testErrorCountThresholdsForCircuitBreak() throws URISyntaxException {
        RoutingConfig config = TestConfig.getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        config.setErrorCountForCooldown(20);

        MetricService metricService = new MetricService(config);
        CircuitBreakerService circuitBreakerService = new CircuitBreakerService(config, metricService);
        URI instanceURI = new URI("http://localhost:8080");

        metricService.addMetric(MetricType.ERROR_COUNT, instanceURI, 500D);

        boolean status = circuitBreakerService.isCircuitClosed(instanceURI);

        assert !status;
    }

    @Test
    public void testLatencyThresholdsForCircuitBreak() throws URISyntaxException {
        RoutingConfig config = TestConfig.getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        config.setErrorCountForCooldown(20);
        config.setLatencyForCooldownInSeconds(5);

        MetricService metricService = new MetricService(config);
        CircuitBreakerService circuitBreakerService = new CircuitBreakerService(config, metricService);
        URI instanceURI = new URI("http://localhost:8080");

        metricService.addMetric(MetricType.ERROR_COUNT, instanceURI, 1D);
        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, (double) (10 * 1000));

        boolean status = circuitBreakerService.isCircuitClosed(instanceURI);

        assert !status;
    }

    @Test
    public void testClosedCircuit() throws URISyntaxException {
        RoutingConfig config = TestConfig.getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        config.setErrorCountForCooldown(20);
        config.setLatencyForCooldownInSeconds(5);

        MetricService metricService = new MetricService(config);
        CircuitBreakerService circuitBreakerService = new CircuitBreakerService(config, metricService);
        URI instanceURI = new URI("http://localhost:8080");

        metricService.addMetric(MetricType.ERROR_COUNT, instanceURI, 1D);
        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, (double) (1000));

        boolean status = circuitBreakerService.isCircuitClosed(instanceURI);

        assert status;
    }

    @Test
    public void testCooldown() throws URISyntaxException, InterruptedException {
        RoutingConfig config = TestConfig.getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        config.setErrorCountForCooldown(20);
        config.setLatencyForCooldownInSeconds(5);
        config.setCooldownTimeoutInSeconds(5);
        config.setMetricsWindowSizeInSeconds(5);

        MetricService metricService = new MetricService(config);
        CircuitBreakerService circuitBreakerService = new CircuitBreakerService(config, metricService);
        URI instanceURI = new URI("http://localhost:8080");

        metricService.addMetric(MetricType.ERROR_COUNT, instanceURI, 100D);
        metricService.addMetric(MetricType.LATENCY_AVERAGE, instanceURI, (double) (10 * 1000));

        // Initiate cooldown.
        boolean status = circuitBreakerService.isCircuitClosed(instanceURI);
        assert !status;

        status = circuitBreakerService.isCircuitClosed(instanceURI);
        assert !status;

        // wait for cooldown to finish
        // Sleep for the window size duration to wipe out all metrics.
        Thread.sleep((config.getCooldownTimeoutInSeconds() + 2) * 1000L);

        status = circuitBreakerService.isCircuitClosed(instanceURI);
        assert status;
    }
}
