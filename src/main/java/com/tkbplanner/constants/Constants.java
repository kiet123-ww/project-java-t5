package com.tkbplanner.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Các hằng số dùng trong ứng dụng
 */
public class Constants {
    
    // Tên các thứ trong tuần (2=Thứ 2, 3=Thứ 3, ..., 8=Chủ Nhật)
    public static final Map<Integer, String> TEN_THU_TRONG_TUAN = new HashMap<>();
    
    static {
        TEN_THU_TRONG_TUAN.put(2, "Thứ 2");
        TEN_THU_TRONG_TUAN.put(3, "Thứ 3");
        TEN_THU_TRONG_TUAN.put(4, "Thứ 4");
        TEN_THU_TRONG_TUAN.put(5, "Thứ 5");
        TEN_THU_TRONG_TUAN.put(6, "Thứ 6");
        TEN_THU_TRONG_TUAN.put(7, "Thứ 7");
        TEN_THU_TRONG_TUAN.put(8, "Chủ Nhật");
    }
    
    // Tên file lưu dữ liệu
    public static final String DATA_FILE = "data_TKB_pro.json";
    // Tên file lưu môn đã học
    public static final String COMPLETED_COURSES_FILE = "completed_courses.json";
    // Tên file lưu giờ bận
    public static final String BUSY_TIMES_FILE = "busy_times.json";
    
    private Constants() {
        // Utility class
    }
}

