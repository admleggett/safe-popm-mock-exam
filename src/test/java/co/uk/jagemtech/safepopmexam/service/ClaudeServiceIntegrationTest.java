package co.uk.jagemtech.safepopmexam.service;

import co.uk.jagemtech.safepopmexam.model.Choice;
import co.uk.jagemtech.safepopmexam.model.Question;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.shell.interactive.enabled=false",
    "spring.shell.command.script.enabled=false"
})
@Tag("integration")
@Disabled("Enable this test explicitly with: mvn test -Dgroups=integration -DincludeCategories=integration")
@ActiveProfiles("integration-test")
public class ClaudeServiceIntegrationTest {

    @Autowired
    private ClaudeService claudeService;

    @Test
    void testGenerateQuestions() {
        // Act
        List<Question> questions = claudeService.generateQuestions(2);
        
        // Assert
        assertNotNull(questions);
        assertFalse(questions.isEmpty());
        assertTrue(questions.size() <= 2);
        
        // Check first question structure
        Question question = questions.get(0);
        assertNotNull(question.getText());
        assertFalse(question.getText().isEmpty());
        
        // Verify choices structure
        List<Choice> choices = question.getChoices();
        assertNotNull(choices);
        assertEquals(4, choices.size());
        
        // Verify exactly one choice is marked as correct
        long correctCount = choices.stream().filter(Choice::isCorrect).count();
        assertEquals(1, correctCount);
        
        // Verify explanation is present
        assertNotNull(question.getExplanation());
        assertFalse(question.getExplanation().isEmpty());
        
        // Print results for manual inspection
        System.out.println("Generated question: " + question.getText());
        System.out.println("Explanation: " + question.getExplanation());
    }
    
    @Test
    void testErrorHandling() {
        // This test verifies the service can handle requesting a large number of questions
        // (potential timeout or token limit issues)
        
        // Act 
        List<Question> questions = claudeService.generateQuestions(20);
        
        // Assert - we should get some questions even if model limits are reached
        assertNotNull(questions);
        assertFalse(questions.isEmpty());
    }
}
