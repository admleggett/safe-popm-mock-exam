package co.uk.jagemtech.safepopmexam.repository;

import co.uk.jagemtech.safepopmexam.model.Choice;
import co.uk.jagemtech.safepopmexam.model.Question;
import co.uk.jagemtech.safepopmexam.service.ClaudeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.shell.interactive.enabled=false",
    "spring.shell.command.script.enabled=false"
})
@TestPropertySource(properties = {
    "spring.main.web-application-type=NONE"
})
public class ClaudeQuestionRepositoryIntegrationTest {

    @MockBean
    private ClaudeService claudeService;

    @Autowired
    private ClaudeQuestionRepository claudeQuestionRepository;

    @Autowired
    private MockQuestionRepository mockQuestionRepository;

    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        // Create test questions
        testQuestions = Arrays.asList(
            new Question(
                "Test question 1?",
                Arrays.asList(
                    new Choice("Option 1", false),
                    new Choice("Option 2", true),
                    new Choice("Option 3", false),
                    new Choice("Option 4", false)
                ),
                "Test explanation 1"
            ),
            new Question(
                "Test question 2?",
                Arrays.asList(
                    new Choice("Option A", false),
                    new Choice("Option B", false),
                    new Choice("Option C", true),
                    new Choice("Option D", false)
                ),
                "Test explanation 2"
            )
        );
        
        // Reset the repository's cached questions before each test
        // This is a bit of a hack since we don't have a public method to clear the cache
        try {
            java.lang.reflect.Field cachedQuestionsField = ClaudeQuestionRepository.class.getDeclaredField("cachedQuestions");
            cachedQuestionsField.setAccessible(true);
            cachedQuestionsField.set(claudeQuestionRepository, null);
        } catch (Exception e) {
            // Just log the error but continue with the test
            System.err.println("Failed to reset cached questions: " + e.getMessage());
        }
    }

    @Test
    void testGetAllQuestions_Success() {
        // Arrange
        when(claudeService.generateQuestions(anyInt())).thenReturn(testQuestions);

        // Act
        List<Question> results = claudeQuestionRepository.getAllQuestions();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Test question 1?", results.get(0).getText());
        assertEquals("Test question 2?", results.get(1).getText());
        verify(claudeService, times(1)).generateQuestions(anyInt());
    }

    @Test
    void testGetAllQuestions_FallbackToMock() {
        // Arrange
        when(claudeService.generateQuestions(anyInt())).thenThrow(new RuntimeException("API error"));

        // Act
        List<Question> results = claudeQuestionRepository.getAllQuestions();

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        // The results should be from mockQuestionRepository
        assertEquals(mockQuestionRepository.getAllQuestions().size(), results.size());
        verify(claudeService, times(1)).generateQuestions(anyInt());
    }

    @Test
    void testRefreshQuestions_Success() {
        // Arrange
        when(claudeService.generateQuestions(5)).thenReturn(testQuestions);

        // Act
        claudeQuestionRepository.refreshQuestions(5);
        List<Question> results = claudeQuestionRepository.getAllQuestions();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Test question 1?", results.get(0).getText());
        verify(claudeService, times(1)).generateQuestions(5);
    }

    @Test
    void testRefreshQuestions_Error() {
        // Arrange
        when(claudeService.generateQuestions(5)).thenThrow(new RuntimeException("API error"));

        // Act
        claudeQuestionRepository.refreshQuestions(5);
        List<Question> results = claudeQuestionRepository.getAllQuestions();

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        // Since the refresh failed, it should fall back to mock questions
        assertEquals(mockQuestionRepository.getAllQuestions().size(), results.size());
        verify(claudeService, times(2)).generateQuestions(anyInt()); // Once for refresh, once for getAllQuestions
    }

    @Test
    void testCachedQuestions() {
        // Arrange
        when(claudeService.generateQuestions(anyInt())).thenReturn(testQuestions);

        // Act - first call should hit the service
        List<Question> firstResults = claudeQuestionRepository.getAllQuestions();
        
        // Reset the mock to verify no more calls
        reset(claudeService);
        
        // Act - second call should use cache
        List<Question> secondResults = claudeQuestionRepository.getAllQuestions();

        // Assert
        assertEquals(2, firstResults.size());
        assertEquals(2, secondResults.size());
        // Verify the questions are the same (not necessarily the same objects but same content)
        assertEquals(firstResults.get(0).getText(), secondResults.get(0).getText());
        assertEquals(firstResults.get(1).getText(), secondResults.get(1).getText());
        
        // Verify the service wasn't called again on second request
        verify(claudeService, never()).generateQuestions(anyInt());
    }
}
