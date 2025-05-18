package br.ifsp.edu.feedback.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.User;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
	
	// Busca por autor
    List<Feedback> findByAuthor(User author);
    
    // Busca por setor
    List<Feedback> findBySector(String sector);

    // Busca por data (intervalo do dia)
    @Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :start AND :end")
    List<Feedback> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Busca por setor e data (intervalo do dia)
    @Query("SELECT f FROM Feedback f WHERE f.sector = :sector AND f.createdAt BETWEEN :start AND :end")
    List<Feedback> findBySectorAndCreatedAtBetween(@Param("sector") String sector, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
