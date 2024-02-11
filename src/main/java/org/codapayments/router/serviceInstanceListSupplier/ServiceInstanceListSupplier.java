package org.codapayments.router.serviceInstanceListSupplier;

import java.net.URI;
import java.util.List;


// Different ways to get the List of instances.

public interface ServiceInstanceListSupplier {
    public List<URI> get();
    public void add(URI uri);
}
