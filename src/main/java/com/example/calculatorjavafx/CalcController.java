package com.example.calculatorjavafx;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CalcController {

    public Label labelAnswer;
    public Label labelResult;
    public Button btnClear;
    public Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    public Button btnPlus, btnMinus, btnMultiply, btnDivide;

    private enum State {
        INIT, NUMBER, SYMBOL, EQUAL, DOT, LEFT, RIGHT, CLEAR
    }

    private State state = State.INIT;

    private int leftBracketCount = 0;

    private boolean dotted = false;

    public void onNumberClicked(MouseEvent mouseEvent) {
        // 当数字（不含0）被按下时
        String value = ((Button) mouseEvent.getSource()).getId().replace("btn", "");

        if (state == State.INIT) {
            // 若输入框为0，即初始状态，则删除0并加上键入的数字
            labelResult.setText(value);
        } else if (state != State.RIGHT) {  // 右括号后不能跟数字
            if (state == State.EQUAL) {
                labelAnswer.setText("Ans = " + labelResult.getText());
            }
            labelResult.setText(labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN") ? value : labelResult.getText() + value);
        } else {
            return;
        }

        btnClear.setText("CE");
        state = State.NUMBER;
    }

    public void onZeroClicked() {
        // 当0被按下时
        if (state != State.INIT && state != State.RIGHT) {
            if (state == State.EQUAL) {
                labelAnswer.setText("Ans = " + labelResult.getText());
            }
            labelResult.setText(labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN") ? "0" : labelResult.getText() + "0");

            btnClear.setText("CE");
            state = State.NUMBER;
        }
    }

    public void onSymbolClicked(MouseEvent mouseEvent) {
        // 当符号（加、减、乘、除）被按下时
        if (state == State.LEFT || state == State.DOT) {
            // 左括号或小数点后不能跟符号
            return;
        }
        if (labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN")) {
            onClearClicked();
            return;
        }

        String value = ((Button) mouseEvent.getSource()).getId().replace("btn", "");

        switch (value) {
            case "Plus" -> value = "+";
            case "Minus" -> value = "-";
            case "Multiply" -> value = "×";
            case "Divide" -> value = "÷";
        }

        if (state == State.SYMBOL) {
            // 若上一个输入为运算符，则更改运算符为当前最新输入
            labelResult.setText(labelResult.getText().substring(0, labelResult.getText().length() - 1) + value);
        } else {
            if (state == State.EQUAL) {
                labelAnswer.setText("Ans = " + labelResult.getText());
            }
            labelResult.setText(labelResult.getText() + value);
        }

        btnClear.setText("CE");
        state = State.SYMBOL;
        dotted = false;
    }

    public void onEqualClicked() {
        // 当“等于”被按下时
        if (labelResult.getText().equals("0")) {
            return;
        }
        if (labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN")) {
            onClearClicked();
        }

        labelAnswer.setText(labelResult.getText() + "=");
        labelResult.setText(calculate(false));

        btnClear.setText("AC");
        state = State.EQUAL;
        dotted = labelResult.getText().contains(".");
        leftBracketCount = 0;
    }

    public void onClearClicked() {
        // 当“清除”被按下时
        if (state != State.EQUAL && state != State.INIT) {
            // CE - Clear Entry 删去一位 Backspace
            if (labelResult.getText().length() == 1) {
                // 已删完
                btnClear.setText("AC");
            } else {
                // 未删完
                switch (labelResult.getText().substring(labelResult.getText().length() - 1)) {
                    case "(" -> leftBracketCount--;
                    case ")" -> leftBracketCount++;
                    case "." -> dotted = false;
                }

                // 删除上一个输入
                labelResult.setText(labelResult.getText().substring(0, labelResult.getText().length() - 1));

                String prevValue = labelResult.getText().substring(labelResult.getText().length() - 1);
                switch (prevValue) {
                    case "." -> state = State.DOT;
                    case "(" -> state = State.LEFT;
                    case ")" -> state = State.RIGHT;
                    case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> state = State.NUMBER;
                    case "+", "-", "×", "÷" -> state = State.SYMBOL;
                }
                return;
            }
        }

        // AC - All Clear 全部删除（恢复为初始状态）
        labelAnswer.setText("Ans = " + labelResult.getText());
        labelResult.setText("0");

        state = State.INIT;
        leftBracketCount = 0;
        dotted = false;
    }

    public void onRootClicked() {
        // 当平方根被按下
        if (labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN")) {
            onClearClicked();
        }

        labelAnswer.setText("√(" + labelResult.getText() + ")=");
        labelResult.setText(calculate(true));

        btnClear.setText("AC");
        state = State.EQUAL;
        dotted = labelResult.getText().contains(".");
        leftBracketCount = 0;
    }

    public void onDotClicked() {
        // 当小数点被按下
        if (dotted || state == State.RIGHT) {
            return;
        }
        if (labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN")) {
            onClearClicked();
            return;
        }

        if (state == State.INIT || state == State.EQUAL || state == State.NUMBER) {
            if (state == State.EQUAL) {
                labelAnswer.setText("Ans = " + labelResult.getText());
            }
            labelResult.setText(labelResult.getText() + ".");
        } else if (state == State.SYMBOL || state == State.LEFT) {
            labelResult.setText(labelResult.getText() + "0.");
        } else {
            return;
        }

        dotted = true;
        btnClear.setText("CE");
        state = State.DOT;
    }

    public void onLeftClicked() {
        // 当左括号被按下
        if (state == State.NUMBER || state == State.DOT || state == State.RIGHT) {
            return;
        }
        if (labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN")) {
            onClearClicked();
        }

        if (state == State.INIT) {
            // 若输入框为0，即初始状态，则删除0并加上左括号
            labelResult.setText("(");
        } else {
            labelResult.setText(labelResult.getText() + "(");
        }

        btnClear.setText("CE");
        state = State.LEFT;
        leftBracketCount++;
        dotted = false;
    }

    public void onRightClicked() {
        // 当右括号被按下
        if (labelResult.getText().equals("Error") || labelResult.getText().equals("Infinite") || labelResult.getText().equals("NaN")) {
            onClearClicked();
            return;
        }

        if (leftBracketCount > 0 && state != State.SYMBOL && state != State.DOT) {
            labelResult.setText(labelResult.getText() + ")");

            leftBracketCount--;
            btnClear.setText("CE");
            state = State.RIGHT;
            dotted = false;
        }
    }

    private String calculate(boolean isRooted) {
        // 计算
        String expression = labelResult.getText();
        List<String> suffix = toSuffix(strToList(expression));
        Stack<String> stack = new Stack<>();

        System.out.println("Calculate: " + expression);
        System.out.println("Suffix   : " + suffix + "\n");

        for (String item : suffix) {
            if (item.matches("^[0-9]+[\\\\.][0-9]+") || item.matches("^[0-9]+")) {
                stack.push(item);  // 数字直接入栈
            } else {
                try {
                    double d1 = Double.parseDouble(stack.pop()), d2 = Double.parseDouble(stack.pop());
                    switch (item) {  // 若为运算符，则出栈并计算
                        case "+" -> stack.push(String.valueOf(BigDecimal.valueOf(d1).add(BigDecimal.valueOf(d2))));
                        case "-" -> stack.push(String.valueOf(BigDecimal.valueOf(d2).subtract(BigDecimal.valueOf(d1))));
                        case "×" -> stack.push(String.valueOf(BigDecimal.valueOf(d1).multiply(BigDecimal.valueOf(d2))));
                        case "÷" ->
                                stack.push(String.valueOf(BigDecimal.valueOf(d2).divide(BigDecimal.valueOf(d1), 9, RoundingMode.HALF_UP)));
                    }
                } catch (ArithmeticException arithmeticException) {
                    return "Infinite";
                } catch (Exception e) {
                    return "Error";
                }
            }
        }

        return isRooted ? formatDouble(Math.sqrt(Double.parseDouble(stack.pop()))) : formatDouble(Double.parseDouble(stack.pop()));
    }

    private String formatDouble(Double d) {
        if (Double.isFinite(d)) {
            return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        } else {
            return d.toString();
        }
    }

    private List<String> strToList(String expression) {
        // 将算式从String转为ArrayList
        ArrayList<String> resultList = new ArrayList<>();
        StringBuilder temp = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char character = expression.charAt(i);
            if (Character.isDigit(character) || character == '.') {
                // 若为数字或小数点，则暂存于temp
                temp.append(character);
            } else {
                // 若为运算符
                if (!temp.toString().equals("")) {
                    // 若temp不为空，即上一位为数字最末，则向ArrayList中加入数字
                    resultList.add(temp.toString());
                    temp = new StringBuilder();  // 清空temp
                }
                resultList.add(String.valueOf(character));  // 将运算符加入ArrayList
            }
        }
        if (!temp.toString().equals("")) {
            resultList.add(temp.toString());
        }

        if (resultList.get(0).equals("-")) {
            resultList.add(2, ")");
            resultList.add(0, "0");
            resultList.add(0, "(");
        }

        return resultList;
    }

    private int getPriority(String operator) {
        return switch (operator) {
            case "×", "÷" -> 1;
            case "+", "-" -> 0;
            default -> -1;
        };
    }

    private List<String> toSuffix(List<String> expressionList) {
        // 获取后缀表达式
        ArrayList<String> suffix = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (String item : expressionList) {
            if (item.matches("^[0-9]+[\\\\.][0-9]+") || item.matches("^[0-9]+")) {
                suffix.add(item);  // 数字直接加入后缀表达式
            } else if (item.equals("(")) {
                stack.push(item);  // 左括号入栈
            } else if (item.equals(")")) {
                while (!stack.peek().equals("(")) {
                    suffix.add(stack.pop());  // 加入后缀表达式直至左括号
                }
                stack.pop();  // 左括号舍去
            } else {
                while (stack.size() != 0 && getPriority(item) <= getPriority(stack.peek())) {
                    suffix.add(stack.pop());  // 当栈不为空且栈顶运算符的优先级大于等于当前运算符时，则将栈顶出栈并加入后缀表达式
                }
                stack.push(item);
            }
        }
        while (stack.size() != 0) {
            suffix.add(stack.pop());
        }

        return suffix;
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        String text = keyEvent.getText().toUpperCase();
        String code = keyEvent.getCode().toString();

        switch (text) {
            case "0" -> onZeroClicked();
            case "1" ->
                    onNumberClicked(new MouseEvent(btn1, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "2" ->
                    onNumberClicked(new MouseEvent(btn2, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "3" ->
                    onNumberClicked(new MouseEvent(btn3, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "4" ->
                    onNumberClicked(new MouseEvent(btn4, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "5" ->
                    onNumberClicked(new MouseEvent(btn5, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "6" ->
                    onNumberClicked(new MouseEvent(btn6, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "7" ->
                    onNumberClicked(new MouseEvent(btn7, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "8" ->
                    onNumberClicked(new MouseEvent(btn8, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "9" ->
                    onNumberClicked(new MouseEvent(btn9, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "+" ->
                    onSymbolClicked(new MouseEvent(btnPlus, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "-" ->
                    onSymbolClicked(new MouseEvent(btnMinus, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "*" ->
                    onSymbolClicked(new MouseEvent(btnMultiply, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "/" ->
                    onSymbolClicked(new MouseEvent(btnDivide, null, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
            case "=" -> onEqualClicked();
            case "." -> onDotClicked();
            case "(" -> onLeftClicked();
            case ")" -> onRightClicked();
            case "R" -> onRootClicked();
            default -> {
                switch (code) {
                    case "ENTER" -> onEqualClicked();
                    case "BACK_SPACE" -> onClearClicked();
                }
            }
        }
    }

}
