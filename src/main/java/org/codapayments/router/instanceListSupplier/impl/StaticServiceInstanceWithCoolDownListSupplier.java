package org.codapayments.router.instanceListSupplier.impl;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StaticServiceInstanceWithCoolDownListSupplier implements ServiceInstanceListSupplier {
    private List<URI> uriList;
    private ConcurrentMap<URI, Long> cooldowns;

    public StaticServiceInstanceWithCoolDownListSupplier(RoutingConfig config) {
        uriList = config.getInstances();
        cooldowns = new ConcurrentHashMap<>();
    }

    @Override
    public List<URI> get() {
        Long currentTimestamp = System.currentTimeMillis();
        var filteredList = uriList.stream().filter(x -> {
            var nextValidTimestamp = cooldowns.getOrDefault(x, currentTimestamp - 1000);
            if (currentTimestamp.compareTo(nextValidTimestamp) > 0)
                return true;

            return false;
        });

        return filteredList.toList();
    }

    @Override
    public void add(URI uri) {

    }

    @Override
    public void setCooldown(URI uri, Long timestamp) {
        cooldowns.compute(uri, (k, v) -> {
            if (v == null)
                return System.currentTimeMillis();
            return timestamp;
        });

    }
}
