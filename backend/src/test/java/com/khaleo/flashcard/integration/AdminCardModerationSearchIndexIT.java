package com.khaleo.flashcard.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.khaleo.flashcard.controller.admin.dto.AdminCardUpdateRequest;
import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.repository.CardRepository;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.admin.AdminModerationService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AdminCardModerationSearchIndexIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AdminModerationService adminModerationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void refreshesSearchTextOnAdminUpdate() {
        User admin = userRepository.saveAndFlush(User.builder()
                .email("admin-search-it@example.com")
                .username("adminsearchit")
                .passwordHash(passwordEncoder.encode("Passw0rd!"))
                .role(UserRole.ROLE_ADMIN)
                .isEmailVerified(true)
                .dailyLearningLimit(20)
                .build());
        User owner = userRepository.saveAndFlush(User.builder()
                .email("owner-search-it@example.com")
                .username("ownersearchit")
                .passwordHash(passwordEncoder.encode("Passw0rd!"))
                .role(UserRole.ROLE_USER)
                .isEmailVerified(true)
                .dailyLearningLimit(20)
                .build());

        Deck deck = deckRepository.saveAndFlush(Deck.builder().author(owner).name("Deck").description("d").isPublic(false).tags("t").build());
        Card card = cardRepository.saveAndFlush(Card.builder()
                .deck(deck)
                .frontContent("<p>Alpha</p>")
                .backContent("<p>Beta</p>")
                .searchText("alpha beta")
                .frontText("Alpha")
                .backText("Beta")
                .examplesJson("[]")
                .build());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(admin.getId().toString(), null, java.util.List.of()));
        SecurityContextHolder.setContext(context);

        adminModerationService.updateCard(card.getId(), new AdminCardUpdateRequest("<p>Gamma</p>", null, "<p>Delta</p>", null));
        Card refreshed = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(refreshed.getSearchText()).contains("gamma").contains("delta");
    }
}

