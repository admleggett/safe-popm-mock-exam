package co.uk.jagemtech.safepopmexam.service;

import co.uk.jagemtech.safepopmexam.model.Choice;
import co.uk.jagemtech.safepopmexam.model.Question;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ClaudeService {
    private static final Logger logger = LoggerFactory.getLogger(ClaudeService.class);

    private final AnthropicChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Autowired
    public ClaudeService(AnthropicChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        this.objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public List<Question> generateQuestions(int numberOfQuestions) {
        logger.debug("Generating {} questions with Claude AI", numberOfQuestions);
        String prompt = buildPrompt(numberOfQuestions);
        logger.debug("Prompt sent to Claude: {}", prompt);
        
        try {
            ChatResponse response = this.chatModel.call(new Prompt(prompt));
            logger.debug("Received response from Claude");
            
            List<Generation> generations = response.getResults();
            if (generations == null || generations.isEmpty()) {
                logger.error("Claude returned no generations in response");
                return Arrays.asList(createErrorQuestion());
            }

            // Extract the first generation
            Generation generation = generations.get(0);
            String content = generation.getOutput().getText();
            logger.debug("Claude response content: {}", content);

            return parseQuestionsFromResponse(content, numberOfQuestions);
        } catch (Exception e) {
            logger.error("Error calling Claude API", e);
            return Arrays.asList(createErrorQuestion("Error calling Claude API: " + e.getMessage()));
        }
    }
    
    private String buildPrompt(int numberOfQuestions) {
        // Limit to 5 questions per request to avoid token limitations
        int batchSize = Math.min(numberOfQuestions, 5);
        
        return """
            Generate exactly %d multiple-choice questions for the SAFe POPM (SAFe Product Owner / Product Manager) certification exam.

            Requirements:
            1. Each question must be concise and clear
            2. Each question must have exactly 4 answer choices
            3. Only one answer choice should be marked as correct
            4. Keep explanations brief (max 100 characters)
            5. Focus on core POPM concepts:
                5.1. Product Owner/Product Management Roles and Responsibilities 
                    5.1.1. SAFe for Product Owner/Product Management
                    5.1.2. The Lean-Agile mindset 
                    5.1.3. Value Streams
                    5.1.4. Product Owner/Product Management Responsibilities
                5.2. PI Planning Preparation
                    5.2.1. PI Planning
                    5.2.2. The Solution Vision
                    5.2.3. Solution and PI Roadmaps
                    5.2.4. Customer Centricity
                    5.2.5. ART Backlog abd Kanban
                5.3. Leadership for PI Planning 
                    5.3.1. The Vision and PI Planning 
                    5.3.2. PI Objectives 
                    5.3.3. ART Planning Boad and Dependencies
                    5.3.4. Risks and the End of PI Planning
                5.4. Iteration Execution
                    5.4.1. Iteration Planning
                    5.4.2. Stories and Story Maps
                    5.4.3. The Team Kanban
                    5.4.4. Backlog Refinement
                    5.4.5. Iteration Review and Retrospective
                    5.4.6. DevOps and Release on Demand
                5.5. PI Execution
                    5.5.1. PO Sync
                    5.5.2. Inspect and Adapt
                    5.5.3. The Innovation and Planning Iteration
                    5.5.4. The System Demo

            Format as a JSON array with the following structure:
            [
              {
                "text": "Brief question text",
                "choices": [
                  {"text": "First option", "correct": false},
                  {"text": "Second option", "correct": true},
                  {"text": "Third option", "correct": false},
                  {"text": "Fourth option", "correct": false}
                ],
                "explanation": "Short explanation"
              }
            ]

            IMPORTANT: You must provide exactly %d questions. Return ONLY the JSON array.
            """.formatted(batchSize, batchSize);
    }
    
    private List<Question> parseQuestionsFromResponse(String content, int expectedCount) {
        List<Question> questions = new ArrayList<>();
        
        try {
            // First, try to clean up the JSON
            String jsonContent = sanitizeJson(extractJsonFromResponse(content));
            logger.debug("Sanitized JSON: {}", jsonContent);
            
            // Try stream-parsing individual questions to handle partial/corrupt JSON
            questions = parseQuestionsStreaming(jsonContent);
            logger.info("Parsed {} questions using streaming parser", questions.size());
            
            // If streaming parser didn't work, fall back to standard parsing
            if (questions.isEmpty()) {
                try {
                    List<QuestionDTO> questionDTOs = objectMapper.readValue(jsonContent, 
                            new TypeReference<List<QuestionDTO>>() {});
                    for (QuestionDTO dto : questionDTOs) {
                        questions.add(convertDtoToQuestion(dto));
                    }
                    logger.info("Parsed {} questions using standard parser", questions.size());
                } catch (Exception e) {
                    logger.error("Failed to parse with standard parser: {}", e.getMessage());
                }
            }
            
            // Log warning if we didn't get the expected number of questions
            if (!questions.isEmpty() && questions.size() < expectedCount) {
                logger.warn("Expected {} questions but parsed only {}. This may be due to token limitations or parsing issues.", 
                    expectedCount, questions.size());
            }
        } catch (Exception e) {
            logger.error("Failed to parse Claude response: {}", e.getMessage());
            questions.add(createErrorQuestion("Failed to parse response: " + e.getMessage()));
        }
        
        // Ensure we return something even if parsing fails
        if (questions.isEmpty()) {
            logger.warn("No questions could be parsed, returning error question");
            questions.add(createErrorQuestion());
        }
        
        return questions;
    }
    
    private List<Question> parseQuestionsStreaming(String json) {
        List<Question> questions = new ArrayList<>();
        
        // Extract individual question objects from the array
        Pattern pattern = Pattern.compile("\\{\\s*\"text\".*?\"explanation\".*?\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        
        while (matcher.find()) {
            String questionJson = matcher.group();
            try {
                QuestionDTO dto = objectMapper.readValue(questionJson, QuestionDTO.class);
                questions.add(convertDtoToQuestion(dto));
            } catch (Exception e) {
                logger.debug("Failed to parse individual question: {}", e.getMessage());
            }
        }
        
        return questions;
    }
    
    private Question convertDtoToQuestion(QuestionDTO dto) {
        List<Choice> choices = new ArrayList<>();
        if (dto.getChoices() != null) {
            for (ChoiceDTO choiceDto : dto.getChoices()) {
                choices.add(new Choice(choiceDto.getText(), choiceDto.isCorrect()));
            }
        }
        return new Question(dto.getText(), choices, dto.getExplanation());
    }
    
    private String sanitizeJson(String json) {
        if (json == null || json.isEmpty()) {
            return "[]";
        }
        
        // Remove any trailing commas before closing brackets or braces
        json = json.replaceAll(",\\s*\\}", "}");
        json = json.replaceAll(",\\s*\\]", "]");
        
        // Ensure all string values are properly quoted
        json = fixMissingQuotes(json);
        
        // Check if JSON starts with [ and ends with ]
        if (!json.trim().startsWith("[")) {
            json = "[" + json;
        }
        if (!json.trim().endsWith("]")) {
            json = json + "]";
        }
        
        return json;
    }
    
    private String fixMissingQuotes(String json) {
        // This is a simple approach - a more robust solution might use a JSON parser
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escaped) {
                result.append(c);
                escaped = false;
            } else if (c == '\\') {
                result.append(c);
                escaped = true;
            } else if (c == '"') {
                result.append(c);
                inString = !inString;
            } else if (c == '\n' || c == '\r') {
                if (inString) {
                    // Replace newlines in strings with spaces
                    result.append(' ');
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        
        // If string is still open at the end, close it
        if (inString) {
            result.append('"');
        }
        
        return result.toString();
    }
    
    private String extractJsonFromResponse(String content) {
        // Look for content between triple backticks
        if (content.contains("```json")) {
            int start = content.indexOf("```json") + 7;
            int end = content.indexOf("```", start);
            if (end > start) {
                logger.debug("Found JSON within code block");
                return content.substring(start, end).trim();
            }
        }
        
        // Look for JSON array directly
        if (content.trim().startsWith("[") && content.trim().endsWith("]")) {
            logger.debug("Found raw JSON array");
            return content.trim();
        }
        
        // Look for any JSON-like structure
        Pattern jsonPattern = Pattern.compile("\\[\\s*\\{.*\\}\\s*\\]", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(content);
        if (matcher.find()) {
            logger.debug("Found JSON-like structure using regex");
            return matcher.group();
        }
        
        logger.debug("Could not identify JSON format, returning raw content");
        return content;
    }
    
    private Question createErrorQuestion() {
        return createErrorQuestion("There was an error in question generation. Please contact the administrator.");
    }
    
    private Question createErrorQuestion(String explanation) {
        List<Choice> choices = Arrays.asList(
            new Choice("Contact administrator", true),
            new Choice("Try again later", false),
            new Choice("Restart the application", false),
            new Choice("Use mock questions instead", false)
        );
        
        return new Question(
            "Unable to generate questions. What should you do?",
            choices,
            explanation
        );
    }
    
    // DTOs for JSON parsing
    private static class QuestionDTO {
        private String text;
        private List<ChoiceDTO> choices;
        private String explanation;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public List<ChoiceDTO> getChoices() { return choices; }
        public void setChoices(List<ChoiceDTO> choices) { this.choices = choices; }
        
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
    
    private static class ChoiceDTO {
        private String text;
        private boolean correct;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }
}
