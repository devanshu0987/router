package org.codapayments.router.statistics;

import org.codapayments.router.config.RoutingConfig;

import java.net.URI;
import java.time.LocalDateTime;

public class ErrorCountMetricStatistics extends MetricStatistics {

    public ErrorCountMetricStatistics(RoutingConfig config) {
        super();
        for (var uri : config.getInstances()) {
            data.put(uri, new CountStatistics());
        }
    }

    @Override
    public void addData(URI uri, LocalDateTime timestamp, Double value) {
        data.compute(uri, (k, v) -> {
            if (v == null) {
                return new CountStatistics();
            } else {
                v.addData(timestamp, value);
            }
            return v;
        });
    }
}
