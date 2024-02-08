package org.codapayments.router.statistics;

import java.time.LocalDateTime;

public class AverageStatistics implements Statistics {
    private Double sum = 0D;
    private Double count = 0D;
    @Override
    public void addData(LocalDateTime timestamp, Double value) {
        sum += value;
        count++;
    }

    @Override
    public Double getData() {
        return sum / count;
    }
}
