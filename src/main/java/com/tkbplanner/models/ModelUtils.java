package com.tkbplanner.models;

/**
 * Các hàm tiện ích cho models
 */
public class ModelUtils {
    
    /**
     * Chuẩn hóa mã lớp: Chữ đầu phải viết hoa
     * Ví dụ: a704 -> A704, A704 -> A704
     */
    public static String chuanHoaMaLop(String maLop) {
        if (maLop == null || maLop.isEmpty()) {
            return maLop;
        }
        maLop = maLop.trim();
        if (maLop.length() > 0) {
            if (maLop.length() > 1) {
                return maLop.substring(0, 1).toUpperCase() + maLop.substring(1).toLowerCase();
            } else {
                return maLop.substring(0, 1).toUpperCase();
            }
        }
        return maLop;
    }
    
    /**
     * Chuẩn hóa tên giáo viên: Các chữ cái đầu của mỗi từ phải viết hoa
     * Ví dụ: nguyễn văn a -> Nguyễn Văn A
     */
    public static String chuanHoaTenGiaoVien(String tenGv) {
        if (tenGv == null || tenGv.isEmpty()) {
            return tenGv;
        }
        String[] words = tenGv.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            if (!words[i].isEmpty()) {
                result.append(words[i].substring(0, 1).toUpperCase());
                if (words[i].length() > 1) {
                    result.append(words[i].substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }
}

