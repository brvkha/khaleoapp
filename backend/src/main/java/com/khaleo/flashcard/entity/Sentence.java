package com.khaleo.flashcard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "sentences")
public class Sentence extends BaseAuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "transcript", nullable = false, columnDefinition = "text")
    private String transcript;

    @Column(name = "translation", columnDefinition = "text")
    private String translation;

    @Column(name = "aliases_json", columnDefinition = "text")
    private String aliasesJson;

    @Column(name = "media_url", length = 2048)
    private String mediaUrl;

    @Column(name = "start_time", precision = 10, scale = 3)
    private BigDecimal startTime;

    @Column(name = "end_time", precision = 10, scale = 3)
    private BigDecimal endTime;
}

