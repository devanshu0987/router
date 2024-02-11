package org.codapayments.router.serviceInstanceListSupplier.impl;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.serviceInstanceListSupplier.ServiceInstanceListSupplier;

import java.net.URI;
import java.util.List;

public class StaticServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    private List<URI> uriList;

    public StaticServiceInstanceListSupplier(RoutingConfig config) {
        uriList = config.getInstances();
    }
    @Override
    public List<URI> get() {
        return uriList;
    }

    @Override
    public void add(URI uri) {

    }
}
