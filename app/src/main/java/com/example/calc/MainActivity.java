package com.example.calc;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private ListView lvHistory;
    private ArrayAdapter<String> historyAdapter;
    private ArrayList<String> historyList = new ArrayList<>();

    private StringBuilder currentInput = new StringBuilder();
    private String operator = "";
    private BigDecimal firstOperand = null;

    private static final String STATE_FIRST_OPERAND = "firstOperand";
    private static final String STATE_CURRENT_INPUT = "currentInput";
    private static final String STATE_OPERATOR = "operator";
    private static final String STATE_HISTORY = "history";

    // Анимации
    private Animation buttonClickAnimation;
    private Animation displayAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Восстанавливаем состояние при повороте экрана
        if (savedInstanceState != null) {
            firstOperand = (BigDecimal) savedInstanceState.getSerializable(STATE_FIRST_OPERAND);
            currentInput = new StringBuilder(savedInstanceState.getString(STATE_CURRENT_INPUT));
            operator = savedInstanceState.getString(STATE_OPERATOR);
            historyList = savedInstanceState.getStringArrayList(STATE_HISTORY);
        }

        tvDisplay = findViewById(R.id.tvDisplay);
        tvDisplay.setText(currentInput.length() > 0 ? currentInput.toString() : "0");

        // Инициализация ListView и адаптера для истории
        lvHistory = findViewById(R.id.lvHistory);
        historyAdapter = new ArrayAdapter<>(this, R.layout.history_item, R.id.tvHistoryItem, historyList);
        lvHistory.setAdapter(historyAdapter);

        // Инициализация анимаций
        buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click);
        displayAnimation = AnimationUtils.loadAnimation(this, R.anim.display_update);

        setNumericOnClickListener();
        setOperatorOnClickListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_FIRST_OPERAND, firstOperand);
        outState.putString(STATE_CURRENT_INPUT, currentInput.toString());
        outState.putString(STATE_OPERATOR, operator);
        outState.putStringArrayList(STATE_HISTORY, historyList);
        super.onSaveInstanceState(outState);
    }

    private void setNumericOnClickListener() {
        View.OnClickListener listener = v -> {
            v.startAnimation(buttonClickAnimation); // Анимация кнопки
            Button button = (Button) v;
            String inputChar = button.getText().toString();

            if (inputChar.equals(".") && currentInput.toString().contains(".")) {
                // Предотвращаем ввод более одного десятичного разделителя
                return;
            }

            currentInput.append(inputChar);
            tvDisplay.setText(currentInput.toString());
        };

        int[] numericButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        };

        for (int id : numericButtons) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void setOperatorOnClickListener() {
        View.OnClickListener listener = v -> {
            v.startAnimation(buttonClickAnimation); // Анимация кнопки
            Button button = (Button) v;
            String op = button.getText().toString();

            if (currentInput.length() > 0) {
                if (firstOperand != null) {
                    calculate();
                } else {
                    firstOperand = new BigDecimal(currentInput.toString());
                }
            }

            operator = op;
            currentInput.setLength(0);
        };

        int[] operatorButtons = {
                R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide
        };

        for (int id : operatorButtons) {
            findViewById(id).setOnClickListener(listener);
        }

        findViewById(R.id.btnEqual).setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation); // Анимация кнопки
            calculate();
            operator = "";
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            v.startAnimation(buttonClickAnimation); // Анимация кнопки
            clearCalculator();
        });
    }

    private void calculate() {
        if (operator.isEmpty() || currentInput.length() == 0 || firstOperand == null) {
            return;
        }

        BigDecimal secondOperand = new BigDecimal(currentInput.toString());
        BigDecimal result = BigDecimal.ZERO;

        try {
            switch (operator) {
                case "+":
                    result = firstOperand.add(secondOperand);
                    break;

                case "-":
                    result = firstOperand.subtract(secondOperand);
                    break;

                case "*":
                    result = firstOperand.multiply(secondOperand);
                    break;

                case "/":
                    if (secondOperand.compareTo(BigDecimal.ZERO) == 0) {
                        tvDisplay.setText(getString(R.string.error_divide_by_zero));
                        clearCalculator();
                        return;
                    }
                    result = firstOperand.divide(secondOperand, 10, BigDecimal.ROUND_HALF_UP);
                    break;
            }
        } catch (ArithmeticException e) {
            tvDisplay.setText(getString(R.string.error));
            clearCalculator();
            return;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##########");
        String resultStr = decimalFormat.format(result);
        tvDisplay.setText(resultStr);
        tvDisplay.startAnimation(displayAnimation); // Анимация экрана

        // Сохранение в историю
        String historyEntry = decimalFormat.format(firstOperand) + " " + operator + " "
                + decimalFormat.format(secondOperand) + " = " + resultStr;
        historyList.add(0, historyEntry); // Добавляем в начало списка
        historyAdapter.notifyDataSetChanged();

        firstOperand = result;
        currentInput.setLength(0);
    }

    private void clearCalculator() {
        firstOperand = null;
        currentInput.setLength(0);
        operator = "";
        tvDisplay.setText("0");
    }
}
