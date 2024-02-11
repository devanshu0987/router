package org.codapayments.router.service;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.statistics.MetricType;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CircuitBreakerService {

    private final RoutingConfig routingConfig;
    private MetricService metricService;
    private static ConcurrentMap<URI, Long> coolDowns;

    public CircuitBreakerService(RoutingConfig routingConfig, MetricService metricService) {
        this.routingConfig = routingConfig;
        this.metricService = metricService;
        coolDowns = new ConcurrentHashMap<>();
    }

    // If circuit is closed, it means, request can pass.
    public boolean isCircuitClosed(URI redirectURI) {

        Long currentTimestamp = System.currentTimeMillis();

        if (metricService.getMetric(MetricType.ERROR_COUNT, redirectURI) > routingConfig.getErrorCountForCooldown()
                || metricService.getMetric(MetricType.LATENCY_AVERAGE, redirectURI) > 1000 * routingConfig.getLatencyForCooldownInSeconds()) {
            // take the instance out for timeout configured in the config.
            setCoolDown(redirectURI, currentTimestamp + (routingConfig.getCooldownTimeoutInSeconds() * 1000));
            return false;
        }

        var nextValidTimestamp = coolDowns.getOrDefault(redirectURI, currentTimestamp - 1000);
        if (currentTimestamp.compareTo(nextValidTimestamp) > 0)
            return true;
        return false;
    }

    private static void setCoolDown(URI uri, Long timestamp) {
        coolDowns.compute(uri, (k, v) -> {
            if (v == null)
                return System.currentTimeMillis();
            return timestamp;
        });
    }
}
