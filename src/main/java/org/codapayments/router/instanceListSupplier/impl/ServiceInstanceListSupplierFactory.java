package org.codapayments.router.instanceListSupplier.impl;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;

public class ServiceInstanceListSupplierFactory {
    public static ServiceInstanceListSupplier getInstance(RoutingConfig config) {
        switch (config.getSupplierType()) {
            case STATIC -> {
                return new StaticServiceInstanceListSupplier(config);
            }
            case STATIC_WITH_COOLDOWN -> {
                return new StaticServiceInstanceWithCoolDownListSupplier(config);
            }
            default -> new IllegalArgumentException("Supplier type not implemented");
        }
        return null;
    }
}
