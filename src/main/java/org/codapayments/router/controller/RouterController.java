package org.codapayments.router.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.service.MetricService;
import org.codapayments.router.service.RoutingService;
import org.codapayments.router.statistics.MetricType;
import org.codapayments.router.statistics.StatisticType;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.instanceListSupplier.impl.ServiceInstanceListSupplierFactory;
import org.codapayments.router.statistics.MetricStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class RouterController {

    @Autowired
    private RoutingConfig routingConfig;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);

    private RoutingService routingService;
    private MetricService metricService;

    @PostConstruct
    public void initialize() {
        logger.info(routingConfig.toString());
        metricService = new MetricService(routingConfig);
        routingService = new RoutingService(routingConfig, metricService);
    }

    // We should get any request and then try to pass it onto the downstream.
    @PostMapping(value = "/**", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ObjectNode> index(RequestEntity<ObjectNode> req) {

        // validate req body.
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

        for (var item : routingConfig.getInstances()) {
            successArray.add(item + " " + metricService.getMetric(MetricType.SUCCESS_COUNT, item));
        }
        for (var item : routingConfig.getInstances()) {
            errorArray.add(item + " " + metricService.getMetric(MetricType.ERROR_COUNT, item));
        }
        for (var item : routingConfig.getInstances()) {
            latencyArray.add(item + " " + metricService.getMetric(MetricType.LATENCY_AVERAGE, item));
        }
        rootNode.put("SUCCESS_COUNT", successArray);
        rootNode.put("ERROR_COUNT", errorArray);
        rootNode.put("LATENCY", latencyArray);

        return rootNode;
    }


}
