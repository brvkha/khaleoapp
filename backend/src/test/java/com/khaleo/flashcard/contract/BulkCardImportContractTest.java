package com.khaleo.flashcard.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.integration.support.IntegrationPersistenceTestBase;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.auth.JwtTokenService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("null")
class BulkCardImportContractTest extends IntegrationPersistenceTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void rejectsCardsLengthAbove500AtRequestLevel() throws Exception {
        User owner = saveUser("bulk-contract-owner@example.com");
        Deck deck = saveDeck(owner);

        List<Map<String, Object>> cards = java.util.stream.IntStream.range(0, 501)
                .mapToObj(index -> Map.<String, Object>of(
                        "line", index + 1,
                        "frontContent", "<p>f</p>",
                        "backContent", "<p>b</p>"))
                .toList();

        String body = objectMapper.writeValueAsString(Map.of("cards", cards));

        mockMvc.perform(post("/api/v1/decks/{deckId}/cards/bulk", deck.getId())
                        .header("Authorization", bearerFor(owner))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private User saveUser(String email) {
        return userRepository.saveAndFlush(User.builder()
                .email(email)
                .username(email.substring(0, email.indexOf('@')))
                .passwordHash(passwordEncoder.encode("Passw0rd!"))
                .role(UserRole.ROLE_USER)
                .isEmailVerified(true)
                .dailyLearningLimit(20)
                .build());
    }

    private Deck saveDeck(User owner) {
        return deckRepository.saveAndFlush(Deck.builder()
                .author(owner)
                .name("Deck")
                .description("desc")
                .isPublic(false)
                .tags("tag")
                .build());
    }

    private String bearerFor(User user) {
        return "Bearer " + jwtTokenService.createAccessToken(user.getId().toString(), Map.of("role", user.getRole().name()));
    }
}

