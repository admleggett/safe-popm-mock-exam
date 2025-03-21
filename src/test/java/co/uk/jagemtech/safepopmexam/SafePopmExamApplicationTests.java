package co.uk.jagemtech.safepopmexam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.shell.interactive.enabled=false",
    "spring.shell.command.script.enabled=false"
})
class SafePopmExamApplicationTests {

    @Test
    void contextLoads() {
        // Just testing that the context loads successfully
    }
}
