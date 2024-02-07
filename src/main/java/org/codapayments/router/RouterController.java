package org.codapayments.router;

import jakarta.annotation.PostConstruct;
import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RouterController {

    @Autowired
    private RoutingConfig routingConfig;

    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);
    private RoutingAlgorithm router;

    @PostConstruct
    public void initialize() {
        logger.info(routingConfig.toString());
        router = RoutingAlgorithmFactory.getAlgorithm(routingConfig);

    }

    // We should get any request and then try to pass it onto the downstream.
    // We accept any path.
    // We accept any media type
    // TODO: How to accept any Object here. No bar on what you send?
    @PostMapping(value = "/**")
    public String index(@RequestBody String message) {
        URI redirectURI = router.route();

        return redirectURI.toString();
    }
}
