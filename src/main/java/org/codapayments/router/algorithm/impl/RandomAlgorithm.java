package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.config.RoutingConfig;

import java.net.URI;

public class RandomAlgorithm implements RoutingAlgorithm {

    public RandomAlgorithm(RoutingConfig config) {
        assert config.getInstances() != null && !config.getInstances().isEmpty();
    }
    @Override
    public URI route() {
        return null;
    }
}
