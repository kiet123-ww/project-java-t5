package com.tkbplanner.ui.dialogs;

import com.tkbplanner.constants.Constants;
import com.tkbplanner.models.MonHoc;
import com.tkbplanner.models.ModelUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dialog để thêm lớp học mới
 */
public class ClassDialog extends Dialog<ClassDialog.Result> {
    
    public static class Result {
        private final String maMon;
        private final String maLop;
        private final String tenGv;
        private final String loaiLop;
        private final int thu;
        private final int tietBd;
        private final int tietKt;
        
        public Result(String maMon, String maLop, String tenGv, String loaiLop, int thu, int tietBd, int tietKt) {
            this.maMon = maMon;
            this.maLop = maLop;
            this.tenGv = tenGv;
            this.loaiLop = loaiLop;
            this.thu = thu;
            this.tietBd = tietBd;
            this.tietKt = tietKt;
        }
        
        public String getMaMon() { return maMon; }
        public String getMaLop() { return maLop; }
        public String getTenGv() { return tenGv; }
        public String getLoaiLop() { return loaiLop; }
        public int getThu() { return thu; }
        public int getTietBd() { return tietBd; }
        public int getTietKt() { return tietKt; }
    }
    
    private ComboBox<String> monHocCombo;
    private TextField maLopField;
    private TextField tenGvField;
    private ComboBox<String> loaiLopCombo;
    private ComboBox<String> thuCombo;
    private Spinner<Integer> tietBdSpinner;
    private Spinner<Integer> tietKtSpinner;
    private Map<String, MonHoc> allCourses;
    private MonHoc fixedMonHoc;
    
    public ClassDialog(Map<String, MonHoc> allCourses, Integer defaultThu, Integer defaultTiet, MonHoc fixedMonHoc) {
        this.allCourses = allCourses;
        this.fixedMonHoc = fixedMonHoc;
        
        setTitle("Thêm Lớp học mới");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        monHocCombo = new ComboBox<>();
        List<String> sortedKeys = new ArrayList<>(allCourses.keySet());
        sortedKeys.sort(String::compareTo);
        monHocCombo.getItems().addAll(sortedKeys);
        if (fixedMonHoc != null) {
            monHocCombo.setValue(fixedMonHoc.getMaMon());
            monHocCombo.setDisable(true);
        } else if (!sortedKeys.isEmpty()) {
            monHocCombo.setValue(sortedKeys.get(0));
        }
        
        maLopField = new TextField();
        tenGvField = new TextField();
        
        loaiLopCombo = new ComboBox<>();
        loaiLopCombo.getItems().addAll("Lý thuyết", "Bài tập", "Lớp");
        loaiLopCombo.setValue("Lớp");
        
        thuCombo = new ComboBox<>();
        thuCombo.getItems().addAll(Constants.TEN_THU_TRONG_TUAN.values());
        if (defaultThu != null) {
            thuCombo.setValue(Constants.TEN_THU_TRONG_TUAN.get(defaultThu));
        } else {
            thuCombo.setValue(Constants.TEN_THU_TRONG_TUAN.get(2)); // Default: Thứ 2
        }
        
        tietBdSpinner = new Spinner<>(1, 12, defaultTiet != null ? defaultTiet : 1);
        tietKtSpinner = new Spinner<>(1, 12, defaultTiet != null ? defaultTiet : 1);
        
        grid.add(new Label("Môn học:"), 0, 0);
        grid.add(monHocCombo, 1, 0);
        grid.add(new Label("Phòng học:"), 0, 1);
        grid.add(maLopField, 1, 1);
        grid.add(new Label("Tên GV:"), 0, 2);
        grid.add(tenGvField, 1, 2);
        grid.add(new Label("Loại lớp:"), 0, 3);
        grid.add(loaiLopCombo, 1, 3);
        grid.add(new Label("Thứ:"), 0, 4);
        grid.add(thuCombo, 1, 4);
        grid.add(new Label("Tiết bắt đầu:"), 0, 5);
        grid.add(tietBdSpinner, 1, 5);
        grid.add(new Label("Tiết kết thúc:"), 0, 6);
        grid.add(tietKtSpinner, 1, 6);
        
        getDialogPane().setContent(grid);
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String maMon = monHocCombo.getValue();
                String maLop = ModelUtils.chuanHoaMaLop(maLopField.getText().trim());
                String tenGv = ModelUtils.chuanHoaTenGiaoVien(tenGvField.getText().trim());
                String loaiLop = loaiLopCombo.getValue();
                
                String tenThu = thuCombo.getValue();
                int thu = Constants.TEN_THU_TRONG_TUAN.entrySet().stream()
                    .filter(e -> e.getValue().equals(tenThu))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElse(2);
                
                int tietBd = tietBdSpinner.getValue();
                int tietKt = tietKtSpinner.getValue();
                
                if (maMon == null || maMon.isEmpty() || maLop.isEmpty() || 
                    tenGv.isEmpty() || tietBd > tietKt) {
                    return null;
                }
                
                return new Result(maMon, maLop, tenGv, loaiLop, thu, tietBd, tietKt);
            }
            return null;
        });
    }
}

