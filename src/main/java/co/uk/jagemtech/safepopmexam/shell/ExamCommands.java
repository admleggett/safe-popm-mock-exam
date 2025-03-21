package co.uk.jagemtech.safepopmexam.shell;

import co.uk.jagemtech.safepopmexam.model.Choice;
import co.uk.jagemtech.safepopmexam.model.Question;
import co.uk.jagemtech.safepopmexam.repository.ClaudeQuestionRepository;
import co.uk.jagemtech.safepopmexam.service.ExamService;
import co.uk.jagemtech.safepopmexam.util.ProgressIndicator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ExamCommands {
    
    private final ExamService examService;
    private final ClaudeQuestionRepository questionRepository;
    private final ProgressIndicator progressIndicator;
    
    @Autowired
    public ExamCommands(ExamService examService, ClaudeQuestionRepository questionRepository) {
        this.examService = examService;
        this.questionRepository = questionRepository;
        this.progressIndicator = new ProgressIndicator(System.out);
    }
    
    @ShellMethod(key = "start-exam", value = "Start a new POPM mock exam")
    public String startExam(@ShellOption(defaultValue = "5") int numberOfQuestions) {
        // Start progress indicator
        progressIndicator.start("Preparing questions...");
        
        try {
            // Run exam setup in a background thread
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                examService.startExam(numberOfQuestions);
            });
            
            // Wait for the future to complete
            future.get();
            
            // Stop progress indicator
            progressIndicator.stop();
            
            return "Starting new POPM mock exam with " + numberOfQuestions + " questions.\n\n" +
                    displayCurrentQuestion();
        } catch (InterruptedException | ExecutionException e) {
            progressIndicator.stop();
            return "Error starting exam: " + e.getMessage();
        }
    }
    
    @ShellMethod(key = "answer", value = "Answer the current question (provide the option number)")
    public String answerQuestion(int choiceNumber) {
        if (!examService.isExamInProgress()) {
            return "No exam is currently in progress. Use 'start-exam' to begin.";
        }
        
        Question currentQuestion = examService.getCurrentQuestion();
        if (currentQuestion == null) {
            return "No current question.";
        }
        
        // Adjust for 0-based indexing in the service
        int choiceIndex = choiceNumber - 1;
        if (choiceIndex < 0 || choiceIndex >= currentQuestion.getChoices().size()) {
            return "Invalid choice number. Please select a number between 1 and " + 
                    currentQuestion.getChoices().size();
        }
        
        boolean isCorrect = examService.submitAnswer(choiceIndex);
        
        StringBuilder response = new StringBuilder();
        response.append(isCorrect ? "Correct! " : "Incorrect. ");
        response.append("The correct answer is: ")
                .append(currentQuestion.getCorrectChoice().getText()).append("\n\n");
        
        if (currentQuestion.getExplanation() != null && !currentQuestion.getExplanation().isEmpty()) {
            response.append("Explanation: ").append(currentQuestion.getExplanation()).append("\n\n");
        }
        
        if (examService.isExamInProgress()) {
            response.append(displayCurrentQuestion());
        } else {
            response.append("Exam completed!\n")
                    .append("Your score: ").append(String.format("%.1f%%", examService.getScore())).append("\n")
                    .append("Correct answers: ").append(examService.getCorrectAnswers())
                    .append(" out of ").append(examService.getTotalQuestions());
        }
        
        return response.toString();
    }
    
    @ShellMethod(key = "current-question", value = "Display the current question")
    public String displayCurrentQuestion() {
        if (!examService.isExamInProgress()) {
            return "No exam is currently in progress. Use 'start-exam' to begin.";
        }
        
        Question question = examService.getCurrentQuestion();
        if (question == null) {
            return "No current question.";
        }
        
        StringBuilder display = new StringBuilder();
        display.append("Question ").append(examService.getCurrentQuestionNumber())
                .append(" of ").append(examService.getTotalQuestions()).append(":\n\n");
        display.append(question.getText()).append("\n\n");
        
        List<Choice> choices = question.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            display.append(i + 1).append(") ").append(choices.get(i).getText()).append("\n");
        }
        
        display.append("\nEnter 'answer [number]' to submit your answer.");
        
        return display.toString();
    }
    
    @ShellMethod(key = "end-exam", value = "End the current exam")
    public String endExam() {
        if (!examService.isExamInProgress()) {
            return "No exam is currently in progress.";
        }
        
        examService.endExam();
        
        return "Exam ended.\n" +
                "Your score: " + String.format("%.1f%%", examService.getScore()) + "\n" +
                "Correct answers: " + examService.getCorrectAnswers() +
                " out of " + examService.getTotalQuestions();
    }
    
    @ShellMethod(key = "exam-help", value = "Display help information for the POPM exam")
    public String examHelp() {
        return "POPM Exam Practice CLI Help\n\n" +
                "Available commands:\n" +
                "- start-exam [number] : Start a new exam with [number] questions (default: 5)\n" +
                "- answer [number]     : Submit your answer for the current question\n" +
                "- current-question    : Display the current question again\n" +
                "- end-exam            : End the current exam and see your score\n" +
                "- refresh-questions [count] : Generate new AI-powered questions (default: 10, processed in batches)\n" +
                "- exam-help           : Display this help information\n\n" +
                "To exit the application, type 'exit'";
    }
    
    @ShellMethod(key = "refresh-questions", value = "Generate new questions using Claude AI (processed in batches of 5)")
    public String refreshQuestions(@ShellOption(defaultValue = "10") int count) {
        if (examService.isExamInProgress()) {
            return "Cannot refresh questions while an exam is in progress. End the current exam first.";
        }
        
        progressIndicator.start("Generating " + count + " new questions using Claude AI (in batches)...");
        
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionRepository.refreshQuestions(count);
            });
            
            future.get();
            progressIndicator.stop();
            
            // Get actual number of questions generated
            int actualCount = questionRepository.getAllQuestions().size();
            return "Successfully generated " + actualCount + " new questions using Claude AI.";
        } catch (Exception e) {
            progressIndicator.stop();
            return "Failed to generate new questions: " + e.getMessage();
        }
    }
}
