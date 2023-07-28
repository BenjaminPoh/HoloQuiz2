package benloti.holoquiz.structs;

import java.util.List;

public class Question {
    private final String question;
    private final List<String> defaultAnswers;
    private final String extraMessage;
    private final List<String> secretAnswers;
    private final String secretMessage;

    public Question(String question, List<String> answers, String message,
                    List<String> secretAnswers, String secretMessage) {
        this.question = question;
        this.defaultAnswers = answers;
        this.extraMessage = message;
        this.secretAnswers = secretAnswers;
        this.secretMessage = secretMessage;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return defaultAnswers;
    }

    public String getExtraMessage() {
        return extraMessage;
    }

    public List<String> getSecretAnswers() {
        return secretAnswers;
    }

    public String getSecretMessage() {
        return secretMessage;
    }

}