package org.codapayments.router;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.statistics.ErrorCountMetricStatistics;
import org.codapayments.router.statistics.LatencyMetricStatistics;
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
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class RouterController {

    @Autowired
    private RoutingConfig routingConfig;
    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);
    private RoutingAlgorithm router;
    private LatencyMetricStatistics latency;
    private ErrorCountMetricStatistics errorCount;

    @PostConstruct
    public void initialize() {
        logger.info(routingConfig.toString());
        router = RoutingAlgorithmFactory.getAlgorithm(routingConfig);
        latency = new LatencyMetricStatistics(routingConfig);
        errorCount = new ErrorCountMetricStatistics(routingConfig);
    }

    // We should get any request and then try to pass it onto the downstream.
    // We accept any path.
    // We accept any media type
    // TODO: How to accept any Object here. No bar on what you send?
    @PostMapping(value = "/**")
    public ResponseEntity<?> index(HttpServletRequest req, @RequestBody String message) {
        URI redirectURI = router.route();

        // check if high error count.
        // Todo: Implement Sliding window statistic for this.
        if (errorCount.getStatistic(redirectURI) > 5 || latency.getStatistic(redirectURI) > 1000) {
            // take the instance out for timeout configured in the config.
            setCoolDown(redirectURI);
        }

        var part = req.getRequestURI();
        HttpHeaders headers = new HttpHeaders();
        // to indicate which server actually responded to us.
        headers.add("Location", redirectURI.toString());

        // call application server
        try {
            AtomicLong beforeTime = new AtomicLong(System.currentTimeMillis());
            return RestClient.create().post()
                    .uri(redirectURI.toString() + part)
                    .contentType(MediaType.parseMediaType(req.getContentType()))
                    .body(message)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            latency.addData(redirectURI, LocalDateTime.now(), System.currentTimeMillis() - beforeTime.doubleValue());
                            return new ResponseEntity<>(response.bodyTo(ObjectNode.class), headers, HttpStatus.OK);
                        } else {
                            latency.addData(redirectURI, LocalDateTime.now(), System.currentTimeMillis() - beforeTime.doubleValue());
                            return new ResponseEntity<>(null, headers, response.getStatusCode());
                        }
                    });
        } catch (Exception ex) {
            errorCount.addData(redirectURI, LocalDateTime.now(), 1D);
        }

        return new ResponseEntity<>(null, headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void setCoolDown(URI uri) {
        router.setCooldown(
                uri, LocalDateTime.now().plus(routingConfig.getTimeoutInSeconds(), ChronoUnit.SECONDS));
    }
}
