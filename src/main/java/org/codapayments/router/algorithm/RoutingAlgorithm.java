package org.codapayments.router.algorithm;

import org.codapayments.router.serviceInstanceListSupplier.ServiceInstanceListSupplier;

import java.net.URI;

public interface RoutingAlgorithm {
    public URI chooseServiceInstance(ServiceInstanceListSupplier supplier);
}
