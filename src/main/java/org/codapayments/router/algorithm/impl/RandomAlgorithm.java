package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class RandomAlgorithm implements RoutingAlgorithm {

    public RandomAlgorithm() {

    }

    @Override
    public URI route(ServiceInstanceListSupplier supplier) {
        // Todo: Is it possible that the underlying list changes in between calls, if we allow for updates?
        var instanceList = supplier.get();
        int size = instanceList.size();

        if(size == 0) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(size);

        return instanceList.get(index);
    }
}
