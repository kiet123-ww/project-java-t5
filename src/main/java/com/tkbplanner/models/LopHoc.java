package com.tkbplanner.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đại diện cho một lớp học cụ thể
 */
public class LopHoc {
    private String maLop;
    private String tenGiaoVien;
    private String maMon;
    private String tenMon;
    private List<ThoiGianHoc> cacKhungGio;
    private String colorHex;
    private String loaiLop;  // "Lý thuyết", "Bài tập", hoặc "Lớp"
    
    public LopHoc(String maLop, String tenGiaoVien, String maMon, String tenMon, String colorHex, String loaiLop) {
        this.maLop = maLop;
        this.tenGiaoVien = tenGiaoVien;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.cacKhungGio = new ArrayList<>();
        this.colorHex = colorHex != null ? colorHex : "#ADD8E6";
        this.loaiLop = loaiLop != null ? loaiLop : "Lớp";
    }
    
    public LopHoc(String maLop, String tenGiaoVien, String maMon, String tenMon) {
        this(maLop, tenGiaoVien, maMon, tenMon, null, "Lớp");
    }
    
    public void themKhungGio(int thu, int tietBatDau, int tietKetThuc) {
        cacKhungGio.add(new ThoiGianHoc(thu, tietBatDau, tietKetThuc));
    }
    
    public String getMaLop() {
        return maLop;
    }
    
    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }
    
    public String getTenGiaoVien() {
        return tenGiaoVien;
    }
    
    public void setTenGiaoVien(String tenGiaoVien) {
        this.tenGiaoVien = tenGiaoVien;
    }
    
    public String getMaMon() {
        return maMon;
    }
    
    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }
    
    public String getTenMon() {
        return tenMon;
    }
    
    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }
    
    public List<ThoiGianHoc> getCacKhungGio() {
        return cacKhungGio;
    }
    
    public void setCacKhungGio(List<ThoiGianHoc> cacKhungGio) {
        this.cacKhungGio = cacKhungGio;
    }
    
    public String getColorHex() {
        return colorHex;
    }
    
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
    
    public String getLoaiLop() {
        return loaiLop;
    }
    
    public void setLoaiLop(String loaiLop) {
        this.loaiLop = loaiLop;
    }
    
    /**
     * Trả về ID duy nhất của lớp (ma_mon-ma_lop)
     */
    public String getId() {
        return maMon + "-" + maLop;
    }
    
    public Map<String, Object> toDict() {
        Map<String, Object> dict = new HashMap<>();
        dict.put("ma_lop", maLop);
        dict.put("ten_giao_vien", tenGiaoVien);
        dict.put("ma_mon", maMon);
        dict.put("ten_mon", tenMon);
        List<Map<String, Object>> gioList = new ArrayList<>();
        for (ThoiGianHoc gio : cacKhungGio) {
            gioList.add(gio.toDict());
        }
        dict.put("cac_khung_gio", gioList);
        dict.put("color_hex", colorHex);
        dict.put("loai_lop", loaiLop);
        return dict;
    }
    
    @Override
    public String toString() {
        StringBuilder gioStr = new StringBuilder();
        for (int i = 0; i < cacKhungGio.size(); i++) {
            if (i > 0) gioStr.append(", ");
            gioStr.append(cacKhungGio.get(i).toString());
        }
        return String.format("(%s) %s - %s", maLop, tenGiaoVien, gioStr.toString());
    }
}

