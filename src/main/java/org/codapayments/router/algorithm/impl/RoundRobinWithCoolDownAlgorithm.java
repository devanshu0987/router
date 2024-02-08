package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.config.RoutingConfig;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class RoundRobinWithCoolDownAlgorithm implements RoutingAlgorithm {
    private List<URI> uriList;

    private ConcurrentMap<URI, LocalDateTime> cooldowns;
    private int presentIndex;

    ReentrantLock lock = new ReentrantLock();

    public RoundRobinWithCoolDownAlgorithm(RoutingConfig config) {
        uriList = config.getInstances();
        cooldowns = new ConcurrentHashMap<>();
        for (var uri : uriList) {
            cooldowns.computeIfAbsent(uri, k -> LocalDateTime.now().minus(5, ChronoUnit.MINUTES));
        }
        presentIndex = 0;
    }

    @Override
    public URI route() {
        lock.lock();
        URI instanceToRouteTo = null;
        while (true) {
            instanceToRouteTo = uriList.get(presentIndex % uriList.size());
            presentIndex++;
            presentIndex = presentIndex % uriList.size();
            // check cool-downs.
            var nextValidTimestamp = cooldowns.get(instanceToRouteTo);
            if (LocalDateTime.now().compareTo(nextValidTimestamp) > 0)
                break;
        }
        lock.unlock();
        return instanceToRouteTo;
    }

    @Override
    public void setCooldown(URI uri, LocalDateTime nextValidTimestamp) {
        cooldowns.compute(uri, (k,v) -> {
            if(v == null)
                return LocalDateTime.now();
            return nextValidTimestamp;
        });
    }
}
