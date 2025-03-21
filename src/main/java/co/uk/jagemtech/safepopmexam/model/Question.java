package co.uk.jagemtech.safepopmexam.model;

import java.util.List;

public class Question {
    private String text;
    private List<Choice> choices;
    private String explanation;

    public Question(String text, List<Choice> choices, String explanation) {
        this.text = text;
        this.choices = choices;
        this.explanation = explanation;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Choice getCorrectChoice() {
        return choices.stream()
                .filter(Choice::isCorrect)
                .findFirst()
                .orElse(null);
    }
}
