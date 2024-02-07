package org.codapayments.router.algorithm;

import java.net.URI;
import java.util.concurrent.Callable;

public class RouterCallable implements Callable<URI> {
    RoutingAlgorithm router;
    public RouterCallable(RoutingAlgorithm router) {
        this.router = router;
    }

    @Override
    public URI call() throws Exception {
        return router.route();
    }
}
