package com.tkbplanner.ui;

import com.tkbplanner.constants.Constants;
import com.tkbplanner.data.DataHandler;
import com.tkbplanner.models.LichBan;
import com.tkbplanner.models.LopHoc;
import com.tkbplanner.models.MonHoc;
import com.tkbplanner.scheduler.Scheduler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cửa sổ chính của ứng dụng TKB Planner Pro
 */
public class MainWindow {
    private Stage stage;
    private Scene scene;
    
    // Data
    private Map<String, MonHoc> allCourses;
    private List<LichBan> danhSachGioBan;
    private List<List<LopHoc>> danhSachTkbTimDuoc;
    private int currentTkbIndex;
    private List<String> completedCourses;
    private boolean darkMode;
    
    // UI Components - Left Panel
    private TextField searchInput;
    private Button addSubjectBtn;
    private VBox courseListLayout;
    private ScrollPane courseScrollPane;
    private Map<String, CourseWidget> courseWidgets;
    
    // Busy Times
    private ComboBox<String> busyThuCombo;
    private TextField busyReasonInput;
    private ComboBox<String> busyStartTimeCombo;
    private ComboBox<String> busyEndTimeCombo;
    private Button addBusyBtn;
    private VBox busyListLayout;
    private ScrollPane busyScrollPane;
    private Map<Double, BusyTimeWidget> busyTimeWidgets;
    
    // Notification
    private TextArea notificationBrowser;
    
    // Right Panel
    private ScheduleWidget scheduleView;
    private Button findTkbBtn;
    private Button prevTkbBtn;
    private Button nextTkbBtn;
    private Button saveTkbBtn;
    private Button clearTkbBtn;
    private Label tkbInfoLabel;
    
    // Menu
    private CheckMenuItem toggleThemeMenuItem;
    
    // Inner classes for widgets
    private static class CourseWidget {
        HBox container;
        CheckBox check;
        CheckBox mandatory;
        Button editBtn;
        Button deleteBtn;
    }
    
    private static class BusyTimeWidget {
        HBox container;
        CheckBox check;
        Button deleteBtn;
        double id;
    }
    
    public MainWindow() {
        stage = new Stage();
        stage.setTitle("Công cụ Sắp xếp TKB Pro");
        stage.setWidth(1288);
        stage.setHeight(900);
        
        // Initialize data
        DataHandler.createSampleDataIfNotExists();
        allCourses = DataHandler.loadData();
        danhSachGioBan = DataHandler.loadBusyTimes();
        danhSachTkbTimDuoc = new ArrayList<>();
        currentTkbIndex = -1;
        completedCourses = DataHandler.loadCompletedCourses();
        darkMode = true; // Default dark mode
        courseWidgets = new HashMap<>();
        busyTimeWidgets = new HashMap<>();
        
        setupUI();
        setupMenuBar();
        populateCourseList();
        populateBusyTimes();
        connectSignals();
        applyTheme();
        updateScheduleDisplay();
        logMessage("Chào mừng! Chọn môn học và giờ bận để bắt đầu.");
    }
    
    private void setupUI() {
        BorderPane root = new BorderPane();
        
        // Main horizontal layout
        HBox mainLayout = new HBox(10);
        mainLayout.setPadding(new Insets(10));
        
        // Left Panel
        VBox leftPanel = createLeftPanel();
        leftPanel.setMinWidth(400);
        
        // Right Panel
        VBox rightPanel = createRightPanel();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        mainLayout.getChildren().addAll(leftPanel, rightPanel);
        root.setCenter(mainLayout);
        
        // Status Bar
        Label statusBar = new Label("Sẵn sàng");
        statusBar.setPadding(new Insets(5));
        root.setBottom(statusBar);
        
        scene = new Scene(root, 1288, 900);
        stage.setScene(scene);
        
        // Apply initial theme
        scene.getRoot().getStyleClass().add(darkMode ? "dark-theme" : "light-theme");
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        
        // Course Group
        TitledPane courseGroup = createCourseGroup();
        VBox.setVgrow(courseGroup, Priority.ALWAYS);
        
        // Busy Times Group
        TitledPane busyGroup = createBusyGroup();
        VBox.setVgrow(busyGroup, Priority.ALWAYS);
        
        // Notification Group
        TitledPane notificationGroup = createNotificationGroup();
        
        leftPanel.getChildren().addAll(courseGroup, busyGroup, notificationGroup);
        return leftPanel;
    }
    
    private TitledPane createCourseGroup() {
        VBox content = new VBox(5);
        
        // Search and Add button
        HBox searchLayout = new HBox(5);
        searchLayout.setAlignment(Pos.CENTER_LEFT);
        searchInput = new TextField();
        searchInput.setPromptText("Nhập mã hoặc tên môn để lọc...");
        HBox.setHgrow(searchInput, Priority.ALWAYS);
        addSubjectBtn = new Button("Thêm Môn");
        addSubjectBtn.setMinHeight(30);
        searchLayout.getChildren().addAll(new Label("Nhập môn:"), searchInput, addSubjectBtn);
        
        // Course list
        courseListLayout = new VBox(3);
        courseListLayout.setPadding(new Insets(5));
        courseScrollPane = new ScrollPane(courseListLayout);
        courseScrollPane.setFitToWidth(true);
        courseScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(courseScrollPane, Priority.ALWAYS);
        
        content.getChildren().addAll(searchLayout, courseScrollPane);
        
        TitledPane group = new TitledPane("Chọn môn học", content);
        group.setCollapsible(false);
        return group;
    }
    
    private TitledPane createBusyGroup() {
        VBox content = new VBox(5);
        
        // Row 1: Day and Reason
        HBox row1 = new HBox(5);
        row1.setAlignment(Pos.CENTER_LEFT);
        busyThuCombo = new ComboBox<>();
        busyThuCombo.getItems().addAll(Constants.TEN_THU_TRONG_TUAN.values());
        busyThuCombo.setMinWidth(120);
        busyReasonInput = new TextField();
        busyReasonInput.setPromptText("Nhập lý do bận...");
        HBox.setHgrow(busyReasonInput, Priority.ALWAYS);
        row1.getChildren().addAll(new Label("Thứ:"), busyThuCombo, new Label("Lý do:"), busyReasonInput);
        
        // Row 2: Time and Add button
        HBox row2 = new HBox(5);
        row2.setAlignment(Pos.CENTER_LEFT);
        
        // Create time combos (simplified - using strings for now)
        busyStartTimeCombo = new ComboBox<>();
        busyStartTimeCombo.getItems().addAll(createTimeOptions());
        busyStartTimeCombo.setValue("07:00");
        busyStartTimeCombo.setMinWidth(80);
        
        busyEndTimeCombo = new ComboBox<>();
        busyEndTimeCombo.getItems().addAll(createTimeOptions());
        busyEndTimeCombo.setValue("08:00");
        busyEndTimeCombo.setMinWidth(80);
        
        addBusyBtn = new Button("Thêm");
        addBusyBtn.setMinWidth(70);
        
        row2.getChildren().addAll(
            new Label("Bắt đầu:"), busyStartTimeCombo,
            new Label("Kết thúc:"), busyEndTimeCombo,
            new Region(), addBusyBtn
        );
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        
        // Busy list
        busyListLayout = new VBox(3);
        busyListLayout.setPadding(new Insets(5));
        busyScrollPane = new ScrollPane(busyListLayout);
        busyScrollPane.setFitToWidth(true);
        busyScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(busyScrollPane, Priority.ALWAYS);
        
        content.getChildren().addAll(row1, row2, busyScrollPane);
        
        TitledPane group = new TitledPane("Giờ bận", content);
        group.setCollapsible(false);
        return group;
    }
    
    private List<String> createTimeOptions() {
        List<String> times = new ArrayList<>();
        for (int h = 7; h < 21; h++) {
            for (int m = 0; m < 60; m += 15) {
                times.add(String.format("%02d:%02d", h, m));
            }
        }
        return times;
    }
    
    private TitledPane createNotificationGroup() {
        notificationBrowser = new TextArea();
        notificationBrowser.setEditable(false);
        notificationBrowser.setWrapText(true);
        notificationBrowser.setPrefRowCount(5);
        
        TitledPane group = new TitledPane("Thông báo", notificationBrowser);
        group.setCollapsible(false);
        return group;
    }
    
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        
        // Schedule Widget
        scheduleView = new ScheduleWidget();
        scheduleView.setMinSize(800, 600);
        scheduleView.setOnCellClicked((thu, tiet) -> handleCellClick(thu, tiet));
        VBox.setVgrow(scheduleView, Priority.ALWAYS);
        
        // Control buttons
        HBox buttonLayout = new HBox(5);
        buttonLayout.setAlignment(Pos.CENTER_LEFT);
        
        findTkbBtn = new Button("Tìm TKB hợp lệ");
        prevTkbBtn = new Button("< TKB Trước");
        tkbInfoLabel = new Label("Chưa có thời khóa biểu");
        tkbInfoLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
        nextTkbBtn = new Button("TKB Tiếp >");
        saveTkbBtn = new Button("Lưu TKB");
        clearTkbBtn = new Button("Xoá TKB");
        
        buttonLayout.getChildren().addAll(
            findTkbBtn, prevTkbBtn, tkbInfoLabel, nextTkbBtn,
            new Region(), saveTkbBtn, clearTkbBtn
        );
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        
        rightPanel.getChildren().addAll(
            new TitledPane("Lịch Tuần", scheduleView),
            buttonLayout
        );
        
        return rightPanel;
    }
    
    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem saveDataItem = new MenuItem("Lưu dữ liệu môn học");
        saveDataItem.setOnAction(e -> handleSaveData());
        MenuItem importTkbItem = new MenuItem("Import thời khóa biểu");
        importTkbItem.setOnAction(e -> handleImportTkb());
        MenuItem exitItem = new MenuItem("Thoát");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().addAll(saveDataItem, new SeparatorMenuItem(), importTkbItem, new SeparatorMenuItem(), exitItem);
        
        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem addSubjectItem = new MenuItem("Thêm Môn học");
        addSubjectItem.setOnAction(e -> handleAddSubject());
        MenuItem addClassItem = new MenuItem("Thêm Lớp học");
        addClassItem.setOnAction(e -> handleAddClassDialog(null, null));
        editMenu.getItems().addAll(addSubjectItem, addClassItem);
        
        // View Menu
        Menu viewMenu = new Menu("View");
        MenuItem selectAllItem = new MenuItem("Chọn tất cả các môn");
        selectAllItem.setOnAction(e -> handleSelectAll(true));
        MenuItem deselectAllItem = new MenuItem("Bỏ chọn tất cả");
        deselectAllItem.setOnAction(e -> handleSelectAll(false));
        MenuItem clearAllItem = new MenuItem("Xóa toàn bộ dữ liệu");
        clearAllItem.setOnAction(e -> handleClearAllData());
        toggleThemeMenuItem = new CheckMenuItem("Chế độ tối");
        toggleThemeMenuItem.setSelected(darkMode);
        toggleThemeMenuItem.setOnAction(e -> {
            darkMode = toggleThemeMenuItem.isSelected();
            toggleTheme();
        });
        viewMenu.getItems().addAll(selectAllItem, deselectAllItem, new SeparatorMenuItem(), 
                                  clearAllItem, new SeparatorMenuItem(), toggleThemeMenuItem);
        
        // TKB Menu
        Menu tkbMenu = new Menu("TKB");
        MenuItem findTkbItem = new MenuItem("Tìm TKB hợp lệ");
        findTkbItem.setOnAction(e -> handleFindTkb());
        MenuItem inputCompletedItem = new MenuItem("Nhập môn đã học");
        inputCompletedItem.setOnAction(e -> handleInputCompletedCourses());
        MenuItem viewCompletedItem = new MenuItem("Xem danh sách môn đã học");
        viewCompletedItem.setOnAction(e -> handleViewCompletedCourses());
        tkbMenu.getItems().addAll(findTkbItem, new SeparatorMenuItem(), 
                                 inputCompletedItem, viewCompletedItem);
        
        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("Giới thiệu");
        aboutItem.setOnAction(e -> handleAbout());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, tkbMenu, helpMenu);
        
        ((BorderPane) scene.getRoot()).setTop(menuBar);
    }
    
    private void connectSignals() {
        addSubjectBtn.setOnAction(e -> handleAddSubject());
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> filterCourseList());
        addBusyBtn.setOnAction(e -> handleAddBusyTime());
        findTkbBtn.setOnAction(e -> handleFindTkb());
        prevTkbBtn.setOnAction(e -> showPrevTkb());
        nextTkbBtn.setOnAction(e -> showNextTkb());
        saveTkbBtn.setOnAction(e -> handleSaveTkb());
        clearTkbBtn.setOnAction(e -> handleClearTkb());
    }
    
    private void populateCourseList() {
        // Clear existing widgets
        courseListLayout.getChildren().clear();
        courseWidgets.clear();
        
        // Sort courses by code
        List<MonHoc> sortedCourses = new ArrayList<>(allCourses.values());
        sortedCourses.sort(Comparator.comparing(MonHoc::getMaMon));
        
        for (MonHoc monHoc : sortedCourses) {
            CourseWidget widget = new CourseWidget();
            widget.container = new HBox(5);
            widget.container.setPadding(new Insets(3, 5, 3, 5));
            
            widget.check = new CheckBox(monHoc.getTenMon() + " (" + monHoc.getMaMon() + ")");
            HBox.setHgrow(widget.check, Priority.ALWAYS);
            
            // Handle double click on text to show course classes
            final MonHoc monHocForClick = monHoc;
            widget.check.setOnMouseClicked(e -> {
                // Double click to show course classes dialog
                if (e.getClickCount() == 2) {
                    handleShowCourseClasses(monHocForClick);
                }
            });
            
            // Disable checkbox for completed courses
            if (completedCourses.contains(monHoc.getMaMon())) {
                widget.check.setDisable(true);
                widget.check.setTooltip(new Tooltip("Môn này đã được đánh dấu là đã học"));
            }
            
            widget.mandatory = new CheckBox("Bắt buộc");
            widget.mandatory.setTooltip(new Tooltip("TKB tìm được phải chứa môn này"));
            if (completedCourses.contains(monHoc.getMaMon())) {
                widget.mandatory.setDisable(true);
            }
            
            widget.editBtn = new Button("Sửa");
            widget.editBtn.setMinWidth(55);
            widget.editBtn.setMaxWidth(70);
            final String maMon = monHoc.getMaMon();
            widget.editBtn.setOnAction(e -> handleEditSubject(maMon));
            
            widget.deleteBtn = new Button("Xoá");
            widget.deleteBtn.setMinWidth(55);
            widget.deleteBtn.setMaxWidth(70);
            widget.deleteBtn.setOnAction(e -> handleDeleteCourse(maMon));
            
            widget.container.getChildren().addAll(widget.check, widget.mandatory, widget.editBtn, widget.deleteBtn);
            courseListLayout.getChildren().add(widget.container);
            courseWidgets.put(monHoc.getMaMon(), widget);
        }
    }
    
    private void populateBusyTimes() {
        busyListLayout.getChildren().clear();
        busyTimeWidgets.clear();
        
        for (LichBan busyTime : danhSachGioBan) {
            addBusyTimeWidget(busyTime);
        }
    }
    
    private void filterCourseList() {
        String filterText = searchInput.getText().toLowerCase();
        for (Map.Entry<String, CourseWidget> entry : courseWidgets.entrySet()) {
            MonHoc monHoc = allCourses.get(entry.getKey());
            String textToCheck = (monHoc.getTenMon() + " (" + monHoc.getMaMon() + ")").toLowerCase();
            entry.getValue().container.setVisible(textToCheck.contains(filterText));
        }
    }
    
    private void handleEditSubject(String maMon) {
        MonHoc monHoc = allCourses.get(maMon);
        if (monHoc == null) {
            return;
        }
        
        com.tkbplanner.ui.dialogs.SubjectDialog dialog = new com.tkbplanner.ui.dialogs.SubjectDialog(monHoc);
        Optional<com.tkbplanner.ui.dialogs.SubjectDialog.Result> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            com.tkbplanner.ui.dialogs.SubjectDialog.Result data = result.get();
            if (data != null) {
                monHoc.setTenMon(data.getTenMon());
                monHoc.setTienQuyet(data.getTienQuyet());
                DataHandler.saveData(allCourses);
                populateCourseList();
                logMessage("Đã cập nhật môn học: " + maMon);
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Tên môn không được để trống.");
            }
        }
    }
    
    private void handleDeleteCourse(String maMon) {
        MonHoc monHoc = allCourses.get(maMon);
        if (monHoc == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xoá");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn xoá môn '" + monHoc.getTenMon() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            allCourses.remove(maMon);
            if (completedCourses.contains(maMon)) {
                completedCourses.remove(maMon);
                DataHandler.saveCompletedCourses(completedCourses);
            }
            populateCourseList();
            DataHandler.saveData(allCourses);
            logMessage("Đã xoá môn " + maMon + ".");
        }
    }
    
    private void handleAddSubject() {
        com.tkbplanner.ui.dialogs.SubjectDialog dialog = new com.tkbplanner.ui.dialogs.SubjectDialog(null);
        Optional<com.tkbplanner.ui.dialogs.SubjectDialog.Result> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            com.tkbplanner.ui.dialogs.SubjectDialog.Result data = result.get();
            if (data != null && !allCourses.containsKey(data.getMaMon())) {
                MonHoc newMon = new MonHoc(data.getMaMon(), data.getTenMon(), data.getTienQuyet(), null);
                allCourses.put(data.getMaMon(), newMon);
                populateCourseList();
                DataHandler.saveData(allCourses);
                logMessage("Đã thêm môn học mới: " + data.getMaMon());
            } else if (data == null) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Mã môn và Tên môn không được để trống.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Mã môn '" + data.getMaMon() + "' đã tồn tại.");
            }
        }
    }
    
    private void handleAddClassDialog(Integer defaultThu, Integer defaultTiet) {
        if (allCourses.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa có môn học", 
                     "Vui lòng thêm một môn học trước khi thêm lớp.");
            return;
        }
        
        com.tkbplanner.ui.dialogs.ClassDialog dialog = 
            new com.tkbplanner.ui.dialogs.ClassDialog(allCourses, defaultThu, defaultTiet, null);
        Optional<com.tkbplanner.ui.dialogs.ClassDialog.Result> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            com.tkbplanner.ui.dialogs.ClassDialog.Result data = result.get();
            if (data != null) {
                MonHoc monHoc = allCourses.get(data.getMaMon());
                LopHoc newLop = new LopHoc(data.getMaLop(), data.getTenGv(), 
                                          monHoc.getMaMon(), monHoc.getTenMon(), null, data.getLoaiLop());
                newLop.themKhungGio(data.getThu(), data.getTietBd(), data.getTietKt());
                
                // Check for conflicts
                Scheduler.ValidationResult validation = 
                    Scheduler.kiemTraTrungTrongCungMon(newLop, monHoc, null);
                if (!validation.isValid()) {
                    showAlert(Alert.AlertType.WARNING, "Lỗi", validation.getErrorMessage());
                    return;
                }
                
                if (monHoc.themLopHoc(newLop)) {
                    DataHandler.saveData(allCourses);
                    logMessage("Đã thêm lớp " + data.getMaLop() + " cho môn " + data.getMaMon());
                } else {
                    showAlert(Alert.AlertType.WARNING, "Lỗi", 
                             "Lớp học '" + data.getMaLop() + "' đã tồn tại với cùng giờ học trong môn này.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi", 
                         "Vui lòng điền đủ thông tin và đảm bảo tiết bắt đầu <= tiết kết thúc.");
            }
        }
    }
    
    private void handleSaveData() {
        if (DataHandler.saveData(allCourses)) {
            logMessage("Lưu dữ liệu môn học thành công!");
            showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                     "Đã lưu dữ liệu vào file " + Constants.DATA_FILE);
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu dữ liệu!");
        }
    }
    
    private void handleImportTkb() {
        // TODO: Implement
        logMessage("Tính năng import TKB đang được phát triển...");
    }
    
    private void handleSelectAll(boolean select) {
        for (CourseWidget widget : courseWidgets.values()) {
            widget.check.setSelected(select);
        }
    }
    
    private void handleClearAllData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn xóa TOÀN BỘ môn học và lớp học?\n\nHành động này không thể hoàn tác!");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            allCourses.clear();
            completedCourses.clear();
            DataHandler.saveCompletedCourses(completedCourses);
            danhSachTkbTimDuoc.clear();
            currentTkbIndex = -1;
            danhSachGioBan.clear();
            populateCourseList();
            updateScheduleDisplay();
            updateTkbInfoLabel();
            logMessage("Đã xóa toàn bộ dữ liệu môn học và lớp học.");
            DataHandler.saveData(allCourses);
        }
    }
    
    private void toggleTheme() {
        applyTheme();
        logMessage("Đã chuyển sang chế độ " + (darkMode ? "tối" : "sáng"));
    }
    
    private void handleFindTkb() {
        List<MonHoc> selectedCourses = new ArrayList<>();
        for (Map.Entry<String, CourseWidget> entry : courseWidgets.entrySet()) {
            if (entry.getValue().check.isSelected()) {
                selectedCourses.add(allCourses.get(entry.getKey()));
            }
        }
        
        if (selectedCourses.isEmpty()) {
            logMessage("Vui lòng chọn ít nhất một môn học.");
            return;
        }
        
        List<String> mandatoryCourses = new ArrayList<>();
        for (Map.Entry<String, CourseWidget> entry : courseWidgets.entrySet()) {
            if (entry.getValue().check.isSelected() && entry.getValue().mandatory.isSelected()) {
                mandatoryCourses.add(entry.getKey());
            }
        }
        
        List<LichBan> activeBusyTimes = getActiveBusyTimes();
        logMessage("Đang tìm kiếm TKB...");
        
        Scheduler.ScheduleResult result = Scheduler.timThoiKhoaBieu(
            selectedCourses, activeBusyTimes, mandatoryCourses, completedCourses
        );
        
        if (result.getErrorMessage() != null) {
            logMessage(result.getErrorMessage());
            scheduleView.displaySchedule(new ArrayList<>(), allCourses, activeBusyTimes);
            currentTkbIndex = -1;
            updateTkbInfoLabel();
        } else if (result.getSchedules().isEmpty()) {
            logMessage("Không tìm thấy TKB nào phù hợp.");
            scheduleView.displaySchedule(new ArrayList<>(), allCourses, activeBusyTimes);
            currentTkbIndex = -1;
            updateTkbInfoLabel();
        } else {
            danhSachTkbTimDuoc = result.getSchedules();
            logMessage("Tìm thấy " + danhSachTkbTimDuoc.size() + " TKB phù hợp!");
            showTkbAtIndex(0);
        }
        updateNavButtons();
    }
    
    private void showTkbAtIndex(int index) {
        if (danhSachTkbTimDuoc.isEmpty() || index < 0 || index >= danhSachTkbTimDuoc.size()) {
            return;
        }
        currentTkbIndex = index;
        List<LopHoc> tkb = danhSachTkbTimDuoc.get(index);
        List<LichBan> activeBusyTimes = getActiveBusyTimes();
        scheduleView.displaySchedule(tkb, allCourses, activeBusyTimes);
        updateTkbInfoLabel();
    }
    
    private void showNextTkb() {
        if (danhSachTkbTimDuoc.isEmpty()) {
            return;
        }
        int newIndex = (currentTkbIndex + 1) % danhSachTkbTimDuoc.size();
        showTkbAtIndex(newIndex);
    }
    
    private void showPrevTkb() {
        if (danhSachTkbTimDuoc.isEmpty()) {
            return;
        }
        int newIndex = (currentTkbIndex - 1 + danhSachTkbTimDuoc.size()) % danhSachTkbTimDuoc.size();
        showTkbAtIndex(newIndex);
    }
    
    private void updateNavButtons() {
        boolean hasResults = !danhSachTkbTimDuoc.isEmpty();
        prevTkbBtn.setDisable(!hasResults);
        nextTkbBtn.setDisable(!hasResults);
        saveTkbBtn.setDisable(!hasResults);
        clearTkbBtn.setDisable(!hasResults);
    }
    
    private void handleClearTkb() {
        danhSachTkbTimDuoc.clear();
        currentTkbIndex = -1;
        List<LichBan> activeBusyTimes = getActiveBusyTimes();
        scheduleView.displaySchedule(new ArrayList<>(), allCourses, activeBusyTimes);
        updateTkbInfoLabel();
        logMessage("Đã xoá kết quả tìm kiếm TKB.");
        updateNavButtons();
    }
    
    private void handleSaveTkb() {
        if (currentTkbIndex == -1 || danhSachTkbTimDuoc.isEmpty()) {
            logMessage("Không có TKB để lưu.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Thời khóa biểu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                List<LopHoc> tkb = danhSachTkbTimDuoc.get(currentTkbIndex);
                writer.write("THỜI KHÓA BIỂU SỐ " + (currentTkbIndex + 1) + "\n");
                writer.write(String.join("", Collections.nCopies(30, "=")) + "\n\n");
                
                // Sort by day and time
                tkb.sort((a, b) -> {
                    int aThu = a.getCacKhungGio().isEmpty() ? 0 : a.getCacKhungGio().get(0).getThu();
                    int bThu = b.getCacKhungGio().isEmpty() ? 0 : b.getCacKhungGio().get(0).getThu();
                    if (aThu != bThu) return Integer.compare(aThu, bThu);
                    int aTiet = a.getCacKhungGio().isEmpty() ? 0 : a.getCacKhungGio().get(0).getTietBatDau();
                    int bTiet = b.getCacKhungGio().isEmpty() ? 0 : b.getCacKhungGio().get(0).getTietBatDau();
                    return Integer.compare(aTiet, bTiet);
                });
                
                for (LopHoc lop : tkb) {
                    writer.write("Môn: " + lop.getTenMon() + " (" + lop.getMaMon() + ")\n");
                    writer.write("  Lớp: " + lop.getMaLop() + "\n");
                    writer.write("  GV: " + lop.getTenGiaoVien() + "\n");
                    for (com.tkbplanner.models.ThoiGianHoc gio : lop.getCacKhungGio()) {
                        String tenThu = Constants.TEN_THU_TRONG_TUAN.getOrDefault(gio.getThu(), "Không rõ");
                        writer.write("  Thời gian: " + tenThu + ", Tiết " + 
                                   gio.getTietBatDau() + "-" + gio.getTietKetThuc() + "\n");
                    }
                    writer.write("\n");
                }
                logMessage("Đã lưu TKB thành công vào: " + file.getAbsolutePath());
            } catch (IOException e) {
                logMessage("Lỗi khi lưu TKB: " + e.getMessage());
            }
        }
    }
    
    private void handleAddBusyTime() {
        String tenThu = busyThuCombo.getValue();
        if (tenThu == null) {
            logMessage("Vui lòng chọn thứ.");
            return;
        }
        
        int thu = Constants.TEN_THU_TRONG_TUAN.entrySet().stream()
            .filter(e -> e.getValue().equals(tenThu))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(2);
        
        LocalTime startTime = LocalTime.parse(busyStartTimeCombo.getValue());
        LocalTime endTime = LocalTime.parse(busyEndTimeCombo.getValue());
        String reason = busyReasonInput.getText().isEmpty() ? "Bận" : busyReasonInput.getText();
        
        if (!startTime.isBefore(endTime)) {
            logMessage("Lỗi: Giờ bắt đầu phải trước giờ kết thúc.");
            return;
        }
        
        double busyId = System.currentTimeMillis();
        LichBan newBusyTime = new LichBan(thu, startTime, endTime, reason, busyId);
        danhSachGioBan.add(newBusyTime);
        
        addBusyTimeWidget(newBusyTime);
        DataHandler.saveBusyTimes(danhSachGioBan);
        updateScheduleDisplay();
        logMessage("Đã thêm giờ bận: " + newBusyTime);
    }
    
    private void addBusyTimeWidget(LichBan busyTime) {
        BusyTimeWidget widget = new BusyTimeWidget();
        widget.id = busyTime.getId();
        widget.container = new HBox(5);
        widget.container.setPadding(new Insets(3, 5, 3, 5));
        
        widget.check = new CheckBox(busyTime.toString());
        widget.check.setSelected(true);
        HBox.setHgrow(widget.check, Priority.ALWAYS);
        widget.check.selectedProperty().addListener((obs, oldVal, newVal) -> updateScheduleDisplay());
        
        widget.deleteBtn = new Button("Xoá");
        widget.deleteBtn.setMinWidth(55);
        widget.deleteBtn.setMaxWidth(70);
        widget.deleteBtn.setOnAction(e -> handleDeleteBusyTime(busyTime.getId()));
        
        widget.container.getChildren().addAll(widget.check, widget.deleteBtn);
        busyListLayout.getChildren().add(widget.container);
        busyTimeWidgets.put(busyTime.getId(), widget);
    }
    
    private void handleDeleteBusyTime(double busyId) {
        danhSachGioBan.removeIf(b -> b.getId() == busyId);
        BusyTimeWidget widget = busyTimeWidgets.remove(busyId);
        if (widget != null) {
            busyListLayout.getChildren().remove(widget.container);
        }
        DataHandler.saveBusyTimes(danhSachGioBan);
        updateScheduleDisplay();
        logMessage("Đã xoá một giờ bận.");
    }
    
    private List<LichBan> getActiveBusyTimes() {
        List<LichBan> active = new ArrayList<>();
        for (BusyTimeWidget widget : busyTimeWidgets.values()) {
            if (widget.check.isSelected()) {
                active.add(danhSachGioBan.stream()
                    .filter(b -> b.getId() == widget.id)
                    .findFirst()
                    .orElse(null));
            }
        }
        active.removeIf(Objects::isNull);
        return active;
    }
    
    private void updateScheduleDisplay() {
        List<LichBan> activeBusyTimes = getActiveBusyTimes();
        if (currentTkbIndex >= 0 && !danhSachTkbTimDuoc.isEmpty()) {
            List<LopHoc> tkb = danhSachTkbTimDuoc.get(currentTkbIndex);
            scheduleView.displaySchedule(tkb, allCourses, activeBusyTimes);
        } else {
            scheduleView.displaySchedule(new ArrayList<>(), allCourses, activeBusyTimes);
        }
    }
    
    private void updateTkbInfoLabel() {
        if (danhSachTkbTimDuoc.isEmpty() || currentTkbIndex == -1) {
            tkbInfoLabel.setText("Chưa có thời khóa biểu");
        } else {
            tkbInfoLabel.setText("Thời khóa biểu: " + (currentTkbIndex + 1) + "/" + danhSachTkbTimDuoc.size());
        }
    }
    
    private void handleInputCompletedCourses() {
        com.tkbplanner.ui.dialogs.CompletedCoursesDialog dialog = 
            new com.tkbplanner.ui.dialogs.CompletedCoursesDialog(
                allCourses, completedCourses, 
                () -> {
                    // Callback to add new course
                    com.tkbplanner.ui.dialogs.SubjectDialog subjectDialog = 
                        new com.tkbplanner.ui.dialogs.SubjectDialog(null);
                    java.util.Optional<com.tkbplanner.ui.dialogs.SubjectDialog.Result> result = 
                        subjectDialog.showAndWait();
                    if (result.isPresent() && result.get() != null) {
                        com.tkbplanner.ui.dialogs.SubjectDialog.Result data = result.get();
                        if (!allCourses.containsKey(data.getMaMon())) {
                            MonHoc newMon = new MonHoc(data.getMaMon(), data.getTenMon(), 
                                                      data.getTienQuyet(), null);
                            allCourses.put(data.getMaMon(), newMon);
                            populateCourseList();
                            DataHandler.saveData(allCourses);
                            logMessage("Đã thêm môn học mới: " + data.getMaMon());
                            return data.getMaMon();
                        }
                    }
                    return null;
                }
            );
        
        java.util.Optional<List<String>> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            completedCourses = result.get();
            if (DataHandler.saveCompletedCourses(completedCourses)) {
                logMessage("Đã lưu " + completedCourses.size() + " môn đã học.");
                populateCourseList();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                         "Đã lưu " + completedCourses.size() + " môn đã học.");
            } else {
                logMessage("Lỗi khi lưu danh sách môn đã học.");
            }
        }
    }
    
    private void handleViewCompletedCourses() {
        com.tkbplanner.ui.dialogs.ViewCompletedCoursesDialog dialog = 
            new com.tkbplanner.ui.dialogs.ViewCompletedCoursesDialog(allCourses, completedCourses);
        
        java.util.Optional<List<String>> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            List<String> updated = result.get();
            if (!updated.equals(completedCourses)) {
                completedCourses = updated;
                if (DataHandler.saveCompletedCourses(completedCourses)) {
                    logMessage("Đã cập nhật danh sách môn đã học: " + completedCourses.size() + " môn.");
                    populateCourseList();
                } else {
                    logMessage("Lỗi khi lưu danh sách môn đã học.");
                }
            }
        }
    }
    
    private void handleCellClick(int thu, int tiet) {
        String tenThu = Constants.TEN_THU_TRONG_TUAN.getOrDefault(thu, "Thứ " + thu);
        logMessage("Chọn thêm lớp mới vào " + tenThu + ", Tiết " + tiet + "...");
        
        if (allCourses.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa có môn học", 
                     "Vui lòng thêm một môn học trước khi thêm lớp.");
            return;
        }
        
        handleAddClassDialog(thu, tiet);
    }
    
    private void handleShowCourseClasses(MonHoc monHoc) {
        com.tkbplanner.ui.dialogs.CourseClassesDialog dialog = 
            new com.tkbplanner.ui.dialogs.CourseClassesDialog(monHoc, allCourses);
        
        Optional<List<LopHoc>> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            List<LopHoc> deletedClasses = result.get();
            for (LopHoc lop : deletedClasses) {
                logMessage("Đã xóa lớp " + lop.getMaLop() + " khỏi môn " + monHoc.getMaMon());
            }
            if (!deletedClasses.isEmpty()) {
                DataHandler.saveData(allCourses);
                logMessage("Đã cập nhật danh sách lớp học của môn " + monHoc.getMaMon());
            }
        }
    }
    
    private void handleAbout() {
        showAlert(Alert.AlertType.INFORMATION, "Giới thiệu", 
                 "Công cụ Sắp xếp TKB Pro\n\nPhiên bản 3.0 (Java Edition)");
    }
    
    private void applyTheme() {
        scheduleView.setDarkMode(darkMode);
        
        // Apply CSS theme
        try {
            String themeClass = darkMode ? "dark-theme" : "light-theme";
            scene.getRoot().getStyleClass().removeAll("dark-theme", "light-theme");
            scene.getRoot().getStyleClass().add(themeClass);
            
            // Load CSS if available
            try {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(
                    getClass().getResource("/styles.css").toExternalForm()
                );
            } catch (Exception e) {
                // CSS file not found, use inline styles
            }
        } catch (Exception e) {
            // Theme application failed, continue without theme
        }
    }
    
    private void logMessage(String message) {
        String timestamp = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        notificationBrowser.appendText("[" + timestamp + "] " + message + "\n");
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public void show() {
        stage.show();
    }
    
    public void close() {
        Platform.exit();
    }
}
