package benloti.holoquiz.games;

import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.structs.MathOPNode;
import benloti.holoquiz.structs.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathQuestionGenerator {
    public static final String ROUND_1DP_INST = "Round off to 1 decimal place: ";
    public static final String ROUND_2DP_INST = "Round off to 2 decimal places: ";
    public static final String ROUND_3DP_INST = "Round off to 3 decimal places: ";
    private final int mathNumberLimit;
    private final boolean useNormalDistribution;
    private final boolean mathDivisorLimit;
    private final int mathOperationLimit;
    private final boolean mathChaosMode;
    private final String[] operationMap = {"+", "-", "*", "/"};

    public MathQuestionGenerator(ConfigFile configFile) {
        this.mathNumberLimit = configFile.getMathDifficulty();
        String mathDistribution = configFile.getMathDistribution();
        if (mathDistribution.equals("Normal")) {
            this.useNormalDistribution = true;
        } else if (mathDistribution.equals("Uniform")) {
            this.useNormalDistribution = false;
        } else {
            this.useNormalDistribution = (mathNumberLimit <= 10);
        }
        this.mathDivisorLimit = configFile.isMathDivisorLimit();
        this.mathOperationLimit = configFile.getMathOperationLimit();
        this.mathChaosMode = configFile.isMathChaosMode();
    }

    public String getMathQuestion() {
        Random randFunc = new Random();
        int operationsUsed = Math.min(mathOperationLimit, normalRNG(randFunc, mathOperationLimit, 1));
        boolean divisionUsed = false;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateNumberForQuestion(randFunc));

        while (operationsUsed != 0) {
            int operationType = uniformRNG(randFunc, 4);
            if (divisionUsed && !mathChaosMode) {
                operationType = uniformRNG(randFunc, 3);
            }
            if (operationType == 3) {
                divisionUsed = true;
            }

            int number = generateNumberForQuestion(randFunc);
            if (operationType == 3 && number > 10 && !mathChaosMode && mathDivisorLimit) {
                number = uniformRNG(randFunc, 10) + 1; // a number from 1 to 10
            }

            stringBuilder.append(operationMap[operationType]);
            stringBuilder.append(number);
            operationsUsed -= 1;
        }
        stringBuilder.append("= ?");
        return stringBuilder.toString();
    }

    //Y'all really testing my coding skills, isn't it?
    public double solver(String question) {
        //String regexString = "(([(])([^()]+)([)]))";
        Stack<MathOPNode> charStack = new Stack<>();
        ArrayList<MathOPNode> postFixQueue = new ArrayList<>();

        String regexString = "([0-9]+|[)(+*/-])";
        Pattern regexPattern = Pattern.compile(regexString);
        Matcher regexMatcher = regexPattern.matcher(question);
        while (regexMatcher.find()) {
            String matchedGroup = regexMatcher.group(0);
            if (matchedGroup.equals("(")) {
                charStack.push(new MathOPNode(matchedGroup, true));
            } else if (matchedGroup.equals(")")) {
                while (!charStack.lastElement().getValue().equals("(")) {
                    postFixQueue.add(charStack.lastElement());
                    charStack.pop();
                }
            } else if ("+-*/".contains(matchedGroup)) {
                int currPriority = operationPriority(matchedGroup);
                while(!charStack.empty() && currPriority <= operationPriority(charStack.lastElement().getValue())) {
                    postFixQueue.add(charStack.lastElement());
                    charStack.pop();
                }
                charStack.push(new MathOPNode(matchedGroup, true));
            } else {
                postFixQueue.add(new MathOPNode(matchedGroup, false));
            }
        }
        while(!charStack.empty()) {
            postFixQueue.add(charStack.lastElement());
            charStack.pop();
        }

        Stack<Double> numStack = new Stack<>();
        for(MathOPNode node : postFixQueue) {
            if(!node.isOperation()) {
                numStack.push(Double.parseDouble(node.getValue()));
            } else {
                Double second = numStack.pop();
                Double first = numStack.pop();
                numStack.push(executeOperation(first, second, node.getValue()));
            }
        }
        return  numStack.pop();
    }

    public Question parser(String question, double answer) {
        List<String> finalAnswer = new ArrayList<>();
        long temp = Math.round(answer * 100);

        if(temp % 100 == 0) {
            long tempAnswer = temp/100;
            finalAnswer.add(String.valueOf(tempAnswer));
            return new Question(question, finalAnswer, null  , null, null);
        }

        double tempAnswer = temp/ 100.0;
        finalAnswer.add(String.valueOf(tempAnswer));
        if(temp % 10 == 0) {
            question = ROUND_1DP_INST + question;
            return new Question(question, finalAnswer, null  , null, null);
        }
        if(!mathChaosMode) {
            question = ROUND_2DP_INST + question;
            return new Question(question, finalAnswer, null  , null, null);
        }

        //Chaos Mode can ask for 3dp.
        List<String> finalAnswerChaos = new ArrayList<>();
        temp = Math.round(answer * 1000);
        double tempAnswerChaos = temp/ 1000.0;
        finalAnswerChaos.add(String.valueOf(tempAnswerChaos));
        if(temp % 100 == 0) {
            question = ROUND_2DP_INST + question;
            return new Question(question, finalAnswerChaos, null  , null, null);
        }
        question = ROUND_3DP_INST + question;
        return new Question(question, finalAnswerChaos, null  , null, null);
    }

    private int generateNumberForQuestion(Random randomFunc) {
        if (useNormalDistribution) {
            return normalRNG(randomFunc, mathNumberLimit, mathNumberLimit / 2.0);
        }
        return uniformRNG(randomFunc, mathNumberLimit);
    }

    private int normalRNG(Random randomFunc, int mean, double stdDev) {
        double rawDouble = randomFunc.nextGaussian() * stdDev + mean;
        long answer = Math.round(rawDouble);
        return (int) Math.abs(answer);
    }

    private int uniformRNG(Random randomFunc, int maxLimit) {
        return randomFunc.nextInt(maxLimit);
    }

    private boolean flipCoinRNG(Random randomFunc) {
        return (randomFunc.nextInt(2) == 0);
    }

    private int operationPriority(String value) {
        if (value.equals("(") || value.equals(")")) {
            return -1;
        }
        if (value.equals("*") || value.equals("/")) {
            return 2;
        }
        return 1;
    }

    private double executeOperation(double first, double second, String operator) {
        if(operator.equals("+")) {
            return first + second;
        }
        if(operator.equals("-")) {
            return first - second;
        }
        if(operator.equals("*")) {
            return first * second;
        }
        return first / second;
    }
}
