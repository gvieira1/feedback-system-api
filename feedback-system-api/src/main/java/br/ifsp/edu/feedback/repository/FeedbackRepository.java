package br.ifsp.edu.feedback.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import br.ifsp.edu.feedback.model.Feedback;
import br.ifsp.edu.feedback.model.User;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

	List<Feedback> findByAuthor(User author);

}
