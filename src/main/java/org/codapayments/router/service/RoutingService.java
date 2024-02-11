package org.codapayments.router.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.instanceListSupplier.impl.ServiceInstanceListSupplierFactory;
import org.codapayments.router.statistics.MetricType;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicLong;

public class RoutingService {

    private RoutingAlgorithm routingAlgorithm;
    private ServiceInstanceListSupplier supplier;
    private CircuitBreakerService circuitBreakerService;
    private MetricService metricService;

    public RoutingService(RoutingConfig routingConfig, MetricService metricService) {
        this.routingAlgorithm = RoutingAlgorithmFactory.getInstance(routingConfig);
        this.metricService = metricService;

        circuitBreakerService = new CircuitBreakerService(routingConfig, metricService);
        this.supplier = ServiceInstanceListSupplierFactory.getInstance(routingConfig, circuitBreakerService);
    }

    public ResponseEntity<ObjectNode> route(RequestEntity<ObjectNode> req, RestTemplate restTemplate) {

        URI redirectURI = routingAlgorithm.chooseServiceInstance(supplier);

        // No routes are available.
        if (redirectURI == null) {
            return new ResponseEntity<>(null, null, HttpStatus.SERVICE_UNAVAILABLE);
        }

        if (!circuitBreakerService.isCircuitClosed(redirectURI)) {
            // Todo: Either we just return OR we retry. For now, just return.
            return new ResponseEntity<>(null, null, HttpStatus.SERVICE_UNAVAILABLE);
        }

        URI downstreamURI = prepareDownstreamURI(req, redirectURI);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirectURI.toString());

        try {
            long beforeTime = System.currentTimeMillis();
            HttpEntity<ObjectNode> downstreamRequest = new HttpEntity<>(req.getBody());
            ResponseEntity<ObjectNode> resp = restTemplate.postForEntity(downstreamURI, downstreamRequest, ObjectNode.class);
            HttpHeaders responseHeaders = prepareResponseHeaders(resp, headers);
            if (resp.getStatusCode().is2xxSuccessful()) {
                metricService.addMetric(MetricType.SUCCESS_COUNT, redirectURI, 1D);
            }
            metricService.addMetric(MetricType.LATENCY_AVERAGE, redirectURI, (System.currentTimeMillis() - beforeTime) * 1D);
            return new ResponseEntity<>(resp.getBody(), responseHeaders, HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(null, headers, ex.getStatusCode());
        } catch (HttpServerErrorException ex) {
            metricService.addMetric(MetricType.ERROR_COUNT, redirectURI, 1D);
            return new ResponseEntity<>(null, headers, ex.getStatusCode());
        } catch (Exception ex) {
            metricService.addMetric(MetricType.ERROR_COUNT, redirectURI, 1D);
        }
        return new ResponseEntity<>(null, headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private URI prepareDownstreamURI(RequestEntity<ObjectNode> req, URI redirectURI) {
        var part = req.getUrl().getRawPath();
        var query = req.getUrl().getQuery() == null ? "" : "?" + req.getUrl().getQuery();
        try {
            return new URI(redirectURI.toString() + part + query);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders prepareResponseHeaders(ResponseEntity<ObjectNode> resp, HttpHeaders headers) {
        headers.addAll(resp.getHeaders());
        return headers;
    }
}
