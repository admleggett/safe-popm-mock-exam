package co.uk.jagemtech.safepopmexam.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;

@TestConfiguration
public class TestShellConfig {

    @Bean
    @Primary
    public ResultHandler resultHandler() {
        return (result) -> {
            // Do nothing with the result in tests
        };
    }
    
    @Bean
    @Primary
    public Shell shell() {
        // Return a mock shell for tests
        return org.mockito.Mockito.mock(Shell.class);
    }
}
