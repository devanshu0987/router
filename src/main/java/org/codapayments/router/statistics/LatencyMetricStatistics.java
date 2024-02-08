package org.codapayments.router.statistics;

import org.codapayments.router.config.RoutingConfig;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class LatencyMetricStatistics extends MetricStatistics {

    public LatencyMetricStatistics(RoutingConfig config) {
        super();
        for (var uri : config.getInstances()) {
            data.put(uri, new AverageStatistics());
        }
    }

    @Override
    public void addData(URI uri, LocalDateTime timestamp, Double value) {
        data.compute(uri, (k, v) -> {
            if (v == null) {
                return new AverageStatistics();
            } else {
                v.addData(timestamp, value);
            }
            return v;
        });
    }
}
