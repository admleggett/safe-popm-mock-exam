package co.uk.jagemtech.safepopmexam.repository;

import co.uk.jagemtech.safepopmexam.model.Question;
import co.uk.jagemtech.safepopmexam.service.ClaudeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Primary
public class ClaudeQuestionRepository implements QuestionRepository {
    private static final Logger logger = LoggerFactory.getLogger(ClaudeQuestionRepository.class);
    private static final int BATCH_SIZE = 5;

    private final ClaudeService claudeService;
    private final MockQuestionRepository mockRepository;
    private List<Question> cachedQuestions = null;
    
    @Autowired
    public ClaudeQuestionRepository(ClaudeService claudeService, MockQuestionRepository mockRepository) {
        this.claudeService = claudeService;
        this.mockRepository = mockRepository;
    }

    @Override
    public List<Question> getAllQuestions() {
        // Use cached questions if available
        if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
            logger.debug("Returning {} cached questions", cachedQuestions.size());
            return new ArrayList<>(cachedQuestions);
        }
        
        try {
            // Try to generate questions with Claude
            logger.info("No cached questions available. Generating questions with Claude...");
            List<Question> generatedQuestions = claudeService.generateQuestions(BATCH_SIZE);
            
            // Cache the generated questions
            if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
                logger.info("Successfully generated {} questions with Claude", generatedQuestions.size());
                cachedQuestions = new ArrayList<>(generatedQuestions);
                return generatedQuestions;
            } else {
                logger.warn("Claude returned empty question list");
            }
        } catch (Exception e) {
            logger.error("Error generating questions with Claude", e);
        }
        
        // Fallback to mock questions if Claude fails
        logger.info("Falling back to mock questions");
        return mockRepository.getAllQuestions();
    }
    
    public void refreshQuestions(int requestedCount) {
        try {
            logger.info("Refreshing questions - requesting {} new questions from Claude", requestedCount);
            List<Question> allQuestions = new ArrayList<>();
            
            // Get questions in batches to avoid token limitations
            int batchesNeeded = (int) Math.ceil((double) requestedCount / BATCH_SIZE);
            logger.info("Fetching questions in {} batches of up to {} questions each", batchesNeeded, BATCH_SIZE);
            
            // Generate questions in batches
            for (int i = 0; i < batchesNeeded && allQuestions.size() < requestedCount; i++) {
                int remainingCount = requestedCount - allQuestions.size();
                int batchCount = Math.min(remainingCount, BATCH_SIZE);
                
                logger.info("Generating batch {} of {}: {} questions", i+1, batchesNeeded, batchCount);
                List<Question> batchQuestions = claudeService.generateQuestions(batchCount);
                
                if (batchQuestions != null && !batchQuestions.isEmpty()) {
                    allQuestions.addAll(batchQuestions);
                    logger.info("Batch {} complete, now have {} total questions", i+1, allQuestions.size());
                } else {
                    logger.warn("Batch {} returned no questions, stopping", i+1);
                    break;
                }
                
                // Small delay between batches to avoid rate limiting
                if (i < batchesNeeded - 1) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            if (!allQuestions.isEmpty()) {
                logger.info("Successfully refreshed questions. Cached {} new questions", allQuestions.size());
                cachedQuestions = new ArrayList<>(allQuestions);
            } else {
                logger.warn("Failed to refresh questions - Claude returned empty results for all batches");
            }
        } catch (Exception e) {
            logger.error("Failed to refresh questions", e);
        }
    }
    
    public void clearCache() {
        logger.info("Clearing question cache");
        cachedQuestions = null;
    }
}
