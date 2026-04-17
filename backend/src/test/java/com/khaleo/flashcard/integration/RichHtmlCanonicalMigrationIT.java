package com.khaleo.flashcard.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.repository.CardRepository;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RichHtmlCanonicalMigrationIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void persistsCanonicalColumnsAfterMigration() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("migration-it@example.com")
                .username("migrationit")
                .passwordHash(passwordEncoder.encode("Passw0rd!"))
                .role(UserRole.ROLE_USER)
                .isEmailVerified(true)
                .dailyLearningLimit(20)
                .build());
        Deck deck = deckRepository.saveAndFlush(Deck.builder().author(user).name("Deck").description("d").isPublic(false).tags("t").build());

        Card card = cardRepository.saveAndFlush(Card.builder()
                .deck(deck)
                .frontContent("<p>Front</p>")
                .backContent("<p>Back</p>")
                .searchText("front back")
                .frontText("Front")
                .backText("Back")
                .examplesJson("[]")
                .build());

        assertThat(card.getFrontContent()).isEqualTo("<p>Front</p>");
        assertThat(card.getBackContent()).isEqualTo("<p>Back</p>");
        assertThat(card.getSearchText()).isEqualTo("front back");
    }
}

