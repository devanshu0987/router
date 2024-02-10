package org.codapayments.router.statistics;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.enums.StatisticType;

public class StatisticsFactory {
    // Todo: Make the window size configurable.
    public static Statistics getInstance(StatisticType type, RoutingConfig config) {
        switch (type) {
            case COUNT:
                return new CountStatistics();
            case AVERAGE:
                return new AverageStatistics();
            case SLIDING_WINDOW_AVERAGE:
                return new SlidingWindowAverageStatistics(config.getMetricsWindowSizeInSeconds() * 1000);
            case SLIDING_WINDOW_COUNT:
                return new SlidingWindowSumStatistics(config.getMetricsWindowSizeInSeconds() * 1000);
            default:
                throw new IllegalArgumentException();
        }

    }
}
