package benloti.holoquiz2.structs;

import java.util.List;

public class Question {
    private final String question;
    private final List<String> answers;
    //private String message; TODO: Custom reply message if it exists

    public Question(String question, List<String> answers) {
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }
}