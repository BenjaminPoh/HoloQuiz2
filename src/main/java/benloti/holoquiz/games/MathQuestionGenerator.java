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

    private void generateChaoticQuestion(Random randFunc, StringBuilder stringBuilder, Stack<Integer> parenthesesPosition, int operationsUsed) {
        int currentGroupPos = 1;
        while (operationsUsed >= currentGroupPos) {
            int operationType = uniformRNG(randFunc, 4);
            int number = generateNumberForQuestion(randFunc);
            if(number == 0 && operationType == 3) {
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
            int operationType = uniformRNG(randFunc, 4);
            int number = generateNumberForQuestion(randFunc);
            if (divisionLeft == 0) {
                operationType = uniformRNG(randFunc, 3);
            }
            if (operationType == 3) {
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
