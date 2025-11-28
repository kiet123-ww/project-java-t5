package com.tkbplanner.models;

import com.tkbplanner.scheduler.Scheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đại diện cho một môn học với các lớp học của nó
 */
public class MonHoc {
    private String maMon;
    private String tenMon;
    private List<LopHoc> cacLopHoc;
    private Map<String, LopHoc> cacLopHocDict;
    private List<String> tienQuyet;
    private String colorHex;
    
    public MonHoc(String maMon, String tenMon, List<String> tienQuyet, String colorHex) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.cacLopHoc = new ArrayList<>();
        this.cacLopHocDict = new HashMap<>();
        this.tienQuyet = tienQuyet != null ? tienQuyet : new ArrayList<>();
        this.colorHex = colorHex;
    }
    
    public MonHoc(String maMon, String tenMon) {
        this(maMon, tenMon, null, null);
    }
    
    /**
     * Thêm một lớp học vào môn học
     */
    public boolean themLopHoc(LopHoc lopHoc) {
        // Kiểm tra xem có lớp nào cùng môn, cùng phòng và trùng giờ không
        String lopId = lopHoc.getId();
        
        // Kiểm tra tất cả các lớp có cùng ID (cùng môn, cùng phòng)
        if (cacLopHocDict.containsKey(lopId)) {
            LopHoc lopCu = cacLopHocDict.get(lopId);
            // Nếu trùng giờ, không cho thêm (giữ nguyên lớp cũ)
            if (Scheduler.checkTrungLich(lopHoc, lopCu)) {
                return false;
            }
        }
        
        // Kiểm tra tất cả các lớp trong danh sách (có thể có nhiều lớp cùng ID nhưng khác giờ)
        for (LopHoc lopDaCo : cacLopHoc) {
            if (lopDaCo.getId().equals(lopId)) {
                // Nếu trùng giờ, không cho thêm
                if (Scheduler.checkTrungLich(lopHoc, lopDaCo)) {
                    return false;
                }
            }
        }
        
        // Nếu chưa có lớp với cùng ID, hoặc có nhưng không trùng giờ, thêm lớp mới
        if (colorHex != null && lopHoc.getColorHex() == null) {
            lopHoc.setColorHex(colorHex);
        }
        
        // Thêm vào danh sách
        cacLopHoc.add(lopHoc);
        
        // Cập nhật dict (nếu chưa có, hoặc nếu lớp mới có giờ học khác)
        if (!cacLopHocDict.containsKey(lopId)) {
            cacLopHocDict.put(lopId, lopHoc);
        }
        
        return true;
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
    
    public List<LopHoc> getCacLopHoc() {
        return cacLopHoc;
    }
    
    public void setCacLopHoc(List<LopHoc> cacLopHoc) {
        this.cacLopHoc = cacLopHoc;
    }
    
    public Map<String, LopHoc> getCacLopHocDict() {
        return cacLopHocDict;
    }
    
    public void setCacLopHocDict(Map<String, LopHoc> cacLopHocDict) {
        this.cacLopHocDict = cacLopHocDict;
    }
    
    public List<String> getTienQuyet() {
        return tienQuyet;
    }
    
    public void setTienQuyet(List<String> tienQuyet) {
        this.tienQuyet = tienQuyet;
    }
    
    public String getColorHex() {
        return colorHex;
    }
    
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
    
    public Map<String, Object> toDict() {
        Map<String, Object> dict = new HashMap<>();
        dict.put("ma_mon", maMon);
        dict.put("ten_mon", tenMon);
        List<Map<String, Object>> lopList = new ArrayList<>();
        for (LopHoc lop : cacLopHoc) {
            lopList.add(lop.toDict());
        }
        dict.put("cac_lop_hoc", lopList);
        dict.put("tien_quyet", tienQuyet);
        dict.put("color_hex", colorHex);
        return dict;
    }
}

