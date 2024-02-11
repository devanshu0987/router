package org.codapayments.router.instanceListSupplier.impl;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.service.CircuitBreakerService;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StaticServiceInstanceWithCoolDownListSupplier implements ServiceInstanceListSupplier {
    private List<URI> uriList;
    private CircuitBreakerService circuitBreakerService;

    public StaticServiceInstanceWithCoolDownListSupplier(RoutingConfig config, CircuitBreakerService circuitBreakerService) {
        uriList = config.getInstances();
        this.circuitBreakerService = circuitBreakerService;
    }

    @Override
    public List<URI> get() {
        var filteredList = uriList.stream().filter(x -> circuitBreakerService.isCircuitClosed(x));
        return filteredList.toList();
    }

    @Override
    public void add(URI uri) {

    }
}
