package com.tkbplanner.ui;

import com.tkbplanner.constants.Constants;
import com.tkbplanner.models.LichBan;
import com.tkbplanner.models.LopHoc;
import com.tkbplanner.models.MonHoc;
import com.tkbplanner.models.ThoiGianHoc;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Widget hiển thị thời khóa biểu dạng lưới với khả năng click vào ô
 */
public class ScheduleWidget extends Pane {
    private static final int MAX_TIET = 12;
    private static final int HEADER_HEIGHT = 60;
    private static final int TIME_COL_WIDTH = 60;
    
    private Canvas canvas;
    private double cellWidth;
    private double cellHeight;
    private List<LopHoc> currentSchedule;
    private List<LichBan> busyTimes;
    private Map<String, String> scheduleColors;
    private LocalDate startOfWeek;
    private boolean darkMode;
    
    private java.util.function.BiConsumer<Integer, Integer> onCellClicked;
    
    public ScheduleWidget() {
        this.currentSchedule = new ArrayList<>();
        this.busyTimes = new ArrayList<>();
        this.scheduleColors = new HashMap<>();
        this.darkMode = false;
        
        // Tính ngày Thứ 2 của tuần này
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        this.startOfWeek = today.minusDays(dayOfWeek - 1);
        
        canvas = new Canvas();
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        
        canvas.setOnMouseClicked(this::handleMouseClick);
        
        getChildren().add(canvas);
        
        // Redraw when size changes
        widthProperty().addListener((obs, oldVal, newVal) -> draw());
        heightProperty().addListener((obs, oldVal, newVal) -> draw());
    }
    
    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        draw();
    }
    
    public void setOnCellClicked(java.util.function.BiConsumer<Integer, Integer> handler) {
        this.onCellClicked = handler;
    }
    
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        
        // Kiểm tra click vào vùng hợp lệ
        if (x < TIME_COL_WIDTH || y < HEADER_HEIGHT) {
            return;
        }
        
        // Tính toán chỉ số thứ và tiết
        int thuIndex = (int) ((x - TIME_COL_WIDTH) / cellWidth);
        int tiet = (int) ((y - HEADER_HEIGHT) / cellHeight) + 1;
        
        // Kiểm tra bounds
        List<Integer> thuKeys = new ArrayList<>(Constants.TEN_THU_TRONG_TUAN.keySet());
        if (thuIndex < 0 || thuIndex >= thuKeys.size()) {
            return;
        }
        if (tiet < 1 || tiet > MAX_TIET) {
            return;
        }
        
        // Lấy thứ tương ứng
        int thu = thuKeys.get(thuIndex);
        
        // Emit signal (sẽ được xử lý bởi MainWindow)
        if (onCellClicked != null) {
            onCellClicked.accept(thu, tiet);
        }
    }
    
    public void displaySchedule(List<LopHoc> tkb, Map<String, MonHoc> allCourses, List<LichBan> busyTimes) {
        this.currentSchedule = tkb != null ? new ArrayList<>(tkb) : new ArrayList<>();
        this.busyTimes = busyTimes != null ? new ArrayList<>(busyTimes) : new ArrayList<>();
        this.scheduleColors.clear();
        
        if (allCourses != null) {
            for (Map.Entry<String, MonHoc> entry : allCourses.entrySet()) {
                if (entry.getValue().getColorHex() != null) {
                    scheduleColors.put(entry.getKey(), entry.getValue().getColorHex());
                }
            }
        }
        
        draw();
    }
    
    private void draw() {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Tính toán kích thước cell
        int numDays = Constants.TEN_THU_TRONG_TUAN.size();
        cellWidth = (canvas.getWidth() - TIME_COL_WIDTH) / numDays;
        cellHeight = (canvas.getHeight() - HEADER_HEIGHT) / MAX_TIET;
        
        drawGrid(gc);
        drawBusyTimes(gc);
        drawSchedule(gc);
    }
    
    private void drawGrid(GraphicsContext gc) {
        Color headerColor = Color.web("#C00000");
        Color textColor = Color.WHITE;
        Color gridColor = darkMode ? Color.web("#666666") : Color.web("#d0d0d0");
        Color bgColor = darkMode ? Color.web("#1e1e1e") : Color.WHITE;
        Color textColorGrid = darkMode ? Color.web("#e0e0e0") : Color.BLACK;
        
        // Vẽ nền
        gc.setFill(bgColor);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Vẽ header cột "Tiết"
        gc.setFill(headerColor);
        gc.fillRect(0, 0, TIME_COL_WIDTH, HEADER_HEIGHT);
        gc.setFill(textColor);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Tiết", TIME_COL_WIDTH / 2, HEADER_HEIGHT / 2);
        
        // Vẽ header các thứ
        List<Integer> thuKeys = new ArrayList<>(Constants.TEN_THU_TRONG_TUAN.keySet());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 0; i < thuKeys.size(); i++) {
            double x = TIME_COL_WIDTH + i * cellWidth;
            gc.setFill(headerColor);
            gc.fillRect(x, 0, cellWidth, HEADER_HEIGHT);
            
            // Vẽ Thứ và Ngày
            int thu = thuKeys.get(i);
            String thuText = Constants.TEN_THU_TRONG_TUAN.get(thu);
            LocalDate currentDay = startOfWeek.plusDays(i);
            String dateStr = currentDay.format(dateFormatter);
            
            gc.setFill(textColor);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.fillText(thuText, x + cellWidth / 2, HEADER_HEIGHT / 2 - 5);
            gc.setFont(Font.font("Segoe UI", 9));
            gc.fillText(dateStr, x + cellWidth / 2, HEADER_HEIGHT / 2 + 10);
        }
        
        // Vẽ lưới và số tiết
        gc.setStroke(gridColor);
        gc.setFill(textColorGrid);
        gc.setFont(Font.font("Segoe UI", 9));
        
        for (int tiet = 1; tiet <= MAX_TIET; tiet++) {
            double y = HEADER_HEIGHT + (tiet - 1) * cellHeight;
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(tiet), TIME_COL_WIDTH / 2, y + cellHeight / 2);
            gc.strokeLine(0, y + cellHeight, canvas.getWidth(), y + cellHeight);
        }
        
        // Vẽ đường kẻ dọc
        for (int i = 0; i <= thuKeys.size(); i++) {
            double x = TIME_COL_WIDTH + i * cellWidth;
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
    }
    
    private void drawBusyTimes(GraphicsContext gc) {
        if (busyTimes.isEmpty()) {
            return;
        }
        
        Color busyColor = darkMode ? Color.web("#3a3a3a") : Color.web("#f0f0f0");
        Color borderColor = darkMode ? Color.web("#666666") : Color.web("#cccccc");
        Color textColor = darkMode ? Color.web("#aaaaaa") : Color.web("#666666");
        
        for (LichBan busyTime : busyTimes) {
            int tietBd = LichBan.timeToTiet(busyTime.getGioBatDau());
            int tietKt = LichBan.timeToTiet(busyTime.getGioKetThuc());
            
            if (tietBd == -1) {
                tietBd = LichBan.findNearestTiet(busyTime.getGioBatDau());
            }
            if (tietKt == -1) {
                tietKt = LichBan.findNearestTiet(busyTime.getGioKetThuc());
            }
            
            if (tietBd == -1 || tietKt == -1) {
                continue;
            }
            
            if (tietBd > tietKt) {
                int temp = tietBd;
                tietBd = tietKt;
                tietKt = temp;
            }
            
            int thu = busyTime.getThu();
            List<Integer> thuKeys = new ArrayList<>(Constants.TEN_THU_TRONG_TUAN.keySet());
            int thuIndex = thuKeys.indexOf(thu);
            if (thuIndex < 0) {
                continue;
            }
            
            double x = TIME_COL_WIDTH + thuIndex * cellWidth;
            double y = HEADER_HEIGHT + (tietBd - 1) * cellHeight;
            double rectWidth = cellWidth;
            double rectHeight = (tietKt - tietBd + 1) * cellHeight;
            
            // Vẽ vùng giờ bận
            gc.setFill(busyColor);
            gc.setStroke(borderColor);
            gc.setLineWidth(1);
            gc.fillRoundRect(x + 2, y + 2, rectWidth - 4, rectHeight - 4, 5, 5);
            gc.strokeRoundRect(x + 2, y + 2, rectWidth - 4, rectHeight - 4, 5, 5);
            
            // Vẽ text
            gc.setFill(textColor);
            gc.setFont(Font.font("Segoe UI", 9));
            gc.setTextAlign(TextAlignment.CENTER);
            
            String gioBdStr = busyTime.getGioBatDau().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String gioKtStr = busyTime.getGioKetThuc().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String tenThu = Constants.TEN_THU_TRONG_TUAN.getOrDefault(thu, "Thứ " + thu);
            String lyDo = busyTime.getLyDo();
            if (lyDo.length() > 15) {
                lyDo = lyDo.substring(0, 15) + "...";
            }
            
            String textContent = "BẬN\n" + tenThu + "\n" + gioBdStr + "-" + gioKtStr + "\n" + lyDo;
            gc.fillText(textContent, x + rectWidth / 2, y + rectHeight / 2);
        }
    }
    
    private void drawSchedule(GraphicsContext gc) {
        Color borderColor = darkMode ? Color.web("#888888") : Color.web("#666666");
        Color textColor = Color.BLACK;
        
        List<Integer> thuKeys = new ArrayList<>(Constants.TEN_THU_TRONG_TUAN.keySet());
        
        for (LopHoc lopHoc : currentSchedule) {
            String colorHex = scheduleColors.getOrDefault(lopHoc.getMaMon(), "#ffffff");
            Color fillColor = Color.web(colorHex);
            
            for (ThoiGianHoc khungGio : lopHoc.getCacKhungGio()) {
                int thu = khungGio.getThu();
                int thuIndex = thuKeys.indexOf(thu);
                if (thuIndex < 0) {
                    continue;
                }
                
                double x = TIME_COL_WIDTH + thuIndex * cellWidth;
                double y = HEADER_HEIGHT + (khungGio.getTietBatDau() - 1) * cellHeight;
                double rectWidth = cellWidth;
                double rectHeight = (khungGio.getTietKetThuc() - khungGio.getTietBatDau() + 1) * cellHeight;
                
                // Vẽ lớp học
                gc.setFill(fillColor);
                gc.setStroke(borderColor);
                gc.setLineWidth(1);
                gc.fillRoundRect(x + 2, y + 2, rectWidth - 4, rectHeight - 4, 5, 5);
                gc.strokeRoundRect(x + 2, y + 2, rectWidth - 4, rectHeight - 4, 5, 5);
                
                // Vẽ text
                gc.setFill(textColor);
                gc.setFont(Font.font("Segoe UI", 9));
                gc.setTextAlign(TextAlignment.CENTER);
                
                String textContent = lopHoc.getTenMon() + "\n(" + lopHoc.getMaMon() + ")\n" +
                                   "Lớp: " + lopHoc.getMaLop() + "\nGV: " + lopHoc.getTenGiaoVien();
                gc.fillText(textContent, x + rectWidth / 2, y + rectHeight / 2);
            }
        }
    }
}

