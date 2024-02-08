package org.codapayments.router.algorithm.impl;

import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.enums.RoutingAlgorithmType;

public class RoutingAlgorithmFactory {
    public static RoutingAlgorithm getAlgorithm(RoutingConfig config) {
        switch (config.getRoutingAlgorithm()) {
            case ROUND_ROBIN -> {
                return new RoundRobinAlgorithm(config);
            }
            case RANDOM -> {
                return new RandomAlgorithm(config);
            }
            case ROUND_ROBIN_COOLDOWN -> {
                return new RoundRobinWithCoolDownAlgorithm(config);
            }
            default -> throw new IllegalArgumentException("Algorithm not implemented");
        }
    }
}
