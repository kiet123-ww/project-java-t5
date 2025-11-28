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
import java.util.function.Supplier;

/**
 * Dialog để nhập môn đã học
 */
public class CompletedCoursesDialog extends Dialog<List<String>> {
    
    private ListView<String> courseList;
    private Map<String, MonHoc> allCourses;
    private List<String> completedCourses;
    private Supplier<String> addCourseCallback;
    
    public CompletedCoursesDialog(Map<String, MonHoc> allCourses, List<String> completedCourses, 
                                  Supplier<String> addCourseCallback) {
        this.allCourses = allCourses;
        this.completedCourses = new ArrayList<>(completedCourses);
        this.addCourseCallback = addCourseCallback;
        
        setTitle("Nhập môn đã học");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label label = new Label("Chọn các môn đã học (có thể chọn nhiều môn):");
        
        courseList = new ListView<>();
        courseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        populateCourseList();
        
        Button addButton = null;
        if (addCourseCallback != null) {
            addButton = new Button("Thêm môn");
            addButton.setOnAction(e -> handleAddCourse());
        }
        
        content.getChildren().add(label);
        content.getChildren().add(courseList);
        if (addButton != null) {
            content.getChildren().add(addButton);
        }
        
        getDialogPane().setContent(content);
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                ObservableList<String> selected = courseList.getSelectionModel().getSelectedItems();
                List<String> result = new ArrayList<>(selected);
                // Add completed courses that are disabled
                for (String maMon : completedCourses) {
                    if (!result.contains(maMon)) {
                        result.add(maMon);
                    }
                }
                return result;
            }
            return null;
        });
    }
    
    private void populateCourseList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        List<String> sortedKeys = new ArrayList<>(allCourses.keySet());
        sortedKeys.sort(String::compareTo);
        
        for (String maMon : sortedKeys) {
            MonHoc monHoc = allCourses.get(maMon);
            items.add(monHoc.getTenMon() + " (" + maMon + ")");
        }
        
        courseList.setItems(items);
        
        // Select completed courses
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            for (String maMon : completedCourses) {
                if (item.contains("(" + maMon + ")")) {
                    courseList.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }
    
    private void handleAddCourse() {
        if (addCourseCallback != null) {
            String newMaMon = addCourseCallback.get();
            if (newMaMon != null) {
                populateCourseList();
            }
        }
    }
}

