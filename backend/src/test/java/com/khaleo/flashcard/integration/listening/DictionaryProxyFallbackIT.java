package com.khaleo.flashcard.integration.listening;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khaleo.flashcard.controller.listening.ListeningSupportController;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.auth.JwtTokenService;
import com.khaleo.flashcard.service.listening.DictionaryProxyService;
import com.khaleo.flashcard.service.listening.ListeningMediaAccessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ListeningSupportController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Dictionary proxy fallback integration tests")
class DictionaryProxyFallbackIT {

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

    @Test
    @DisplayName("returns controlled fallback payload when provider throws")
    void returnsFallbackPayloadOnProviderFailure() throws Exception {
        when(dictionaryProxyService.lookup("unstable")).thenThrow(new RuntimeException("provider down"));

        mockMvc.perform(get("/api/v1/listening/dictionary").param("term", "unstable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code", equalTo("PROVIDER_UNAVAILABLE")))
                .andExpect(jsonPath("$.message", equalTo("Dictionary service is temporarily unavailable.")));
    }
}


