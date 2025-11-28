package com.tkbplanner.ui.dialogs;

import com.tkbplanner.models.MonHoc;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog để thêm hoặc sửa môn học
 */
public class SubjectDialog extends Dialog<SubjectDialog.Result> {
    
    public static class Result {
        private final String maMon;
        private final String tenMon;
        private final List<String> tienQuyet;
        
        public Result(String maMon, String tenMon, List<String> tienQuyet) {
            this.maMon = maMon;
            this.tenMon = tenMon;
            this.tienQuyet = tienQuyet;
        }
        
        public String getMaMon() { return maMon; }
        public String getTenMon() { return tenMon; }
        public List<String> getTienQuyet() { return tienQuyet; }
    }
    
    private TextField maMonField;
    private TextField tenMonField;
    private TextField tienQuyetField;
    private boolean isEditMode;
    
    public SubjectDialog(MonHoc monHoc) {
        this.isEditMode = monHoc != null;
        
        setTitle(isEditMode ? "Chỉnh sửa Môn học" : "Thêm Môn học mới");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        maMonField = new TextField();
        tenMonField = new TextField();
        tienQuyetField = new TextField();
        tienQuyetField.setPromptText("VD: MI1111, IT1110 (cách nhau bởi dấu phẩy)");
        
        if (isEditMode) {
            maMonField.setText(monHoc.getMaMon());
            maMonField.setEditable(false);
            tenMonField.setText(monHoc.getTenMon());
            if (monHoc.getTienQuyet() != null && !monHoc.getTienQuyet().isEmpty()) {
                tienQuyetField.setText(String.join(", ", monHoc.getTienQuyet()));
            }
        }
        
        grid.add(new Label("Mã môn:"), 0, 0);
        grid.add(maMonField, 1, 0);
        grid.add(new Label("Tên môn:"), 0, 1);
        grid.add(tenMonField, 1, 1);
        grid.add(new Label("Môn tiên quyết:"), 0, 2);
        grid.add(tienQuyetField, 1, 2);
        
        getDialogPane().setContent(grid);
        
        // Buttons
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        
        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String maMon = maMonField.getText().trim().toUpperCase();
                String tenMon = tenMonField.getText().trim();
                List<String> tienQuyet = Arrays.stream(tienQuyetField.getText().split(","))
                    .map(s -> s.trim().toUpperCase())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
                
                if (maMon.isEmpty() || tenMon.isEmpty()) {
                    return null;
                }
                
                return new Result(maMon, tenMon, tienQuyet);
            }
            return null;
        });
    }
}

