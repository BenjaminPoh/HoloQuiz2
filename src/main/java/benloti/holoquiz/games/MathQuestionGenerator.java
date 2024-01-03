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
    public static final String ROUND_2DP_INST = "Round off to 2 decimal places: ";
    public static final String ROUND_3DP_INST = "Round off to 3 decimal places: ";


    private final int mathNumberLimit;
    private final boolean useNormalDistribution;
    private final boolean mathDivisorLimit;
    private final int mathOperationLimit;
    private final boolean mathChaosMode;
    private final String questionColour;
    private final String[] operationMap;

    public MathQuestionGenerator(ConfigFile configFile) {
        this.mathNumberLimit = configFile.getMathRange();
        String mathDistribution = configFile.getMathDistribution();
        if (mathDistribution.equals("Normal")) {
            this.useNormalDistribution = true;
        } else if (mathDistribution.equals("Uniform")) {
            this.useNormalDistribution = false;
        } else {
            this.useNormalDistribution = (mathNumberLimit <= 10);
        }
        String difficulty = configFile.getMathDifficulty();
        this.mathChaosMode = configFile.isMathChaosMode();
        this.mathDivisorLimit = configFile.isMathDivisorLimit();
        this.mathOperationLimit = configFile.getMathOperationLimit();
        if(difficulty.equals("Hard") || this.mathChaosMode) {
            this.operationMap = new String[]{"+", "-", "*", "/"};
        } else if (difficulty.equals("Easy") ) {
            this.operationMap = new String[]{"+", "-", "+", "-", "+", "-", "*", "/"};
        } else {
            this.operationMap = new String[]{"+", "-", "+", "-", "*", "/"};
        }

        this.questionColour = configFile.getMathQuestionColour();
    }

    public String getMathQuestionColour() {
        return this.questionColour;
    }

    public String getMathQuestion() {
        Random randFunc = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        Stack<Integer> parenthesesPosition = new Stack<>();

        int operationsUsed = Math.min(mathOperationLimit, normalRNG(randFunc, mathOperationLimit, 1));
        if(operationsUsed <1) {
            operationsUsed = 1;
        }

        if(operationsUsed > 1 && flipCoinRNG(randFunc)) {
            parenthesesPosition.push(0);
            stringBuilder.append('(');
        }
        stringBuilder.append(generateNumberForQuestion(randFunc));

        if(mathChaosMode) {
            generateChaoticQuestion(randFunc, stringBuilder, parenthesesPosition, operationsUsed);
        } else {
            generateNormalQuestion(randFunc, stringBuilder, parenthesesPosition, operationsUsed);
        }

        while(!parenthesesPosition.empty()) {
            parenthesesPosition.pop();
            stringBuilder.append(')');
        }

        stringBuilder.append(" = ?");
        return stringBuilder.toString();
    }

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
                while (!charStack.empty() && !charStack.peek().getValue().equals("(")) {
                    postFixQueue.add(charStack.peek());
                    charStack.pop();
                }
                charStack.pop();
            } else if ("+-*/".contains(matchedGroup)) {
                int currPriority = operationPriority(matchedGroup);
                while (!charStack.empty() && currPriority <= operationPriority(charStack.peek().getValue())) {
                    postFixQueue.add(charStack.lastElement());
                    charStack.pop();
                }
                charStack.push(new MathOPNode(matchedGroup, true));
            } else {
                postFixQueue.add(new MathOPNode(matchedGroup,false));
            }
        }
        while (!charStack.empty()) {
            postFixQueue.add(charStack.lastElement());
            charStack.pop();
        }

        Stack<Double> numStack = new Stack<>();
        for (MathOPNode node : postFixQueue) {
            if (!node.isOperation()) {
                numStack.push(Double.parseDouble(node.getValue()));
            } else {
                Double second = numStack.pop();
                Double first = numStack.pop();
                numStack.push(executeOperation(first, second, node.getValue()));
            }
        }
        return numStack.pop();
    }

    public Question parser(String questionColour, String question, double answer) {
        List<String> finalAnswer = new ArrayList<>();

        //Exact answer wanted if 3dp or less
        if(answer % 1 == 0) {
            finalAnswer.add(String.valueOf((long) answer));
            return new Question(questionColour + question, finalAnswer, null  , new ArrayList<>(), null);
        }
        if((answer * 1000) % 1 == 0) {
            finalAnswer.add(String.valueOf(answer));
            return new Question(questionColour + question, finalAnswer, null  , new ArrayList<>(), null);
        }

        boolean isNegative = answer < 0;
        double tempAnswer;
        if(mathChaosMode) {
            long temp = Math.round(Math.abs(answer) * 1000);
            tempAnswer = temp / 1000.0;
        } else {
            long temp = Math.round(Math.abs(answer) * 100);
            tempAnswer = temp / 100.0;
        }
        if(isNegative) {
            tempAnswer *= -1;
        }
        finalAnswer.add(String.valueOf(tempAnswer));

        if(mathChaosMode) {
            question = questionColour + ROUND_3DP_INST + question;
            return new Question(question, finalAnswer, null  , new ArrayList<>(), null);
        }
        question = questionColour + ROUND_2DP_INST + question;
        return new Question(question, finalAnswer, null  , new ArrayList<>(), null);
    }

    private void generateChaoticQuestion(Random randFunc, StringBuilder stringBuilder, Stack<Integer> parenthesesPosition, int operationsUsed) {
        int currentGroupPos = 1;
        while (operationsUsed >= currentGroupPos) {
            int operationType = uniformRNG(randFunc, operationMap.length);
            int number = generateNumberForQuestion(randFunc);
            if(number == 0 && operationType == operationMap.length - 1) {
                number = generateNumberForQuestion(randFunc) + 1;
            }

            if (flipCoinRNG(randFunc)) {
                stringBuilder.append(operationMap[operationType]);
            } else {
                stringBuilder.append(operationMap[operationType]);
                stringBuilder.append('(');
                parenthesesPosition.push(currentGroupPos);
            }

            if (!parenthesesPosition.empty() && flipCoinRNG(randFunc)) {
                stringBuilder.append(number);
                stringBuilder.append(')');
                parenthesesPosition.pop();
            } else {
                stringBuilder.append(number);
            }

            currentGroupPos += 1;
        }
    }

    private void generateNormalQuestion(Random randFunc, StringBuilder stringBuilder, Stack<Integer> parenthesesPosition, int operationsUsed) {
        int divisionLeft = 1;
        int currentGroupPos = 1;
        while (operationsUsed >= currentGroupPos) {
            boolean divisionUsed = false;
            int operationType = uniformRNG(randFunc,  operationMap.length);
            int number = generateNumberForQuestion(randFunc);
            if (divisionLeft == 0) {
                operationType = uniformRNG(randFunc,  operationMap.length - 1);
            }
            if (operationType ==  operationMap.length - 1) {
                divisionUsed = true;
                divisionLeft -= 1;
                if(mathDivisorLimit && (number > 10 || number  < 1)) {
                    number = uniformRNG(randFunc, 10) + 1;
                }
            }

            boolean addParentheses = (flipCoinRNG(randFunc) && flipCoinRNG(randFunc));
            if (addParentheses) {
                if(parenthesesPosition.empty()) {
                    stringBuilder.append(operationMap[operationType]);
                    stringBuilder.append('(');
                    stringBuilder.append(number);
                    parenthesesPosition.push(currentGroupPos);
                } else {
                    stringBuilder.append(operationMap[operationType]);
                    stringBuilder.append(number);
                    if(parenthesesPosition.peek() != currentGroupPos - 1) {
                        stringBuilder.append(')');
                        parenthesesPosition.pop();
                    }
                }
            } else {
                stringBuilder.append(operationMap[operationType]);
                stringBuilder.append(number);
            }
            if(!parenthesesPosition.empty() && divisionUsed) {
                stringBuilder.append(')');
                parenthesesPosition.pop();
            }
            currentGroupPos += 1;
        }
    }

    private int generateNumberForQuestion(Random randFunc) {
        if (useNormalDistribution) {
            return normalRNG(randFunc, mathNumberLimit, mathNumberLimit / 2.0);
        }
        return uniformRNG(randFunc, mathNumberLimit);
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
        if (value.equals("*") || value.equals("/")) {
            return 2;
        }
        if (value.equals("+") || value.equals("-")) {
            return 1;
        }
        return -1;
    }

    private double executeOperation(double first, double second, String operator) {
        if (operator.equals("+")) {
            return first + second;
        }
        if (operator.equals("-")) {
            return first - second;
        }
        if (operator.equals("*")) {
            return first * second;
        }
        return first / second;
    }
}
