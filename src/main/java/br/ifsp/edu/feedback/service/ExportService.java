package br.ifsp.edu.feedback.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Service;

import br.ifsp.edu.feedback.model.Feedback;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ExportService {

	private final FeedbackService feedbackService;

	public Path exportFeedbacksToCsvFile() {
		List<Feedback> feedbacks = feedbackService.getAllFeedbacksInListFormat();
		ByteArrayInputStream csvStream = generateFeedbackCsv(feedbacks);

		try {
			Path exportPath = Paths.get("exports");
			if (!Files.exists(exportPath)) {
				Files.createDirectories(exportPath);
			}

			Path filePath = exportPath.resolve("feedbacks-export.csv");
			Files.write(filePath, csvStream.readAllBytes());

			return filePath;

		} catch (IOException e) {
			throw new RuntimeException("Erro ao salvar CSV", e);
		}
	}

	//EXPORTA FEEDBACKS PARA .CSV
	public ByteArrayInputStream generateFeedbackCsv(List<Feedback> feedbacks) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (PrintWriter writer = new PrintWriter(out)) {
			writer.println("ID,Título,Conteúdo,Setor,Tipo,Anônimo,Data de Criação,Tags,Autor");

			for (Feedback feedback : feedbacks) {
				writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%b,\"%s\",\"%s\",\"%s\"%n", feedback.getId(),
						feedback.getTitulo().replace("\"", "\"\""), feedback.getContent().replace("\"", "\"\""),
						feedback.getSector(), feedback.getType(), feedback.isAnonymous(), feedback.getCreatedAt(),
						String.join(";", feedback.getTags()),
						feedback.isAnonymous() ? "Anônimo" : feedback.getAuthor().getUsername());
			}

			writer.flush();
			return new ByteArrayInputStream(out.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("Erro ao gerar CSV", e);
		}
	}

	
}
