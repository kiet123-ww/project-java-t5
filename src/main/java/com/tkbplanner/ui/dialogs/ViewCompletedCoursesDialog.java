package com.tkbplanner.ui.dialogs;

import com.tkbplanner.models.MonHoc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dialog để hiển thị danh sách môn đã học
 */
public class ViewCompletedCoursesDialog extends Dialog<List<String>> {
    
    private ListView<String> courseList;
    private List<String> completedCourses;
    private Map<String, MonHoc> allCourses;
    
    public ViewCompletedCoursesDialog(Map<String, MonHoc> allCourses, List<String> completedCourses) {
        this.allCourses = allCourses;
        this.completedCourses = new ArrayList<>(completedCourses);
        
        setTitle("Danh sách môn đã học");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        getDialogPane().setMinWidth(500);
        getDialogPane().setMinHeight(400);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label label = new Label("Tổng số môn đã học: " + completedCourses.size());
        
        courseList = new ListView<>();
        courseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        ObservableList<String> items = FXCollections.observableArrayList();
        List<String> sorted = new ArrayList<>(completedCourses);
        sorted.sort(String::compareTo);
        
        for (String maMon : sorted) {
            MonHoc monHoc = allCourses.get(maMon);
            if (monHoc != null) {
                items.add(monHoc.getTenMon() + " (" + maMon + ")");
            } else {
                items.add(maMon + " (Môn không còn trong hệ thống)");
            }
        }
        
        if (items.isEmpty()) {
            items.add("Chưa có môn nào được đánh dấu là đã học");
        }
        
        courseList.setItems(items);
        
        Button deleteButton = new Button("Xóa các môn đã chọn");
        deleteButton.setOnAction(e -> handleDeleteSelected());
        
        content.getChildren().addAll(label, courseList, deleteButton);
        
        getDialogPane().setContent(content);
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, closeButtonType);
        
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType || dialogButton == closeButtonType) {
                return this.completedCourses;
            }
            return null;
        });
    }
    
    private void handleDeleteSelected() {
        ObservableList<String> selected = courseList.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa chọn môn");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn ít nhất một môn để xóa.");
            alert.showAndWait();
            return;
        }
        
        List<String> toRemove = new ArrayList<>();
        for (String item : selected) {
            if (item.contains("(") && item.contains(")")) {
                String maMon = item.substring(item.indexOf("(") + 1, item.indexOf(")"));
                if (completedCourses.contains(maMon)) {
                    toRemove.add(maMon);
                }
            }
        }
        
        if (toRemove.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy môn nào để xóa.");
            alert.showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa " + toRemove.size() + " môn khỏi danh sách môn đã học?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            completedCourses.removeAll(toRemove);
            
            // Update list
            ObservableList<String> items = FXCollections.observableArrayList();
            List<String> sorted = new ArrayList<>(completedCourses);
            sorted.sort(String::compareTo);
            
            for (String maMon : sorted) {
                MonHoc monHoc = allCourses.get(maMon);
                if (monHoc != null) {
                    items.add(monHoc.getTenMon() + " (" + maMon + ")");
                } else {
                    items.add(maMon + " (Môn không còn trong hệ thống)");
                }
            }
            
            if (items.isEmpty()) {
                items.add("Chưa có môn nào được đánh dấu là đã học");
            }
            
            courseList.setItems(items);
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Thành công");
            success.setHeaderText(null);
            success.setContentText("Đã xóa " + toRemove.size() + " môn khỏi danh sách môn đã học.");
            success.showAndWait();
        }
    }
}

