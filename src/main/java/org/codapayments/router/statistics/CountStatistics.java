package org.codapayments.router.statistics;

import java.time.LocalDateTime;

public class CountStatistics implements Statistics {
    private Double count = 0D;
    @Override
    public void addData(LocalDateTime timestamp, Double value) {
        count += 1;
    }

    @Override
    public Double getData() {
        return count;
    }
}
