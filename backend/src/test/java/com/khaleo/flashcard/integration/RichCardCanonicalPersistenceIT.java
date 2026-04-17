package com.khaleo.flashcard.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.repository.CardRepository;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.persistence.CardSearchTextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RichCardCanonicalPersistenceIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardSearchTextBuilder cardSearchTextBuilder;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void keepsSearchTextInSyncWithCanonicalHtml() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("canonical-it@example.com")
                .username("canonicalit")
                .passwordHash(passwordEncoder.encode("Passw0rd!"))
                .role(UserRole.ROLE_USER)
                .isEmailVerified(true)
                .dailyLearningLimit(20)
                .build());
        Deck deck = deckRepository.saveAndFlush(Deck.builder().author(user).name("Deck").description("d").isPublic(false).tags("t").build());

        String front = "<p>Hello</p>";
        String back = "<p>World</p>";
        Card card = cardRepository.saveAndFlush(Card.builder()
                .deck(deck)
                .frontContent(front)
                .backContent(back)
                .searchText(cardSearchTextBuilder.fromCanonicalHtml(front, back))
                .frontText(front)
                .backText(back)
                .examplesJson("[]")
                .build());

        assertThat(card.getSearchText()).contains("hello").contains("world");
    }
}

