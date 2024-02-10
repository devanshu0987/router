package org.codapayments.router.instanceListSupplier;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;


// Different ways to get the List of instances.

public interface ServiceInstanceListSupplier {
    public List<URI> get();
    public void add(URI uri);
    public void setCooldown(URI uri, Long timestamp);
}
