package org.codapayments.router.serviceInstanceListSupplier.impl;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.serviceInstanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.service.CircuitBreakerService;

public class ServiceInstanceListSupplierFactory {
    public static ServiceInstanceListSupplier getInstance(RoutingConfig config, CircuitBreakerService circuitBreakerService) {
        switch (config.getSupplierType()) {
            case STATIC -> {
                return new StaticServiceInstanceListSupplier(config);
            }
            case STATIC_WITH_COOLDOWN -> {
                return new StaticServiceInstanceWithCoolDownListSupplier(config, circuitBreakerService);
            }
            default -> new IllegalArgumentException("Supplier type not implemented");
        }
        return null;
    }
}
