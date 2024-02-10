package org.codapayments.router.statistics;

public class DataPoint {
    private Long timestamp;
    private Double value;

    public DataPoint(Long timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
