package com.tkbplanner.ui.dialogs;

import com.tkbplanner.constants.Constants;
import com.tkbplanner.models.LopHoc;
import com.tkbplanner.models.MonHoc;
import com.tkbplanner.models.ThoiGianHoc;
import com.tkbplanner.scheduler.Scheduler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dialog để hiển thị các lớp học của một môn học
 */
public class CourseClassesDialog extends Dialog<List<LopHoc>> {
    
    private MonHoc monHoc;
    private Map<String, MonHoc> allCourses;
    private VBox classesLayout;
    private List<LopHoc> deletedClasses;
    
    public CourseClassesDialog(MonHoc monHoc, Map<String, MonHoc> allCourses) {
        this.monHoc = monHoc;
        this.allCourses = allCourses;
        this.deletedClasses = new ArrayList<>();
        
        setTitle("Các lớp học - " + monHoc.getTenMon() + " (" + monHoc.getMaMon() + ")");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        getDialogPane().setMinWidth(800);
        getDialogPane().setMinHeight(400);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Button addClassBtn = new Button("Thêm lớp học");
        addClassBtn.setOnAction(e -> handleAddClass());
        
        classesLayout = new VBox(5);
        classesLayout.setPadding(new Insets(5));
        
        ScrollPane scrollPane = new ScrollPane(classesLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        populateClasses();
        
        content.getChildren().addAll(addClassBtn, scrollPane);
        getDialogPane().setContent(content);
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return deletedClasses;
            }
            return null;
        });
    }
    
    private void populateClasses() {
        classesLayout.getChildren().clear();
        
        // Group by type
        Map<String, List<LopHoc>> classesByType = new java.util.HashMap<>();
        classesByType.put("Lý thuyết", new ArrayList<>());
        classesByType.put("Bài tập", new ArrayList<>());
        classesByType.put("Lớp", new ArrayList<>());
        
        for (LopHoc lop : monHoc.getCacLopHoc()) {
            String loai = lop.getLoaiLop() != null ? lop.getLoaiLop() : "Lớp";
            if (!classesByType.containsKey(loai)) {
                loai = "Lớp";
            }
            classesByType.get(loai).add(lop);
        }
        
        // Display each type
        for (String loaiLop : new String[]{"Lý thuyết", "Bài tập", "Lớp"}) {
            List<LopHoc> classes = classesByType.get(loaiLop);
            if (!classes.isEmpty()) {
                TitledPane group = new TitledPane(loaiLop, createClassGroup(classes));
                group.setCollapsible(false);
                classesLayout.getChildren().add(group);
            }
        }
    }
    
    private VBox createClassGroup(List<LopHoc> classes) {
        VBox group = new VBox(3);
        
        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(5));
        header.getChildren().addAll(
            new Label("Phòng học"),
            new Label("Giờ học"),
            new Label("Thứ"),
            new Label("GV"),
            new Label(""),
            new Label("")
        );
        group.getChildren().add(header);
        
        // Classes
        for (LopHoc lop : classes) {
            for (int i = 0; i < lop.getCacKhungGio().size(); i++) {
                ThoiGianHoc gio = lop.getCacKhungGio().get(i);
                HBox row = new HBox(10);
                row.setPadding(new Insets(3, 5, 3, 5));
                
                if (i == 0) {
                    row.getChildren().add(new Label(lop.getMaLop()));
                    row.getChildren().add(new Label("Tiết " + gio.getTietBatDau() + "-" + gio.getTietKetThuc()));
                    row.getChildren().add(new Label(Constants.TEN_THU_TRONG_TUAN.getOrDefault(gio.getThu(), "Thứ " + gio.getThu())));
                    row.getChildren().add(new Label(lop.getTenGiaoVien()));
                    
                    Button editBtn = new Button("Sửa");
                    editBtn.setMinWidth(60);
                    editBtn.setOnAction(e -> handleEditClass(lop));
                    
                    Button deleteBtn = new Button("Xóa");
                    deleteBtn.setMinWidth(60);
                    deleteBtn.setOnAction(e -> handleDeleteClass(lop));
                    
                    row.getChildren().addAll(editBtn, deleteBtn);
                } else {
                    row.getChildren().add(new Label(""));
                    row.getChildren().add(new Label("Tiết " + gio.getTietBatDau() + "-" + gio.getTietKetThuc()));
                    row.getChildren().add(new Label(Constants.TEN_THU_TRONG_TUAN.getOrDefault(gio.getThu(), "Thứ " + gio.getThu())));
                    row.getChildren().add(new Label(""));
                    row.getChildren().add(new Label(""));
                    row.getChildren().add(new Label(""));
                }
                
                group.getChildren().add(row);
            }
        }
        
        return group;
    }
    
    private void handleAddClass() {
        ClassDialog dialog = new ClassDialog(allCourses, null, null, monHoc);
        Optional<ClassDialog.Result> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() != null) {
            ClassDialog.Result data = result.get();
            LopHoc newLop = new LopHoc(data.getMaLop(), data.getTenGv(), 
                                      monHoc.getMaMon(), monHoc.getTenMon(), null, data.getLoaiLop());
            newLop.themKhungGio(data.getThu(), data.getTietBd(), data.getTietKt());
            
            Scheduler.ValidationResult validation = 
                Scheduler.kiemTraTrungTrongCungMon(newLop, monHoc, null);
            if (!validation.isValid()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText(validation.getErrorMessage());
                alert.showAndWait();
                return;
            }
            
            if (monHoc.themLopHoc(newLop)) {
                com.tkbplanner.data.DataHandler.saveData(allCourses);
                populateClasses();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã thêm lớp " + data.getMaLop() + " cho môn " + monHoc.getMaMon());
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Lớp học '" + data.getMaLop() + "' đã tồn tại với cùng giờ học trong môn này.");
                alert.showAndWait();
            }
        }
    }
    
    private void handleEditClass(LopHoc lop) {
        Integer defaultThu = null;
        Integer defaultTiet = null;
        if (!lop.getCacKhungGio().isEmpty()) {
            ThoiGianHoc gioDau = lop.getCacKhungGio().get(0);
            defaultThu = gioDau.getThu();
            defaultTiet = gioDau.getTietBatDau();
        }
        
        ClassDialog dialog = new ClassDialog(allCourses, defaultThu, defaultTiet, monHoc);
        // Pre-fill data
        // Note: ClassDialog doesn't have setters, so we'll need to modify it or create a new one
        // For now, user will need to re-enter
        
        Optional<ClassDialog.Result> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() != null) {
            ClassDialog.Result data = result.get();
            String oldId = lop.getId();
            
            LopHoc tempLop = new LopHoc(data.getMaLop(), data.getTenGv(), 
                                       monHoc.getMaMon(), monHoc.getTenMon(), null, data.getLoaiLop());
            tempLop.themKhungGio(data.getThu(), data.getTietBd(), data.getTietKt());
            
            Scheduler.ValidationResult validation = 
                Scheduler.kiemTraTrungTrongCungMon(tempLop, monHoc, oldId);
            if (!validation.isValid()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText(validation.getErrorMessage());
                alert.showAndWait();
                return;
            }
            
            // Update class
            lop.setMaLop(data.getMaLop());
            lop.setTenGiaoVien(data.getTenGv());
            lop.setLoaiLop(data.getLoaiLop());
            lop.getCacKhungGio().clear();
            lop.themKhungGio(data.getThu(), data.getTietBd(), data.getTietKt());
            
            // Update dict if ID changed
            if (!oldId.equals(lop.getId())) {
                monHoc.getCacLopHocDict().remove(oldId);
                monHoc.getCacLopHocDict().put(lop.getId(), lop);
            }
            
            com.tkbplanner.data.DataHandler.saveData(allCourses);
            populateClasses();
        }
    }
    
    private void handleDeleteClass(LopHoc lop) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa lớp '" + lop.getMaLop() + "'?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            monHoc.getCacLopHoc().remove(lop);
            monHoc.getCacLopHocDict().remove(lop.getId());
            deletedClasses.add(lop);
            com.tkbplanner.data.DataHandler.saveData(allCourses);
            populateClasses();
        }
    }
}

