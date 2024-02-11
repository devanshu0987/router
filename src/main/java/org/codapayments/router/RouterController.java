package org.codapayments.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.enums.StatisticType;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.instanceListSupplier.impl.ServiceInstanceListSupplierFactory;
import org.codapayments.router.statistics.MetricStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class RouterController {

    @Autowired
    private RoutingConfig routingConfig;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);
    private RoutingAlgorithm router;
    private ServiceInstanceListSupplier supplier;
    private MetricStatistics latencyMetric;
    private MetricStatistics successCountMetric;
    private MetricStatistics errorCountMetric;

    public RouterController() {
        // logger.info("From the controller" + routingConfig.toString());
    }

    @PostConstruct
    public void initialize() {
        logger.info(routingConfig.toString());
        router = RoutingAlgorithmFactory.getInstance(routingConfig);
        supplier = ServiceInstanceListSupplierFactory.getInstance(routingConfig);
        latencyMetric = new MetricStatistics(StatisticType.SLIDING_WINDOW_AVERAGE, routingConfig);
        errorCountMetric = new MetricStatistics(StatisticType.SLIDING_WINDOW_COUNT, routingConfig);
        successCountMetric = new MetricStatistics(StatisticType.SLIDING_WINDOW_COUNT, routingConfig);
    }

    // We should get any request and then try to pass it onto the downstream.
    // We accept any path.
    // We accept any media type
    // TODO: How to accept any Object here. No bar on what you send?
    @PostMapping(value = "/**")
    public ResponseEntity<?> index(RequestEntity<ObjectNode> req) {
        URI redirectURI = router.route(supplier);
        // No routes are available.
        if(redirectURI == null) {
            return new ResponseEntity<>(null, null, HttpStatus.SERVICE_UNAVAILABLE);
        }

        // check if high error count.
        if (errorCountMetric.getStatistic(redirectURI) > routingConfig.getErrorCountForCooldown()
                || latencyMetric.getStatistic(redirectURI) > 1000 * routingConfig.getLatencyForCooldownInSeconds()) {
            // take the instance out for timeout configured in the config.
            setCoolDown(redirectURI);
            return new ResponseEntity<>(null, null, HttpStatus.SERVICE_UNAVAILABLE);
        }

        var part = req.getUrl().getRawPath();
        var query = req.getUrl().getQuery() == null ? "": "?" + req.getUrl().getQuery();
        HttpHeaders headers = new HttpHeaders();
        // to indicate which server actually responded to us.
        headers.add("Location", redirectURI.toString());

        // call application server
        try {
            AtomicLong beforeTime = new AtomicLong(System.currentTimeMillis());
            HttpEntity<ObjectNode> request = new HttpEntity<>(req.getBody());
            ResponseEntity<ObjectNode> resp = restTemplate.postForEntity(redirectURI.toString() + part + query, request, ObjectNode.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                successCountMetric.addData(redirectURI, System.currentTimeMillis(), 1D);
            }
            latencyMetric.addData(redirectURI, System.currentTimeMillis(), System.currentTimeMillis() - beforeTime.doubleValue());
            headers.addAll(resp.getHeaders());
            return new ResponseEntity<>(resp.getBody(), headers, HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            // 4xx: Bad Request, Un-Authorized, Not Found.
            // Client error: Do not increment error count.
            return new ResponseEntity<>(null, headers, ex.getStatusCode());
        } catch (HttpServerErrorException ex) {
            errorCountMetric.addData(redirectURI, System.currentTimeMillis(), 1D);
            return new ResponseEntity<>(null, headers, ex.getStatusCode());
        }
        catch (Exception ex) {
            errorCountMetric.addData(redirectURI, System.currentTimeMillis(), 1D);
        }

        return new ResponseEntity<>(null, headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping(value = "/statistics")
    public ObjectNode statistics() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode successArray = mapper.createArrayNode();
        ArrayNode errorArray = mapper.createArrayNode();
        ArrayNode latencyArray = mapper.createArrayNode();

        for (var item : routingConfig.getInstances()) {
            successArray.add(item + " " + successCountMetric.getStatistic(item));
        }
        for (var item : routingConfig.getInstances()) {
            errorArray.add(item + " " + errorCountMetric.getStatistic(item));
        }
        for (var item : routingConfig.getInstances()) {
            latencyArray.add(item + " " + latencyMetric.getStatistic(item));
        }
        rootNode.put("SUCCESS_COUNT", successArray);
        rootNode.put("ERROR_COUNT", errorArray);
        rootNode.put("LATENCY", latencyArray);

        return rootNode;
    }

    private void setCoolDown(URI uri) {
        supplier.setCooldown(uri, System.currentTimeMillis() + (routingConfig.getCooldownTimeoutInSeconds() * 1000));
    }
}
