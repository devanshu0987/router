package org.codapayments.router.statistics;

import java.time.LocalDateTime;

public interface Statistics {
    public void addData(LocalDateTime timestamp, Double value);
    public Double getData();
}
