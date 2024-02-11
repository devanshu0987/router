package org.codapayments.router.controller;

import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.controller.RouterController;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties(RoutingConfig.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class RouterControllerTests {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void testHappyPath() throws Exception {
        var mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RouterController controller = mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(RouterController.class);
        controller.initialize();
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));
        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
    }

    @Test
    void testAnyUrl() throws Exception {
        var mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RouterController controller = mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(RouterController.class);
        controller.initialize();
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8082/echo/abc"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8083/echo/abc/cde/efrgt"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        mockMvc.perform(post("/echo/abc").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isNotFound());
        mockMvc.perform(post("/echo/abc/cde/efrgt").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isNotFound());
    }

    @Test
    void testAnyMediaType() throws Exception {
        var mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RouterController controller = mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(RouterController.class);
        controller.initialize();
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));

        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        mockMvc.perform(post("/echo").contentType(MediaType.TEXT_PLAIN).content(payload.toString())).andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMediaTypeNotSupportedException));
        mockMvc.perform(post("/echo").contentType(MediaType.TEXT_HTML).content(payload.toString())).andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMediaTypeNotSupportedException));
    }

    @Test
    void testOnlyPostAllowed() throws Exception {
        var mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RouterController controller = mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(RouterController.class);
        controller.initialize();
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));

        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        mockMvc.perform(get("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(result -> {
            assertTrue(result.getResolvedException() instanceof HttpRequestMethodNotSupportedException);
        });
        mockMvc.perform(put("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(result -> {
            assertTrue(result.getResolvedException() instanceof HttpRequestMethodNotSupportedException);
        });
    }

    @Test
    void testInvalidJsonPayload() throws Exception {
        var mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RouterController controller = mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(RouterController.class);
        controller.initialize();
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString().substring(1))).andExpect(result -> {
            assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException);
        });
    }

    @Test
    void testDifferentHttpStatusCodes() throws Exception {
        var mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RouterController controller = mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(RouterController.class);
        controller.initialize();
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8082/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8083/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withException(new IOException()));

        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().is(500));
        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().is(504));
        mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().is(503));
    }
}
