package org.codapayments.router.statistics;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.enums.StatisticType;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetricStatistics {
    private ConcurrentMap<URI, Statistics> data;
    private StatisticType metricType;

    public MetricStatistics(StatisticType type, RoutingConfig config) {
        data = new ConcurrentHashMap<>();
        metricType = type;
        for (var uri : config.getInstances()) {
            data.put(uri, StatisticsFactory.getInstance(type));
        }
    }

    public void addData(URI uri, Long timestamp, Double value) {
        data.compute(uri, (k, v) -> {
            if (v == null) {
                return StatisticsFactory.getInstance(metricType);
            } else {
                v.addData(new DataPoint(timestamp, value));
            }
            return v;
        });
    }

    public Double getStatistic(URI uri) {
        if (data.containsKey(uri)) {
            return data.get(uri).getData();
        }
        return 0D;
    }
}