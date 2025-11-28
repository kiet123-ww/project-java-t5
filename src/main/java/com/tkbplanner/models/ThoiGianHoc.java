package com.tkbplanner.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Đại diện cho một khung giờ học (thứ, tiết bắt đầu, tiết kết thúc)
 */
public class ThoiGianHoc {
    private int thu;
    private int tietBatDau;
    private int tietKetThuc;
    
    public ThoiGianHoc(int thu, int tietBatDau, int tietKetThuc) {
        this.thu = thu;
        this.tietBatDau = tietBatDau;
        this.tietKetThuc = tietKetThuc;
    }
    
    public int getThu() {
        return thu;
    }
    
    public void setThu(int thu) {
        this.thu = thu;
    }
    
    public int getTietBatDau() {
        return tietBatDau;
    }
    
    public void setTietBatDau(int tietBatDau) {
        this.tietBatDau = tietBatDau;
    }
    
    public int getTietKetThuc() {
        return tietKetThuc;
    }
    
    public void setTietKetThuc(int tietKetThuc) {
        this.tietKetThuc = tietKetThuc;
    }
    
    public Map<String, Object> toDict() {
        Map<String, Object> dict = new HashMap<>();
        dict.put("thu", thu);
        dict.put("tiet_bat_dau", tietBatDau);
        dict.put("tiet_ket_thuc", tietKetThuc);
        return dict;
    }
    
    @Override
    public String toString() {
        return String.format("[Thứ %d, Tiết %d-%d]", thu, tietBatDau, tietKetThuc);
    }
}

