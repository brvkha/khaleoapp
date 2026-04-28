package com.khaleo.flashcard.contract.listening;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khaleo.flashcard.controller.listening.ListeningSupportController;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.auth.JwtTokenService;
import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupResponse;
import com.khaleo.flashcard.controller.listening.dto.MediaAccessResponse;
import com.khaleo.flashcard.service.listening.DictionaryProxyService;
import com.khaleo.flashcard.service.listening.ListeningMediaAccessService;
import com.khaleo.flashcard.service.listening.ListeningStructuredLogger;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(ListeningSupportController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Listening support API contract tests")
class ListeningDictionaryMediaContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListeningMediaAccessService listeningMediaAccessService;

    @MockBean
    private DictionaryProxyService dictionaryProxyService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ListeningStructuredLogger listeningStructuredLogger;

    @Test
    @DisplayName("POST /api/v1/listening/media/access returns presigned metadata")
    void mediaAccessReturnsMetadata() throws Exception {
        Instant expiresAt = Instant.parse("2026-04-21T00:00:00Z");
        when(listeningMediaAccessService.issueAccessUrl(anyString()))
                .thenReturn(new MediaAccessResponse("https://signed.example/audio.mp3", expiresAt));

        mockMvc.perform(post("/api/v1/listening/media/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaUrl\":\"s3://bucket/audio.mp3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", equalTo("https://signed.example/audio.mp3")))
                .andExpect(jsonPath("$.expiresAt", equalTo("2026-04-21T00:00:00Z")));
    }

    @Test
    @DisplayName("GET /api/v1/listening/dictionary returns success payload")
    void dictionaryLookupReturnsSuccessPayload() throws Exception {
        DictionaryLookupResponse response = new DictionaryLookupResponse(
                "practice",
                List.of(new DictionaryLookupResponse.Entry("/prak.tis/", null, null, List.of("to do repeatedly"))));
        when(dictionaryProxyService.lookup("practice")).thenReturn(response);

        mockMvc.perform(get("/api/v1/listening/dictionary").param("term", "practice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term", equalTo("practice")))
                .andExpect(jsonPath("$.entries", hasSize(1)))
                .andExpect(jsonPath("$.entries[0].ipa", equalTo("/prak.tis/")))
                .andExpect(jsonPath("$.entries[0].definitions", hasSize(1)));
    }
}


