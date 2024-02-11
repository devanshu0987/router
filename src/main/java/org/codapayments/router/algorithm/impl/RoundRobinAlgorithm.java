package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;

import java.net.URI;
import java.util.concurrent.locks.ReentrantLock;

public class RoundRobinAlgorithm implements RoutingAlgorithm {
    private int presentIndex;

    ReentrantLock lock = new ReentrantLock();

    public RoundRobinAlgorithm() {
        presentIndex = 0;
    }

    @Override
    public URI route(ServiceInstanceListSupplier supplier) {
        lock.lock();
        // Todo: Is it possible that the underlying list changes in between calls, if we allow for updates?
        var instanceList = supplier.get();
        int size = instanceList.size();

        if(size == 0) {
            lock.unlock();
            return null;
        }
        URI instanceToRouteTo = instanceList.get(presentIndex % size);
        presentIndex++;
        presentIndex = presentIndex % size;

        lock.unlock();

        return instanceToRouteTo;
    }
}
