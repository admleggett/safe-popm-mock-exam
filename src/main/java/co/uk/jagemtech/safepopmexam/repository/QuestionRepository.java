package co.uk.jagemtech.safepopmexam.repository;

import co.uk.jagemtech.safepopmexam.model.Question;
import java.util.List;

public interface QuestionRepository {
    List<Question> getAllQuestions();
}
