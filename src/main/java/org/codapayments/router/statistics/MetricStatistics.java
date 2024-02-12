package org.codapayments.router.statistics;

import org.codapayments.router.config.RoutingConfig;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetricStatistics {
    private ConcurrentMap<URI, Statistics> data;
    private StatisticType metricType;
    private RoutingConfig config;

    public MetricStatistics(StatisticType type, RoutingConfig config) {
        data = new ConcurrentHashMap<>();
        metricType = type;
        this.config = config;
        for (var uri : config.getInstances()) {
            data.put(uri, StatisticsFactory.getInstance(metricType, config));
        }
    }

    public void addData(URI uri, Long timestamp, Double value) {
        data.compute(uri, (k, v) -> {
            if (v == null) {
                Statistics instance = StatisticsFactory.getInstance(metricType, config);
                instance.addData(new DataPoint(timestamp, value));
                return instance;
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
