package org.codapayments.router.algorithm;

import java.net.URI;

public interface RoutingAlgorithm {
    public URI route();

    public void beginConnection(URI uri);

    public void endConnection(URI uri);
}
