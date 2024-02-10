package org.codapayments.router;

import org.codapayments.router.config.RoutingConfig;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties(RoutingConfig.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class RouterApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setUp() {
        // mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testHappyPath() throws Exception {
        var mockServer = MockRestServiceServer.createServer(restTemplate);
        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
    }

    @Test
    void testAnyUrl() throws Exception {
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8082/echo/abc"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8083/echo/abc/cde/efrgt"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        this.mockMvc.perform(post("/echo/abc").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isNotFound());
        this.mockMvc.perform(post("/echo/abc/cde/efrgt").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isNotFound());
    }

    @Test
    void testAnyMediaType() throws Exception {
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));

        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        this.mockMvc.perform(post("/echo").contentType(MediaType.TEXT_PLAIN).content(payload.toString())).andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMediaTypeNotSupportedException));
        this.mockMvc.perform(post("/echo").contentType(MediaType.TEXT_HTML).content(payload.toString())).andExpect(result -> assertTrue(result.getResolvedException() instanceof HttpMediaTypeNotSupportedException));
    }

    @Test
    void testOnlyPostAllowed() throws Exception {
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));

        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        this.mockMvc.perform(get("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(result -> {
            assertTrue(result.getResolvedException() instanceof HttpRequestMethodNotSupportedException);
        });
        this.mockMvc.perform(put("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(result -> {
            assertTrue(result.getResolvedException() instanceof HttpRequestMethodNotSupportedException);
        });
    }

    @Test
    void testInvalidJsonPayload() throws Exception {
        var mockServer = MockRestServiceServer.createServer(restTemplate);

        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString().substring(1))).andExpect(result -> {
            assertTrue(result.getResolvedException() instanceof HttpMessageNotReadableException);
        });
    }
}
