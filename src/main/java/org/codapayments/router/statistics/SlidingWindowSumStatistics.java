package org.codapayments.router.statistics;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class SlidingWindowSumStatistics implements Statistics {
    private ConcurrentLinkedDeque<DataPoint> window;
    private long windowSizeInMillis;
    private AtomicReference<Double> sum;


    public SlidingWindowSumStatistics(long windowSizeInMillis) {
        this.windowSizeInMillis = windowSizeInMillis;
        this.window = new ConcurrentLinkedDeque<>();
        this.sum = new AtomicReference<>(0D);
    }

    @Override
    public void addData(DataPoint dataPoint) {
        window.add(dataPoint);
        sum.updateAndGet(prevCount -> prevCount + dataPoint.getValue()); // atomically increment count
        evictExpiredElements();
    }

    @Override
    public Double getData() {
        evictExpiredElements();
        return sum.get();
    }

    // Todo : Make it thread safe. Do I need to take a lock here?
    private void evictExpiredElements() {
        while (!window.isEmpty() && window.peekFirst().getTimestamp() < System.currentTimeMillis() - windowSizeInMillis) {
            var item = window.pollFirst();
            sum.updateAndGet(prevCount -> prevCount - item.getValue()); // atomically decrement count
        }
    }
}
