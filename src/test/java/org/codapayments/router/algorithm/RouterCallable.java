package org.codapayments.router.algorithm;

import org.codapayments.router.serviceInstanceListSupplier.ServiceInstanceListSupplier;

import java.net.URI;
import java.util.concurrent.Callable;

public class RouterCallable implements Callable<URI> {
    RoutingAlgorithm router;
    ServiceInstanceListSupplier supplier;
    public RouterCallable(RoutingAlgorithm router, ServiceInstanceListSupplier supplier) {

        this.router = router;
        this.supplier = supplier;
    }

    @Override
    public URI call() throws Exception {
        return router.chooseServiceInstance(supplier);
    }
}
