package org.codapayments.router.statistics;

public class StatisticsFactory {
    // Todo: Make the window size configurable.
    public static Statistics getStatisticInstance(String type) {
        switch (type) {
            case "COUNT":
                return new CountStatistics();
            case "AVERAGE":
                return new AverageStatistics();
            case "SLIDING_WINDOW_AVERAGE":
                return new SlidingWindowAverageStatistics(10 * 1000);
            case "SLIDING_WINDOW_COUNT":
                return new SlidingWindowSumStatistics(10 * 1000);
            default:
                throw new IllegalArgumentException();
        }

    }
}
