package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.config.RoutingConfig;

public class RoutingAlgorithmFactory {
    public static RoutingAlgorithm getInstance(RoutingConfig config) {
        switch (config.getRoutingAlgorithm()) {
            case ROUND_ROBIN -> {
                return new RoundRobinAlgorithm();
            }
            case RANDOM -> {
                return new RandomAlgorithm();
            }
            default -> throw new IllegalArgumentException("Algorithm not implemented");
        }
    }
}
