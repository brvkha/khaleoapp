package com.khaleo.flashcard.contract.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.repository.CardRepository;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.auth.JwtTokenService;
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
class AdminCardModerationRichContentContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void acceptsCanonicalPayloadForAdminCardUpdate() throws Exception {
        User admin = saveUser("admin-rich-contract@example.com", UserRole.ROLE_ADMIN);
        User owner = saveUser("owner-rich-contract@example.com", UserRole.ROLE_USER);
        Deck deck = deckRepository.saveAndFlush(Deck.builder().author(owner).name("Deck").description("d").isPublic(false).tags("t").build());
        Card card = cardRepository.saveAndFlush(Card.builder()
                .deck(deck)
                .frontContent("<p>front</p>")
                .backContent("<p>back</p>")
                .searchText("front back")
                .frontText("front")
                .backText("back")
                .examplesJson("[]")
                .build());

        String body = objectMapper.writeValueAsString(Map.of(
                "frontContent", "<p>updated-front</p>",
                "backContent", "<p>updated-back</p>"));

        mockMvc.perform(put("/api/v1/admin/cards/{cardId}", card.getId())
                        .header("Authorization", bearerFor(admin))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());
    }

    private User saveUser(String email, UserRole role) {
        return userRepository.saveAndFlush(User.builder()
                .email(email)
                .username(email.substring(0, email.indexOf('@')))
                .passwordHash(passwordEncoder.encode("Passw0rd!"))
                .role(role)
                .isEmailVerified(true)
                .dailyLearningLimit(20)
                .build());
    }

    private String bearerFor(User user) {
        return "Bearer " + jwtTokenService.createAccessToken(user.getId().toString(), Map.of("role", user.getRole().name()));
    }
}

