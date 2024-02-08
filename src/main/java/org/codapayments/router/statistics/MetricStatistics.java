package org.codapayments.router.statistics;

import org.codapayments.router.config.RoutingConfig;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class MetricStatistics {
    public MetricStatistics() {
        data = new ConcurrentHashMap<>();
    }

    protected ConcurrentMap<URI, Statistics> data;

    public abstract void addData(URI uri, LocalDateTime timestamp, Double value);

    public Double getStatistic(URI uri) {
        if (data.containsKey(uri)) {
            return data.get(uri).getData();
        }
        return 0D;
    }
}
