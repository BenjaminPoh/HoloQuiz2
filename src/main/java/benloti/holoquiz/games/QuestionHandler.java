package benloti.holoquiz.games;

import benloti.holoquiz.structs.Question;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionHandler {
    private final ArrayList<Question> questionList;

    public QuestionHandler(ConfigurationSection config) {
        this.questionList = new ArrayList<>();
        for (String key : config.getKeys(false)) {
            ConfigurationSection configSection = config.getConfigurationSection(key);
            questionCategoryLoader(configSection);
        }
    }

    public Question getRandomQuestion() {
        int size = questionList.size();
        Random rand = new Random();
        int randomIndex = rand.nextInt(size);
        return questionList.get(randomIndex);
    }

    public boolean secretMessageFound(String answer, Question question) {
        for(String peko : question.getSecretAnswers()) {
            if(peko.equals(answer)) {
                return true;
            }
        }
        return false;
    }

    private void questionCategoryLoader(ConfigurationSection config) {
        String questionColourCode = nullReplacer(config.getString("QuestionColour"));
        String messageColourCode = nullReplacer(config.getString("MessageColour"));
        String categoryLabel = nullReplacer(config.getString("CategoryLabel"));
        String categoryPrefix = categoryLabel + questionColourCode;
        ConfigurationSection questionListSection = config.getConfigurationSection("QuestionList");
        questionListLoader(questionListSection, categoryPrefix, messageColourCode);
    }

    private void questionListLoader(ConfigurationSection config, String prefix, String msgColorCode) {
        for (String key : config.getKeys(false)) {
            ConfigurationSection questionConfig = config.getConfigurationSection(key);
            String question = questionConfig.getString("question");
            List<String> answers = questionConfig.getStringList("answer");
            if(question == null || answers.size() == 0) {
                Bukkit.getLogger().info("[HoloQuiz] Error with loading question: " + question);
                continue;
            }
            question = prefix + question;
            String message = nullReplacer(questionConfig.getString("message"));
            message = msgColorCode + message;
            List<String> secretAnswer = questionConfig.getStringList("secretAnswer");
            String secretMessage = questionConfig.getString("secretMessage");
            secretMessage = msgColorCode + secretMessage;
            Question newQuestion = new Question(question, answers, message, secretAnswer, secretMessage);

            questionList.add(newQuestion);
        }
    }

    private String nullReplacer(String x) {
        if(x == null) {
            return "";
        }
        return x;
    }

}
