package com.tkbplanner.models;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Đại diện cho một khoảng thời gian bận (không thể học)
 */
public class LichBan {
    private int thu;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String lyDo;
    private double id;
    
    public LichBan(int thu, LocalTime gioBatDau, LocalTime gioKetThuc, String lyDo, double id) {
        this.thu = thu;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.lyDo = lyDo;
        this.id = id;
    }
    
    public int getThu() {
        return thu;
    }
    
    public void setThu(int thu) {
        this.thu = thu;
    }
    
    public LocalTime getGioBatDau() {
        return gioBatDau;
    }
    
    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }
    
    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }
    
    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }
    
    public String getLyDo() {
        return lyDo;
    }
    
    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }
    
    public double getId() {
        return id;
    }
    
    public void setId(double id) {
        this.id = id;
    }
    
    /**
     * Chuyển đổi LocalTime sang số tiết (1-13) dựa vào lịch thực tế
     */
    public static int timeToTiet(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int totalMinutes = hour * 60 + minute;
        
        // Ca Sáng
        if (420 <= totalMinutes && totalMinutes < 430) {  // 07:00 - 07:50
            return 1;
        }
        if (475 <= totalMinutes && totalMinutes < 525) {  // 07:55 - 08:45
            return 2;
        }
        if (530 <= totalMinutes && totalMinutes < 580) {  // 08:50 - 09:40
            return 3;
        }
        if (590 <= totalMinutes && totalMinutes < 640) {  // 09:50 - 10:40
            return 4;
        }
        if (645 <= totalMinutes && totalMinutes < 695) {  // 10:45 - 11:35
            return 5;
        }
        if (700 <= totalMinutes && totalMinutes < 750) {  // 11:40 - 12:30
            return 6;
        }
        
        // Ca Chiều
        if (810 <= totalMinutes && totalMinutes < 860) {  // 13:30 - 14:20
            return 7;
        }
        if (865 <= totalMinutes && totalMinutes < 915) {  // 14:25 - 15:15
            return 8;
        }
        if (920 <= totalMinutes && totalMinutes < 970) {  // 15:20 - 16:10
            return 9;
        }
        if (980 <= totalMinutes && totalMinutes < 1030) {  // 16:20 - 17:10
            return 10;
        }
        if (1035 <= totalMinutes && totalMinutes < 1085) {  // 17:15 - 18:05
            return 11;
        }
        
        // Ca Tối
        if (1100 <= totalMinutes && totalMinutes < 1150) {  // 18:20 - 19:10
            return 12;
        }
        if (1155 <= totalMinutes && totalMinutes < 1205) {  // 19:15 - 20:05
            return 13;
        }
        
        return -1;
    }
    
    /**
     * Tìm tiết gần nhất với thời gian cho trước
     */
    public static int findNearestTiet(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int totalMinutes = hour * 60 + minute;
        
        // Danh sách thời gian bắt đầu của các tiết (tính bằng phút từ 00:00)
        int[][] tietTimes = {
            {420, 1},   // 07:00 - Tiết 1
            {475, 2},   // 07:55 - Tiết 2
            {530, 3},   // 08:50 - Tiết 3
            {590, 4},   // 09:50 - Tiết 4
            {645, 5},   // 10:45 - Tiết 5
            {700, 6},   // 11:40 - Tiết 6
            {810, 7},   // 13:30 - Tiết 7
            {865, 8},   // 14:25 - Tiết 8
            {920, 9},   // 15:20 - Tiết 9
            {980, 10},  // 16:20 - Tiết 10
            {1035, 11}, // 17:15 - Tiết 11
            {1100, 12}, // 18:20 - Tiết 12
            {1155, 13}, // 19:15 - Tiết 13
        };
        
        // Tìm tiết gần nhất
        int minDiff = Integer.MAX_VALUE;
        int nearestTiet = -1;
        
        for (int[] tietTime : tietTimes) {
            int diff = Math.abs(totalMinutes - tietTime[0]);
            if (diff < minDiff) {
                minDiff = diff;
                nearestTiet = tietTime[1];
            }
        }
        
        return nearestTiet;
    }
    
    /**
     * Chuyển đổi LichBan sang ThoiGianHoc
     */
    public ThoiGianHoc toThoiGianHoc() {
        int tietBd = timeToTiet(gioBatDau);
        int tietKt = timeToTiet(gioKetThuc);
        if (tietBd != -1 && tietKt != -1) {
            return new ThoiGianHoc(thu, tietBd, tietKt);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("Thứ %d (%02d:%02d-%02d:%02d) - %s", 
            thu, gioBatDau.getHour(), gioBatDau.getMinute(),
            gioKetThuc.getHour(), gioKetThuc.getMinute(), lyDo);
    }
}

