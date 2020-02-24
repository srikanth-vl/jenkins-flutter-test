package com.vassar.unifiedapp.ui;

import android.view.View;

public class ComputedExpression {
    private String expression;
    private View view;
    private String value;

    public ComputedExpression() {
    }

    public ComputedExpression(String expression, View view, String value) {
        this.expression = expression;
        this.view = view;
        this.value = value;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ComputedExpression{" +
                "expression='" + expression + '\'' +
                ", view=" + view +
                ", value=" + value +
                '}';
    }
}
