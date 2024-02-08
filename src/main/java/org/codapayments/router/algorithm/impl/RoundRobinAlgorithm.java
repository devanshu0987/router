package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.config.RoutingConfig;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class RoundRobinAlgorithm implements RoutingAlgorithm {
    private List<URI> uriList;
    private int presentIndex;

    ReentrantLock lock = new ReentrantLock();

    public RoundRobinAlgorithm(RoutingConfig config) {
        uriList = config.getInstances();
        presentIndex = 0;
    }

    @Override
    public URI route() {
        lock.lock();

        URI instanceToRouteTo = uriList.get(presentIndex % uriList.size());
        presentIndex++;
        presentIndex = presentIndex % uriList.size();

        lock.unlock();

        return instanceToRouteTo;
    }

    @Override
    public void setCooldown(URI uri, LocalDateTime time) {
        // No op.
    }
}
