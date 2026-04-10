package com.khaleo.flashcard.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khaleo.flashcard.integration.support.IntegrationPersistenceTestBase;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class SwaggerDocsIntegrationIT extends IntegrationPersistenceTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldServeSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Swagger UI")));
    }

    @Test
    void shouldExposeOpenApiWithCoreDomains() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode paths = objectMapper.readTree(body).path("paths");
        assertThat(paths.isObject()).isTrue();

        assertThat(paths.has("/api/v1/auth/login")).isTrue();

        java.util.List<String> discoveredPaths = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(paths.fieldNames(), 0), false)
                .toList();

        assertThat(discoveredPaths).anyMatch(path -> path.startsWith("/api/v1/decks"));
        assertThat(discoveredPaths).anyMatch(path -> path.contains("/cards"));
        assertThat(discoveredPaths).anyMatch(path -> path.startsWith("/api/v1/study") || path.startsWith("/api/v1/study-session"));
    }
}

