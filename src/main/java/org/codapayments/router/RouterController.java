package org.codapayments.router;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

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
    public ResponseEntity<?> index(HttpServletRequest req, @RequestBody String message) {
        URI redirectURI = router.route();

        var part = req.getRequestURI();

        // call application server
        return RestClient.create().post()
                .uri(redirectURI.toString() + part)
                .contentType(MediaType.parseMediaType(req.getContentType()))
                .body(message)
                .exchange((request, response) -> {
                    HttpHeaders headers = new HttpHeaders();
                    // to indicate which server actually responded to us.
                    headers.add("Location", redirectURI.toString());

                    if (response.getStatusCode().is2xxSuccessful()) {
                        return new ResponseEntity<>(response.bodyTo(ObjectNode.class), headers, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(null, headers, response.getStatusCode());
                    }
                });
    }
}
