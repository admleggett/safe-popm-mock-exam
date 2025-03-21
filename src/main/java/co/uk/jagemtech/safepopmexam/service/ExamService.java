package co.uk.jagemtech.safepopmexam.service;

import co.uk.jagemtech.safepopmexam.model.Question;
import co.uk.jagemtech.safepopmexam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ExamService {
    
    private final QuestionRepository questionRepository;
    private List<Question> examQuestions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private boolean examInProgress = false;
    
    @Autowired
    public ExamService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }
    
    public void startExam(int numberOfQuestions) {
        List<Question> allQuestions = questionRepository.getAllQuestions();
        
        if (numberOfQuestions > allQuestions.size()) {
            numberOfQuestions = allQuestions.size();
        }
        
        // Shuffle and select the specified number of questions
        Collections.shuffle(allQuestions);
        examQuestions = allQuestions.subList(0, numberOfQuestions);
        
        currentQuestionIndex = 0;
        correctAnswers = 0;
        examInProgress = true;
    }
    
    public Question getCurrentQuestion() {
        if (!examInProgress || currentQuestionIndex >= examQuestions.size()) {
            return null;
        }
        return examQuestions.get(currentQuestionIndex);
    }
    
    public boolean submitAnswer(int choiceIndex) {
        if (!examInProgress || currentQuestionIndex >= examQuestions.size()) {
            return false;
        }
        
        Question currentQuestion = examQuestions.get(currentQuestionIndex);
        if (choiceIndex < 0 || choiceIndex >= currentQuestion.getChoices().size()) {
            return false;
        }
        
        boolean isCorrect = currentQuestion.getChoices().get(choiceIndex).isCorrect();
        if (isCorrect) {
            correctAnswers++;
        }
        
        currentQuestionIndex++;
        if (currentQuestionIndex >= examQuestions.size()) {
            examInProgress = false;
        }
        
        return isCorrect;
    }
    
    public boolean isExamInProgress() {
        return examInProgress;
    }
    
    public int getTotalQuestions() {
        return examQuestions != null ? examQuestions.size() : 0;
    }
    
    public int getCurrentQuestionNumber() {
        return currentQuestionIndex + 1;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public double getScore() {
        if (examQuestions == null || examQuestions.isEmpty()) {
            return 0.0;
        }
        return (double) correctAnswers / examQuestions.size() * 100;
    }
    
    public void endExam() {
        examInProgress = false;
    }
}
