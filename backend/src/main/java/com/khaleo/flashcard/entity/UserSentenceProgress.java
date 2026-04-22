package com.khaleo.flashcard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_sentence_progress",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_sentence_progress", columnNames = {"user_id", "sentence_id"})})
public class UserSentenceProgress extends BaseAuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sentence_id", nullable = false)
    private Sentence sentence;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = Boolean.FALSE;

    @Column(name = "completion_source", length = 32)
    private String completionSource;

    @Column(name = "completed_at")
    private java.time.Instant completedAt;
}

