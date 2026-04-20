package com.khaleo.flashcard.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.khaleo.flashcard.controller.card.dto.BulkCreateCardsRequest;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.persistence.BulkCardImportService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BulkCardImportIntegrationIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BulkCardImportService bulkCardImportService;

    @Test
    void keepsSuccessFailedCountInvariant() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("bulk-it@example.com")
                .username("bulkit")
                .passwordHash(passwordEncoder.encode("Passw0rd!"))
                .role(UserRole.ROLE_USER)
                .isEmailVerified(true)
                .dailyLearningLimit(20)
                .build());
        Deck deck = deckRepository.saveAndFlush(Deck.builder().author(user).name("Deck").description("d").isPublic(false).tags("t").build());

        BulkCreateCardsRequest request = new BulkCreateCardsRequest(List.of(
                new BulkCreateCardsRequest.BulkCreateCardItem(1, "<p>Front</p>", "<p>Back</p>"),
                new BulkCreateCardsRequest.BulkCreateCardItem(2, "", "<p>Back</p>")));

        BulkCreateCardsRequest.BulkCreateCardsResponse response = bulkCardImportService.importCards(deck.getId(), request);
        assertThat(response.successCount() + response.failedCount()).isEqualTo(2);
        assertThat(response.errors()).allMatch(error -> error.line() >= 1);
    }
}

