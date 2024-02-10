package org.codapayments.router.statistics;

import java.time.LocalDateTime;

public class AverageStatistics implements Statistics {
    private Double sum = 0D;
    private Double count = 0D;

    @Override
    public void addData(DataPoint dataPoint) {
        sum += dataPoint.getValue();
        count += dataPoint.getValue();
    }

    @Override
    public Double getData() {
        if (count > 0)
            return sum / count;
        return 0D;
    }
}
