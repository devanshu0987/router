package org.codapayments.router.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.codapayments.router.TestConfig;
import org.codapayments.router.algorithm.RoutingAlgorithmType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;


@SpringBootTest
public class RoutingServiceTests {
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testHappyPath() throws URISyntaxException {
        var config = TestConfig.getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
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
