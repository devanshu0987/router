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
        int index = ThreadLocalRandom.current().nextInt(supplier.get().size());

        return supplier.get().get(index);
    }
}
