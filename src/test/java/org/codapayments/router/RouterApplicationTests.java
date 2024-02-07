package org.codapayments.router;

import org.codapayments.router.config.RoutingConfig;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouterController.class)
@EnableConfigurationProperties(RoutingConfig.class)
class RouterApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testHappyPath() throws Exception {
		String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
		JSONObject payload = new JSONObject(payloadString);
		this.mockMvc
				.perform(post("/echo").contentType(MediaType.APPLICATION_JSON).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
	}

	@Test
	void testAnyUrl() throws Exception {
		String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
		JSONObject payload = new JSONObject(payloadString);
		this.mockMvc
				.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
		this.mockMvc
				.perform(post("/echo/abc").contentType(MediaType.APPLICATION_JSON).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
		this.mockMvc
				.perform(post("/echo/abc/cde/efrgt").contentType(MediaType.APPLICATION_JSON).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
	}

	@Test
	void testAnyMediaType() throws Exception {
		String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
		JSONObject payload = new JSONObject(payloadString);
		this.mockMvc
				.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
		this.mockMvc
				.perform(post("/echo/abc").contentType(MediaType.TEXT_PLAIN).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
		this.mockMvc
				.perform(post("/echo/abc/cde/efrgt").contentType(MediaType.TEXT_HTML).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
	}

	@Test
	void testOnlyPostAllowed() throws Exception {
		String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
		JSONObject payload = new JSONObject(payloadString);
		this.mockMvc
				.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(payload.toString()))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString()));
		this.mockMvc
				.perform(get("/echo/abc").contentType(MediaType.TEXT_PLAIN).content(payload.toString()))
				.andExpect(status().is(405));
		this.mockMvc
				.perform(put("/echo/abc/cde/efrgt").contentType(MediaType.TEXT_HTML).content(payload.toString()))
				.andExpect(status().is(405));
	}

	@Test
	void testInvalidJsonPayload() throws Exception {
		String payloadString = "{\"game\":\"Mobile Legends\", \"gamerID\":\"GYUTDTE\", \"points\":20}";
		JSONObject payload = new JSONObject(payloadString);
		this.mockMvc
				.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(payload.toString().substring(1)))
				.andExpect(status().isOk()).andExpect(content().string(payload.toString().substring(1)));
	}

}
