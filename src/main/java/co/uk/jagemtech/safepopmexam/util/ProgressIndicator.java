package co.uk.jagemtech.safepopmexam.util;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for showing progress indicators in the CLI
 */
public class ProgressIndicator {
    private static final String[] SPINNER_FRAMES = {"|", "/", "-", "\\"};
    private Thread spinnerThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final PrintStream out;
    private String message;
    
    public ProgressIndicator(PrintStream out) {
        this.out = out;
    }
    
    /**
     * Start showing a progress spinner with the given message
     */
    public void start(String message) {
        this.message = message;
        if (running.compareAndSet(false, true)) {
            spinnerThread = new Thread(this::showSpinner);
            spinnerThread.setDaemon(true);
            spinnerThread.start();
        }
    }
    
    /**
     * Stop showing the progress spinner
     */
    public void stop() {
        running.set(false);
        if (spinnerThread != null) {
            try {
                spinnerThread.join(500); // Wait for spinner to stop
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Clear the spinner line
            out.print("\r" + " ".repeat(message.length() + 10) + "\r");
            out.flush();
        }
    }
    
    private void showSpinner() {
        int i = 0;
        try {
            while (running.get()) {
                String spinner = SPINNER_FRAMES[i % SPINNER_FRAMES.length];
                out.print("\r" + message + " " + spinner);
                out.flush();
                Thread.sleep(100);
                i++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
