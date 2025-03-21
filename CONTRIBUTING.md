# Contributing to POPM Exam CLI

Thank you for considering contributing to the POPM Exam CLI project! This document outlines the process for contributing to the project.

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## How Can I Contribute?

### Reporting Bugs

- Ensure the bug hasn't already been reported by searching GitHub Issues
- If you can't find an existing issue, create a new one
- Include detailed steps to reproduce the bug
- Include any relevant logs or screenshots

### Suggesting Enhancements

- Clearly describe the enhancement
- Provide a step-by-step description of the suggested enhancement
- Explain why this enhancement would be useful

### Pull Requests

1. Fork the repository
2. Create a new branch for your feature (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add or update tests as necessary
5. Run the tests (`mvn test`)
6. Commit your changes (`git commit -m 'Add some amazing feature'`)
7. Push to your branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

## Development Setup

1. Clone the repository
2. Install Java 17 or higher
3. Install Maven
4. Build the project: `mvn clean install`

## Project Structure

- `src/main/java` - Java source code
  - `.../model` - Domain model classes
  - `.../repository` - Data access layer
  - `.../service` - Business logic
  - `.../shell` - CLI commands
  - `.../config` - Configuration classes
- `src/main/resources` - Resources (application.properties, banner, etc.)
- `src/test` - Test code

## Coding Guidelines

- Follow standard Java coding conventions
- Use descriptive variable and method names
- Write clear comments and Javadoc
- Keep methods small and focused
- Write unit tests for new code
- Maintain backward compatibility unless discussed

## Running Tests

```bash
# Run unit tests
mvn test

# Run integration tests
mvn test -Pintegration-tests
```

## Documentation

- Update the README.md with details of changes to the interface
- Update any relevant documentation
- Add Javadoc comments to public methods

## Questions?

If you have any questions, please feel free to create an issue or contact the project maintainers.
