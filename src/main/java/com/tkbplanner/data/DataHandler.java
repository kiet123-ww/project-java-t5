package com.tkbplanner.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tkbplanner.constants.Constants;
import com.tkbplanner.models.LichBan;
import com.tkbplanner.models.LopHoc;
import com.tkbplanner.models.MonHoc;
import com.tkbplanner.models.ThoiGianHoc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý lưu và tải dữ liệu từ file JSON
 */
public class DataHandler {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Lưu dữ liệu môn học vào file JSON
     */
    public static boolean saveData(Map<String, MonHoc> allCoursesDict) {
        try {
            Map<String, Object> dataToSave = new HashMap<>();
            for (Map.Entry<String, MonHoc> entry : allCoursesDict.entrySet()) {
                dataToSave.put(entry.getKey(), entry.getValue().toDict());
            }
            
            try (FileWriter writer = new FileWriter(Constants.DATA_FILE, StandardCharsets.UTF_8)) {
                gson.toJson(dataToSave, writer);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tải dữ liệu môn học từ file JSON
     */
    public static Map<String, MonHoc> loadData() {
        File file = new File(Constants.DATA_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject data = gson.fromJson(reader, JsonObject.class);
            if (data == null) {
                return new HashMap<>();
            }
            
            Map<String, MonHoc> loadedCourses = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                JsonObject monData = entry.getValue().getAsJsonObject();
                
                String maMon = monData.get("ma_mon").getAsString();
                String tenMon = monData.get("ten_mon").getAsString();
                
                List<String> tienQuyet = new ArrayList<>();
                if (monData.has("tien_quyet") && monData.get("tien_quyet").isJsonArray()) {
                    JsonArray tienQuyetArray = monData.get("tien_quyet").getAsJsonArray();
                    for (JsonElement elem : tienQuyetArray) {
                        tienQuyet.add(elem.getAsString());
                    }
                }
                
                String colorHex = null;
                if (monData.has("color_hex") && !monData.get("color_hex").isJsonNull()) {
                    colorHex = monData.get("color_hex").getAsString();
                }
                
                MonHoc monHoc = new MonHoc(maMon, tenMon, tienQuyet, colorHex);
                
                if (monData.has("cac_lop_hoc") && monData.get("cac_lop_hoc").isJsonArray()) {
                    JsonArray lopHocArray = monData.get("cac_lop_hoc").getAsJsonArray();
                    for (JsonElement lopElem : lopHocArray) {
                        JsonObject lopData = lopElem.getAsJsonObject();
                        
                        String maLop = lopData.get("ma_lop").getAsString();
                        String tenGiaoVien = lopData.get("ten_giao_vien").getAsString();
                        String lopColorHex = null;
                        if (lopData.has("color_hex") && !lopData.get("color_hex").isJsonNull()) {
                            lopColorHex = lopData.get("color_hex").getAsString();
                        }
                        String loaiLop = "Lớp";
                        if (lopData.has("loai_lop") && !lopData.get("loai_lop").isJsonNull()) {
                            loaiLop = lopData.get("loai_lop").getAsString();
                        }
                        
                        LopHoc lopHoc = new LopHoc(maLop, tenGiaoVien, maMon, tenMon, lopColorHex, loaiLop);
                        
                        if (lopData.has("cac_khung_gio") && lopData.get("cac_khung_gio").isJsonArray()) {
                            JsonArray gioArray = lopData.get("cac_khung_gio").getAsJsonArray();
                            for (JsonElement gioElem : gioArray) {
                                JsonObject gioData = gioElem.getAsJsonObject();
                                int thu = gioData.get("thu").getAsInt();
                                int tietBatDau = gioData.get("tiet_bat_dau").getAsInt();
                                int tietKetThuc = gioData.get("tiet_ket_thuc").getAsInt();
                                lopHoc.themKhungGio(thu, tietBatDau, tietKetThuc);
                            }
                        }
                        
                        monHoc.themLopHoc(lopHoc);
                    }
                }
                
                loadedCourses.put(maMon, monHoc);
            }
            return loadedCourses;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    /**
     * Tạo file JSON trống nếu file dữ liệu chưa tồn tại
     */
    public static void createSampleDataIfNotExists() {
        File file = new File(Constants.DATA_FILE);
        if (file.exists()) {
            return;
        }
        
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(new HashMap<>(), writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Lưu danh sách môn đã học vào file JSON
     */
    public static boolean saveCompletedCourses(List<String> completedCoursesList) {
        try {
            try (FileWriter writer = new FileWriter(Constants.COMPLETED_COURSES_FILE, StandardCharsets.UTF_8)) {
                gson.toJson(completedCoursesList, writer);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tải danh sách môn đã học từ file JSON
     */
    public static List<String> loadCompletedCourses() {
        File file = new File(Constants.COMPLETED_COURSES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonArray data = gson.fromJson(reader, JsonArray.class);
            if (data == null) {
                return new ArrayList<>();
            }
            
            List<String> result = new ArrayList<>();
            for (JsonElement elem : data) {
                result.add(elem.getAsString());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Lưu danh sách giờ bận vào file JSON
     */
    public static boolean saveBusyTimes(List<LichBan> busyTimesList) {
        try {
            List<Map<String, Object>> dataToSave = new ArrayList<>();
            for (LichBan busyTime : busyTimesList) {
                Map<String, Object> item = new HashMap<>();
                item.put("thu", busyTime.getThu());
                item.put("gio_bat_dau", busyTime.getGioBatDau().format(timeFormatter));
                item.put("gio_ket_thuc", busyTime.getGioKetThuc().format(timeFormatter));
                item.put("ly_do", busyTime.getLyDo());
                item.put("id", busyTime.getId());
                dataToSave.add(item);
            }
            
            try (FileWriter writer = new FileWriter(Constants.BUSY_TIMES_FILE, StandardCharsets.UTF_8)) {
                gson.toJson(dataToSave, writer);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tải danh sách giờ bận từ file JSON
     */
    public static List<LichBan> loadBusyTimes() {
        File file = new File(Constants.BUSY_TIMES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonArray data = gson.fromJson(reader, JsonArray.class);
            if (data == null) {
                return new ArrayList<>();
            }
            
            List<LichBan> busyTimes = new ArrayList<>();
            for (JsonElement elem : data) {
                JsonObject item = elem.getAsJsonObject();
                
                int thu = item.get("thu").getAsInt();
                String gioBatDauStr = item.get("gio_bat_dau").getAsString();
                String gioKetThucStr = item.get("gio_ket_thuc").getAsString();
                String lyDo = item.get("ly_do").getAsString();
                double id = item.get("id").getAsDouble();
                
                LocalTime gioBatDau = LocalTime.parse(gioBatDauStr, timeFormatter);
                LocalTime gioKetThuc = LocalTime.parse(gioKetThucStr, timeFormatter);
                
                LichBan busyTime = new LichBan(thu, gioBatDau, gioKetThuc, lyDo, id);
                busyTimes.add(busyTime);
            }
            return busyTimes;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

