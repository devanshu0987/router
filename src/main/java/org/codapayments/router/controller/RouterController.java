package org.codapayments.router.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.service.CircuitBreakerService;
import org.codapayments.router.service.MetricService;
import org.codapayments.router.service.RoutingService;
import org.codapayments.router.statistics.MetricType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class RouterController {
    @Autowired
    private RoutingConfig routingConfig;
    @Autowired
    private RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);
    private RoutingService routingService;
    private MetricService metricService;
    private CircuitBreakerService circuitBreakerService;

    @PostConstruct
    public void initialize() {
        this.metricService = new MetricService(routingConfig);
        this.circuitBreakerService = new CircuitBreakerService(routingConfig, metricService);
        this.routingService = new RoutingService(routingConfig, metricService, circuitBreakerService);
    }

    // We should get any request and then try to pass it onto the downstream.
    @PostMapping(value = "/**", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ObjectNode> index(RequestEntity<ObjectNode> req) {

        var response = routingService.route(req, restTemplate);
        return response;
    }

    @GetMapping(value = "/statistics")
    public ObjectNode statistics() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode successArray = mapper.createArrayNode();
        ArrayNode errorArray = mapper.createArrayNode();
        ArrayNode latencyArray = mapper.createArrayNode();
        ArrayNode cooldownArray = mapper.createArrayNode();

        for (var item : routingConfig.getInstances()) {
            successArray.add(item + " " + metricService.getMetric(MetricType.SUCCESS_COUNT, item));
        }
        for (var item : routingConfig.getInstances()) {
            errorArray.add(item + " " + metricService.getMetric(MetricType.ERROR_COUNT, item));
        }
        for (var item : routingConfig.getInstances()) {
            latencyArray.add(item + " " + metricService.getMetric(MetricType.LATENCY_AVERAGE, item));
        }
        for(var item : routingConfig.getInstances()) {
            cooldownArray.add(item + " " + !circuitBreakerService.isCircuitClosed(item));
        }
        rootNode.put("SUCCESS_COUNT", successArray);
        rootNode.put("ERROR_COUNT", errorArray);
        rootNode.put("LATENCY", latencyArray);
        rootNode.put("COOLDOWNS", cooldownArray);

        return rootNode;
    }


}
