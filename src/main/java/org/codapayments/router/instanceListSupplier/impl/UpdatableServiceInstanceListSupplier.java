package org.codapayments.router.instanceListSupplier.impl;

import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.instanceListSupplier.Add;

import java.net.URI;
import java.util.List;

public class UpdatableServiceInstanceListSupplier implements ServiceInstanceListSupplier {
    @Override
    public void add(URI uri) {

    }

    @Override
    public void setCooldown(URI uri, Long timestamp) {

    }

    @Override
    public List<URI> get() {
        return null;
    }
}
