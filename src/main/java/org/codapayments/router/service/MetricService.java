package org.codapayments.router.service;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.statistics.MetricStatistics;
import org.codapayments.router.statistics.MetricType;
import org.codapayments.router.statistics.StatisticType;

import java.net.URI;
import java.util.Map;

public class MetricService {
    private MetricStatistics latencyMetric;
    private MetricStatistics successCountMetric;
    private MetricStatistics errorCountMetric;

    public MetricService(RoutingConfig routingConfig) {
        latencyMetric = new MetricStatistics(StatisticType.SLIDING_WINDOW_AVERAGE, routingConfig);
        errorCountMetric = new MetricStatistics(StatisticType.SLIDING_WINDOW_COUNT, routingConfig);
        successCountMetric = new MetricStatistics(StatisticType.SLIDING_WINDOW_COUNT, routingConfig);
    }

    public void addMetric(MetricType type, URI uri, Double value) {
        switch (type) {
            case SUCCESS_COUNT -> {
                successCountMetric.addData(uri, System.currentTimeMillis(), value);
            }
            case ERROR_COUNT -> {
                errorCountMetric.addData(uri, System.currentTimeMillis(), value);
            }
            case LATENCY_AVERAGE -> {
                latencyMetric.addData(uri, System.currentTimeMillis(), value);
            }
        }
    }

    public Double getMetric(MetricType type, URI uri) {
        switch (type) {
            case SUCCESS_COUNT -> {
                return successCountMetric.getStatistic(uri);
            }
            case ERROR_COUNT -> {
                return errorCountMetric.getStatistic(uri);
            }
            case LATENCY_AVERAGE -> {
                return latencyMetric.getStatistic(uri);
            }
        }
        return 0D;
    }


}
