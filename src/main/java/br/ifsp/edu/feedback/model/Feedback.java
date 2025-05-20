package br.ifsp.edu.feedback.model;

import java.time.LocalDateTime;
import java.util.List;

import br.ifsp.edu.feedback.model.enumerations.FeedbackType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String sector;

    @Enumerated(EnumType.STRING)
    private FeedbackType type;

    private boolean anonymous;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ElementCollection
    private List<String> tags;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}