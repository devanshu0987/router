package org.codapayments.router.statistics;

import org.codapayments.router.enums.StatisticType;

public class StatisticsFactory {
    // Todo: Make the window size configurable.
    public static Statistics getInstance(StatisticType type) {
        switch (type) {
            case COUNT:
                return new CountStatistics();
            case AVERAGE:
                return new AverageStatistics();
            case SLIDING_WINDOW_AVERAGE:
                return new SlidingWindowAverageStatistics(100 * 1000);
            case SLIDING_WINDOW_COUNT:
                return new SlidingWindowSumStatistics(100 * 1000);
            default:
                throw new IllegalArgumentException();
        }

    }
}
