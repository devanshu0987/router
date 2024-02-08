package org.codapayments.router.statistics;

public class StatisticsFactory {
    public static Statistics getStatisticInstance(String type) {
        switch (type) {
            case "COUNT":
                return new CountStatistics();
            case "AVERAGE":
                return new AverageStatistics();
            default:
                throw new IllegalArgumentException();
        }

    }
}
