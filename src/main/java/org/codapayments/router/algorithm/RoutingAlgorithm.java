package org.codapayments.router.algorithm;

import java.net.URI;
import java.time.LocalDateTime;

public interface RoutingAlgorithm {
    public URI route();

    // Sets cool-down for a URI. In cool-down time, the URI will not be selected.
    // ToDO : If all URI are in cool-down then choose the decision.
    public void setCooldown(URI uri, LocalDateTime time);
}
