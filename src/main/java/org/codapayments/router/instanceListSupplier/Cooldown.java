package org.codapayments.router.instanceListSupplier;

import java.net.URI;

public interface Cooldown {
    public void setCooldown(URI uri, Long timestamp);
}
