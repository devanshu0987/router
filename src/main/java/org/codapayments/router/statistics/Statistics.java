package org.codapayments.router.statistics;

import java.time.LocalDateTime;

public interface Statistics {
    public void addData(DataPoint dataPoint);
    public Double getData();
}
