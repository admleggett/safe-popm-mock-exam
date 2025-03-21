# POPM Exam CLI Application

<p align="center">
  <pre>
 ____   ___  ____  __  __            _    
|  _ \ / _ \|  _ \|  \/  | ___   ___| | __
| |_) | | | | |_) | |\/| |/ _ \ / __| |/ /
|  __/| |_| |  __/| |  | | (_) | (__|   < 
|_|    \___/|_|   |_|  |_|\___/ \___|_|\_\
                                                        
       SAFe POPM Exam CLI - Powered by Claude AI
  </pre>
</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen)](https://spring.io/projects/spring-boot)

A command-line application for practicing Scaled Agile Framework® (SAFe) Product Owner / Product Manager (POPM) certification questions with AI-powered question generation.

## Features

- Interactive CLI environment for exam practice
- AI-powered questions generated by Claude AI
- Multiple-choice question format matching the real POPM exam
- Detailed explanations for all answers
- Progress tracking and scoring
- Question caching to reduce API calls
- Fallback to pre-defined questions when offline

## Prerequisites

- Java 17 or higher
- Maven
- Anthropic API key (for Claude AI access)

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/safe-popm-exam.git
cd safe-popm-exam
```

2. Set up your Anthropic API key:
```bash
export ANTHROPIC_API_KEY=your_api_key_here
```

3. Build the application:
```bash
./mvnw clean package
```

## Running the Application

Start the application using Java:

```bash
java -jar safe-popm-exam/target/safe-popm-exam-0.0.1-SNAPSHOT.jar
```

## Usage

Once started, you'll see the POPM Exam CLI prompt. Here are the available commands:

| Command | Description |
|---------|-------------|
| `start-exam [num]` | Start a new exam with [num] questions (default: 5) |
| `answer [num]` | Submit your answer (the option number) |
| `current-question` | Display the current question again |
| `end-exam` | End the current exam and see your score |
| `refresh-questions [num]` | Generate new AI-powered questions (default: 10) |
| `exam-help` | Display help information for the POPM exam |
| `exit` | Exit the application |

### Administrative Commands

| Command | Description |
|---------|-------------|
| `debug-claude [true/false]` | Enable/disable debug logging for Claude service |
| `clear-cache` | Clear the question cache |
| `debug-request [num]` | Make a debug API request for [num] questions |

## Example Session

```
popm-exam:>refresh-questions 5
Successfully generated 5 new questions using Claude AI.

popm-exam:>start-exam
Starting new POPM mock exam with 5 questions.

Question 1 of 5:

What is SAFe's primary approach to Lean-Agile adoption?

1) Bottom-up implementation across teams
2) Top-down implementation starting with leadership training
3) Middle-out implementation focusing on program managers
4) Implementation through external consultants only

Enter 'answer [number]' to submit your answer.

popm-exam:>answer 2
Correct! The correct answer is: Top-down implementation starting with leadership training

Explanation: SAFe advocates a top-down implementation approach, starting with training leaders, as this accelerates organizational change.

Question 2 of 5:
...
```

## Architecture

- Spring Boot application with Spring Shell for CLI functionality
- Spring AI integration for Claude API access
- In-memory caching of questions
- Batch processing for question generation
- Fallback mechanisms for offline usage

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

SAFe® is a registered trademark of Scaled Agile, Inc. This project is not affiliated with or endorsed by Scaled Agile, Inc.
