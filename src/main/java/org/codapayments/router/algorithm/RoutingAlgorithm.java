package org.codapayments.router.algorithm;

import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;

import java.net.URI;
import java.time.LocalDateTime;

public interface RoutingAlgorithm {
    public URI route(ServiceInstanceListSupplier supplier);
}
