package benloti.holoquiz2.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private String question;
    private String answers; //TODO: Make it a list
    //private String message; TODO: Custom reply message if it exists

    public Question(String question, String answers) {
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswers() {
        return answers;
    }

    public static List<Question> loadFromConfig(ConfigurationSection config) {
        List<Question> questions = new ArrayList<>();

        for (String key: config.getKeys(false)) {
            ConfigurationSection questionSection = config.getConfigurationSection(key);
            String question = questionSection.getString("question");
            String answers = questionSection.getString("answer");
            questions.add(new Question(question,answers));
        }

        return questions;
    }

}