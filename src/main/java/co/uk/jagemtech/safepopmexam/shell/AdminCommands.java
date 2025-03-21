package co.uk.jagemtech.safepopmexam.shell;

import co.uk.jagemtech.safepopmexam.repository.ClaudeQuestionRepository;
import co.uk.jagemtech.safepopmexam.util.ProgressIndicator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.concurrent.CompletableFuture;

@ShellComponent
public class AdminCommands {
    
    private final ClaudeQuestionRepository questionRepository;
    private final ProgressIndicator progressIndicator;
    
    @Autowired
    public AdminCommands(ClaudeQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
        this.progressIndicator = new ProgressIndicator(System.out);
    }
    
    @ShellMethod(key = "debug-claude", value = "Enable or disable debug logging for Claude service")
    public String toggleClaudeDebug(@ShellOption(defaultValue = "true") boolean enable) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Set debug for ClaudeService
        Logger claudeLogger = loggerContext.getLogger("co.uk.jagemtech.safepopmexam.service.ClaudeService");
        claudeLogger.setLevel(enable ? Level.DEBUG : Level.INFO);
        
        // Set debug for ClaudeQuestionRepository
        Logger repoLogger = loggerContext.getLogger("co.uk.jagemtech.safepopmexam.repository.ClaudeQuestionRepository");
        repoLogger.setLevel(enable ? Level.DEBUG : Level.INFO);
        
        return "Claude debug logging " + (enable ? "enabled" : "disabled");
    }
    
    @ShellMethod(key = "clear-cache", value = "Clear the question cache")
    public String clearCache() {
        questionRepository.clearCache();
        return "Question cache cleared. Next request will generate new questions.";
    }
    
    @ShellMethod(key = "debug-request", value = "Make a debug request to Claude API and show full response")
    public String debugClaudeRequest(@ShellOption(defaultValue = "1") int count) {
        toggleClaudeDebug(true);
        questionRepository.clearCache();
        
        progressIndicator.start("Making debug request to Claude API...");
        
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionRepository.refreshQuestions(count);
            });
            
            future.get();
            progressIndicator.stop();
            
            toggleClaudeDebug(false);
            return "Debug request completed. Check logs for details.";
        } catch (Exception e) {
            progressIndicator.stop();
            toggleClaudeDebug(false);
            return "Debug request failed: " + e.getMessage();
        }
    }
}
