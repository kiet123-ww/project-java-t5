package com.tkbplanner.ui;

import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;

/**
 * Custom Checkbox với style đặc biệt
 */
public class CustomCheckBox extends CheckBox {
    private Runnable onTextClicked;
    
    public CustomCheckBox(String text) {
        super(text);
        setStyle("-fx-font-size: 10pt;");
    }
    
    public void setOnTextClicked(Runnable handler) {
        this.onTextClicked = handler;
    }
    
    // Note: JavaFX CheckBox không có cách dễ dàng để phân biệt click vào text vs checkbox
    // Có thể implement bằng cách override skin hoặc sử dụng event filter
    // Tạm thời để đơn giản, chúng ta sẽ xử lý trong MainWindow
}

