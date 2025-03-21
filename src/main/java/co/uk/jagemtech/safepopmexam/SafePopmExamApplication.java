package co.uk.jagemtech.safepopmexam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SafePopmExamApplication {

    public static void main(String[] args) {
        // Add shell:>interactive false explicitly to run non-interactively
        SpringApplication.run(SafePopmExamApplication.class, args);
    }
}
