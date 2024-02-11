package org.codapayments.router.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.codapayments.router.algorithm.RoutingAlgorithm;
import org.codapayments.router.algorithm.RoutingAlgorithmType;
import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.serviceInstanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.serviceInstanceListSupplier.ServiceInstanceListSupplierType;
import org.codapayments.router.serviceInstanceListSupplier.impl.ServiceInstanceListSupplierFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;


@SpringBootTest
public class RoutingServiceTests {
    @Autowired
    private RestTemplate restTemplate;

    private RoutingConfig getRoutingConfig(RoutingAlgorithmType type) {
        RoutingConfig config = new RoutingConfig();
        config.setRoutingAlgorithm(type);
        config.setSupplierType(ServiceInstanceListSupplierType.STATIC);
        config.setCooldownTimeoutInSeconds(5);
        try {
            config.setInstances(List.of(
                    new URI("http://localhost:8081"),
                    new URI("http://localhost:8082"),
                    new URI("http://localhost:8083"),
                    new URI("http://localhost:8084"))
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    @Test
    public void testHappyPath() throws URISyntaxException {
        var config = getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        MetricService metricService = new MetricService(config);
        RoutingService service = new RoutingService(config, metricService);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        RequestEntity<ObjectNode> request = new RequestEntity<>(root, HttpMethod.POST, config.getInstances().get(0));
        var mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(config.getInstances().get(0)))
                .andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON).body(root.toString()));

        // ACT
        var response = service.route(request, restTemplate);

        assert response.getStatusCode().is2xxSuccessful();
    }
}
