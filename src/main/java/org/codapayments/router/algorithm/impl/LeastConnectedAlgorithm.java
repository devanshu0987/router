package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.config.RoutingConfig;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class LeastConnectedAlgorithm implements RoutingAlgorithm {
    private List<URI> uriList;

    // A ConcurrentHashMap can be used as scalable frequency map by using LongAdder values.
    // This class is usually preferable to AtomicLong when multiple threads update a common sum
    // that is used for purposes such as collecting statistics, not for fine-grained synchronization control.
    // Under low update contention, the two classes have similar characteristics.
    // But under high contention, expected throughput of this class is significantly higher,
    // at the expense of higher space consumption
    private ConcurrentMap<URI, LongAdder> activeConnections;

    public LeastConnectedAlgorithm(RoutingConfig config) {
        uriList = config.getInstances();
        activeConnections = new ConcurrentHashMap<>();
        for (var uri : uriList) {
            activeConnections.computeIfAbsent(uri, k -> new LongAdder());
        }
    }

    // Linear in time.
    // TODO: To implement this in Constant time, we need sorted uris in decreasing order
    // TODO: Along with frequency updates that keep happening.
    // TODO: To satisfy this, We need to maintain a LeastFrequentlyUsed DataStructure.

    @Override
    public URI route() {
        URI chosenServer = null;
        long minConnections = Long.MAX_VALUE;
        for (URI uri : uriList) {
            // returns the latest state for the URI. It is possible that during that time, the counts increase
            // if the traffic is too high.
            LongAdder connections = activeConnections.get(uri);
            if (connections.longValue() < minConnections) {
                minConnections = connections.longValue();
                chosenServer = uri;
            }
        }
        return chosenServer;
    }

    @Override
    public void beginConnection(URI uri) {
        activeConnections.computeIfAbsent(uri, k -> new LongAdder()).increment();
    }

    @Override
    public void endConnection(URI uri) {
        activeConnections.computeIfAbsent(uri, k -> new LongAdder()).decrement();
    }
}
