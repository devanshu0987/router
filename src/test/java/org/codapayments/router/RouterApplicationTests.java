package org.codapayments.router;

import org.codapayments.router.config.RoutingConfig;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties(RoutingConfig.class)
class RouterApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testHappyPath() throws Exception {
        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/echo"))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(payload.toString()));
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
    }

    @Test
    void testAnyUrl() throws Exception {
        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        this.mockMvc.perform(post("/echo/abc").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isNotFound());
        this.mockMvc.perform(post("/echo/abc/cde/efrgt").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isNotFound());
    }

    @Test
    void testAnyMediaType() throws Exception {
        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        this.mockMvc.perform(post("/echo").contentType(MediaType.TEXT_PLAIN).content(payload.toString())).andExpect(status().is(415));
        this.mockMvc.perform(post("/echo").contentType(MediaType.TEXT_HTML).content(payload.toString())).andExpect(status().is(415));
    }

    @Test
    void testOnlyPostAllowed() throws Exception {
        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString())).andExpect(status().isOk()).andExpect(content().string(payload.toString()));
        this.mockMvc.perform(get("/echo").contentType(MediaType.TEXT_PLAIN).content(payload.toString())).andExpect(status().is(405));
        this.mockMvc.perform(put("/echo").contentType(MediaType.TEXT_HTML).content(payload.toString())).andExpect(status().is(405));
    }

    @Test
    void testInvalidJsonPayload() throws Exception {
        String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
        JSONObject payload = new JSONObject(payloadString);
        this.mockMvc.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString().substring(1))).andExpect(status().is(400));
    }
}
