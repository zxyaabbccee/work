package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private String currentInput = "";
    private String operator = "";
    private double firstValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        tvDisplay = findViewById(R.id.tvDisplay);

        // 数字按钮
        setNumberButtonListener(R.id.btn0, "0");
        setNumberButtonListener(R.id.btn1, "1");
        setNumberButtonListener(R.id.btn2, "2");
        setNumberButtonListener(R.id.btn3, "3");
        setNumberButtonListener(R.id.btn4, "4");
        setNumberButtonListener(R.id.btn5, "5");
        setNumberButtonListener(R.id.btn6, "6");
        setNumberButtonListener(R.id.btn7, "7");
        setNumberButtonListener(R.id.btn8, "8");
        setNumberButtonListener(R.id.btn9, "9");

        // 运算符按钮
        setOperatorButtonListener(R.id.btnPlus, "+");
        setOperatorButtonListener(R.id.btnMinus, "-");
        setOperatorButtonListener(R.id.btnMultiply, "×");
        setOperatorButtonListener(R.id.btnDivide, "÷");

        // 小数点
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (!currentInput.contains(".")) {
                currentInput += ".";
                tvDisplay.setText(currentInput);
            }
        });

        // 清除按钮
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput = "";
            operator = "";
            firstValue = 0;
            tvDisplay.setText("0.0");
        });
    }

    private void setNumberButtonListener(int buttonId, String number) {
        findViewById(buttonId).setOnClickListener(v -> {
            currentInput += number;
            tvDisplay.setText(currentInput);
        });
    }

    private void setOperatorButtonListener(int buttonId, String op) {
        findViewById(buttonId).setOnClickListener(v -> {
            if (!currentInput.isEmpty()) {
                if (!operator.isEmpty()) {
                    calculate();
                } else {
                    firstValue = Double.parseDouble(currentInput);
                }
                operator = op;
                currentInput = "";
            }
        });
    }

    private void calculate() {
        if (!currentInput.isEmpty() && !operator.isEmpty()) {
            double secondValue = Double.parseDouble(currentInput);
            double result = 0;

            switch (operator) {
                case "+":
                    result = firstValue + secondValue;
                    break;
                case "-":
                    result = firstValue - secondValue;
                    break;
                case "×":
                    result = firstValue * secondValue;
                    break;
                case "÷":
                    if (secondValue != 0) {
                        result = firstValue / secondValue;
                    }
                    break;
            }

            tvDisplay.setText(String.valueOf(result));
            firstValue = result;
            currentInput = String.valueOf(result);
            operator = "";
        }
    }
}
