package org.codapayments.router.statistics;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class SlidingWindowAverageStatistics implements Statistics {
    private SlidingWindowSumStatistics sum;
    private SlidingWindowSumStatistics count;

    SlidingWindowAverageStatistics(long windowSizeInMillis) {
        sum = new SlidingWindowSumStatistics(windowSizeInMillis);
        count = new SlidingWindowSumStatistics(windowSizeInMillis);
    }

    @Override
    public void addData(DataPoint dataPoint) {
        sum.addData(dataPoint);
        count.addData(new DataPoint(dataPoint.getTimestamp(), 1D));
    }

    @Override
    public Double getData() {
        Double accumulatedCount = count.getData();
        if (accumulatedCount > 0)
            return sum.getData() / accumulatedCount;
        return 0D;
    }
}
