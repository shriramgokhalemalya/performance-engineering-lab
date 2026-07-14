package com.perflab.login;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class LoginServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void loginEndpointReturnsJwt() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "admin",
								  "password": "password"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value(matchesPattern("^[^.]+\\.[^.]+\\.[^.]+$")))
				.andExpect(jsonPath("$.expiresIn").value(3600));
	}

	@Test
	void loginEndpointRejectsInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "admin",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void allocationEndpointAllocatesRequestedMegabytes() throws Exception {
		mockMvc.perform(get("/allocate/3"))
				.andExpect(status().isOk())
				.andExpect(content().string("Allocated 3 MB"));
	}

	@Test
	void shortAllocationPathIsAccessibleWithoutAuth() throws Exception {
		mockMvc.perform(get("/allocate/3"))
				.andExpect(status().isOk())
				.andExpect(content().string("Allocated 3 MB"));
	}

	@Test
	void shortAllocationPathIgnoresInvalidAuthorizationHeader() throws Exception {
		mockMvc.perform(get("/allocate/3")
					.header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isOk())
				.andExpect(content().string("Allocated 3 MB"));
	}

	@Test
	void protectedEndpointRejectsMissingToken() throws Exception {
		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointAcceptsValidToken() throws Exception {
		String token = loginAndGetToken();

		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"));
	}

	@Test
	void usersListEndpointReadsFromUsersTable() throws Exception {
		String token = loginAndGetToken();

		mockMvc.perform(post("/api/users/add")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "user1"
								}
							"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/users/all")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("user1")));

		mockMvc.perform(get("/api/user/all")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("user1")));
	}

	private String loginAndGetToken() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "admin",
								  "password": "password"
								}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.get("token").asText();
	}
}
