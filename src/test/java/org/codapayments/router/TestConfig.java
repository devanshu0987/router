package org.codapayments.router;

import org.codapayments.router.algorithm.RoutingAlgorithmType;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.serviceInstanceListSupplier.ServiceInstanceListSupplierType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class TestConfig {
    public static RoutingConfig getRoutingConfig(RoutingAlgorithmType type) {
        RoutingConfig config = new RoutingConfig();
        config.setRoutingAlgorithm(type);
        config.setSupplierType(ServiceInstanceListSupplierType.STATIC);
        config.setCooldownTimeoutInSeconds(5);
        try {
            config.setInstances(List.of(
                    new URI("http://localhost:8081"),
                    new URI("http://localhost:8082"),
                    new URI("http://localhost:8083"),
                    new URI("http://localhost:8084"))
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return config;
    }
}
