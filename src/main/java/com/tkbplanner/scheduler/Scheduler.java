package com.tkbplanner.scheduler;

import com.tkbplanner.constants.Constants;
import com.tkbplanner.models.LichBan;
import com.tkbplanner.models.LopHoc;
import com.tkbplanner.models.MonHoc;
import com.tkbplanner.models.ThoiGianHoc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Logic xử lý tìm kiếm và kiểm tra xung đột thời khóa biểu
 */
public class Scheduler {
    
    /**
     * Kiểm tra xem hai khung giờ có xung đột không
     */
    public static boolean kiemTraXungDotGio(ThoiGianHoc gioA, ThoiGianHoc gioB) {
        if (gioA == null || gioB == null || gioA.getThu() != gioB.getThu()) {
            return false;
        }
        return !(gioA.getTietKetThuc() < gioB.getTietBatDau() || 
                 gioA.getTietBatDau() > gioB.getTietKetThuc());
    }
    
    /**
     * Kiểm tra xem lớp A có trùng lịch với lớp B hoặc lịch bận không
     */
    public static boolean checkTrungLich(LopHoc lopA, Object lopBOrLichBan) {
        List<ThoiGianHoc> cacKhungGioB = new ArrayList<>();
        
        if (lopBOrLichBan instanceof LopHoc) {
            cacKhungGioB = ((LopHoc) lopBOrLichBan).getCacKhungGio();
        } else if (lopBOrLichBan instanceof LichBan) {
            LichBan lichBan = (LichBan) lopBOrLichBan;
            ThoiGianHoc gioBanConverted = lichBan.toThoiGianHoc();
            if (gioBanConverted != null) {
                cacKhungGioB.add(gioBanConverted);
            }
        }
        
        for (ThoiGianHoc gioA : lopA.getCacKhungGio()) {
            for (ThoiGianHoc gioB : cacKhungGioB) {
                if (kiemTraXungDotGio(gioA, gioB)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra xem lớp mới có trùng với lịch hiện tại hoặc giờ bận không
     */
    private static boolean kiemTraTrungVoiLich(LopHoc lopMoi, List<LopHoc> lichHienTai, List<LichBan> danhSachGioBan) {
        for (LopHoc lopDaChon : lichHienTai) {
            if (checkTrungLich(lopMoi, lopDaChon)) {
                return true;
            }
        }
        for (LichBan gioBan : danhSachGioBan) {
            if (checkTrungLich(lopMoi, gioBan)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Hàm đệ quy để tìm tất cả các thời khóa biểu hợp lệ
     */
    private static void timKiemDeQuy(List<MonHoc> danhSachMonHoc, int monHocIndex, 
                                     List<LopHoc> lichHienTai, List<List<LopHoc>> ketQua, 
                                     List<LichBan> danhSachGioBan) {
        if (monHocIndex == danhSachMonHoc.size()) {
            ketQua.add(new ArrayList<>(lichHienTai));
            return;
        }
        
        MonHoc monHienTai = danhSachMonHoc.get(monHocIndex);
        if (monHienTai.getCacLopHoc().isEmpty()) {
            timKiemDeQuy(danhSachMonHoc, monHocIndex + 1, lichHienTai, ketQua, danhSachGioBan);
            return;
        }
        
        for (LopHoc lopHoc : monHienTai.getCacLopHoc()) {
            if (!kiemTraTrungVoiLich(lopHoc, lichHienTai, danhSachGioBan)) {
                lichHienTai.add(lopHoc);
                timKiemDeQuy(danhSachMonHoc, monHocIndex + 1, lichHienTai, ketQua, danhSachGioBan);
                lichHienTai.remove(lichHienTai.size() - 1);
            }
        }
    }
    
    /**
     * Tìm tất cả các thời khóa biểu hợp lệ từ danh sách môn học
     */
    public static ScheduleResult timThoiKhoaBieu(List<MonHoc> danhSachMonHoc, List<LichBan> danhSachGioBan, 
                                                 List<String> monBatBuoc, List<String> completedCourses) {
        // Kiểm tra môn tiên quyết
        Set<String> completedCoursesSet = completedCourses != null ? new HashSet<>(completedCourses) : new HashSet<>();
        for (MonHoc mon : danhSachMonHoc) {
            for (String monTienQuyet : mon.getTienQuyet()) {
                if (!completedCoursesSet.contains(monTienQuyet)) {
                    String errorMsg = String.format(
                        "Lỗi: Môn '%s (%s)' yêu cầu phải học môn tiên quyết '%s' trước. " +
                        "Vui lòng thêm môn '%s' vào danh sách môn đã học.",
                        mon.getTenMon(), mon.getMaMon(), monTienQuyet, monTienQuyet
                    );
                    return new ScheduleResult(new ArrayList<>(), errorMsg);
                }
            }
        }
        
        // Tìm tất cả các TKB hợp lệ
        List<List<LopHoc>> ketQuaThuan = new ArrayList<>();
        timKiemDeQuy(danhSachMonHoc, 0, new ArrayList<>(), ketQuaThuan, danhSachGioBan);
        
        // Nếu không có môn bắt buộc, trả về tất cả kết quả
        if (monBatBuoc == null || monBatBuoc.isEmpty()) {
            return new ScheduleResult(ketQuaThuan, null);
        }
        
        // Lọc các TKB có chứa tất cả môn bắt buộc
        List<List<LopHoc>> ketQuaDaLoc = new ArrayList<>();
        Set<String> maMonBatBuoc = new HashSet<>(monBatBuoc);
        for (List<LopHoc> tkb : ketQuaThuan) {
            Set<String> maMonTrongTkb = new HashSet<>();
            for (LopHoc lop : tkb) {
                maMonTrongTkb.add(lop.getMaMon());
            }
            if (maMonTrongTkb.containsAll(maMonBatBuoc)) {
                ketQuaDaLoc.add(tkb);
            }
        }
        
        return new ScheduleResult(ketQuaDaLoc, null);
    }
    
    /**
     * Kiểm tra xem lớp mới có trùng phòng học và trùng giờ với lớp khác không
     */
    public static ValidationResult kiemTraTrungPhongHoc(LopHoc lopMoi, java.util.Map<String, MonHoc> allCourses, String excludeLopId) {
        // Tìm tất cả các lớp có cùng phòng học (ma_lop)
        List<LopHoc> cacLopCungPhong = new ArrayList<>();
        for (MonHoc monHoc : allCourses.values()) {
            for (LopHoc lop : monHoc.getCacLopHoc()) {
                // Bỏ qua lớp hiện tại nếu đang sửa
                if (excludeLopId != null && lop.getId().equals(excludeLopId)) {
                    continue;
                }
                if (lop.getMaLop().equals(lopMoi.getMaLop())) {
                    cacLopCungPhong.add(lop);
                }
            }
        }
        
        // Nếu không có lớp nào cùng phòng, cho phép thêm
        if (cacLopCungPhong.isEmpty()) {
            return new ValidationResult(true, null);
        }
        
        // Kiểm tra xem có trùng giờ trong cùng 1 ngày không
        List<String> cacThuTrung = new ArrayList<>();
        for (LopHoc lopCungPhong : cacLopCungPhong) {
            for (ThoiGianHoc gioMoi : lopMoi.getCacKhungGio()) {
                for (ThoiGianHoc gioCu : lopCungPhong.getCacKhungGio()) {
                    // Nếu cùng thứ và trùng giờ
                    if (gioMoi.getThu() == gioCu.getThu()) {
                        if (kiemTraXungDotGio(gioMoi, gioCu)) {
                            String tenThu = Constants.TEN_THU_TRONG_TUAN.getOrDefault(gioMoi.getThu(), "Thứ " + gioMoi.getThu());
                            if (!cacThuTrung.contains(tenThu)) {
                                cacThuTrung.add(tenThu);
                            }
                        }
                    }
                }
            }
        }
        
        // Nếu có trùng giờ, báo lỗi
        if (!cacThuTrung.isEmpty()) {
            String errorMsg = String.format(
                "Lỗi: Phòng học '%s' đã được sử dụng vào %s. " +
                "Vui lòng chọn phòng khác hoặc thay đổi thời gian học.",
                lopMoi.getMaLop(), String.join(", ", cacThuTrung)
            );
            return new ValidationResult(false, errorMsg);
        }
        
        // Không trùng giờ, cho phép thêm
        return new ValidationResult(true, null);
    }
    
    /**
     * Kiểm tra xem lớp mới có trùng phòng học, giáo viên và trùng giờ với lớp khác trong cùng môn không
     */
    public static ValidationResult kiemTraTrungTrongCungMon(LopHoc lopMoi, MonHoc monHoc, String excludeLopId) {
        // Kiểm tra trùng phòng học trong cùng môn
        List<LopHoc> cacLopCungPhong = new ArrayList<>();
        List<LopHoc> cacLopCungGv = new ArrayList<>();
        
        for (LopHoc lop : monHoc.getCacLopHoc()) {
            // Bỏ qua lớp hiện tại nếu đang sửa
            if (excludeLopId != null && lop.getId().equals(excludeLopId)) {
                continue;
            }
            
            if (lop.getMaLop().equals(lopMoi.getMaLop())) {
                cacLopCungPhong.add(lop);
            }
            
            if (lop.getTenGiaoVien().trim().equalsIgnoreCase(lopMoi.getTenGiaoVien().trim())) {
                cacLopCungGv.add(lop);
            }
        }
        
        // Kiểm tra trùng phòng học và trùng giờ
        List<String> cacThuTrungPhong = new ArrayList<>();
        for (LopHoc lopCungPhong : cacLopCungPhong) {
            for (ThoiGianHoc gioMoi : lopMoi.getCacKhungGio()) {
                for (ThoiGianHoc gioCu : lopCungPhong.getCacKhungGio()) {
                    if (gioMoi.getThu() == gioCu.getThu()) {
                        if (kiemTraXungDotGio(gioMoi, gioCu)) {
                            String tenThu = Constants.TEN_THU_TRONG_TUAN.getOrDefault(gioMoi.getThu(), "Thứ " + gioMoi.getThu());
                            if (!cacThuTrungPhong.contains(tenThu)) {
                                cacThuTrungPhong.add(tenThu);
                            }
                        }
                    }
                }
            }
        }
        
        if (!cacThuTrungPhong.isEmpty()) {
            String errorMsg = String.format(
                "Lỗi: Phòng học '%s' đã được sử dụng trong môn này vào %s. " +
                "Vui lòng chọn phòng khác hoặc thay đổi thời gian học.",
                lopMoi.getMaLop(), String.join(", ", cacThuTrungPhong)
            );
            return new ValidationResult(false, errorMsg);
        }
        
        // Kiểm tra trùng giáo viên và trùng giờ
        List<String> cacThuTrungGv = new ArrayList<>();
        for (LopHoc lopCungGv : cacLopCungGv) {
            for (ThoiGianHoc gioMoi : lopMoi.getCacKhungGio()) {
                for (ThoiGianHoc gioCu : lopCungGv.getCacKhungGio()) {
                    if (gioMoi.getThu() == gioCu.getThu()) {
                        if (kiemTraXungDotGio(gioMoi, gioCu)) {
                            String tenThu = Constants.TEN_THU_TRONG_TUAN.getOrDefault(gioMoi.getThu(), "Thứ " + gioMoi.getThu());
                            if (!cacThuTrungGv.contains(tenThu)) {
                                cacThuTrungGv.add(tenThu);
                            }
                        }
                    }
                }
            }
        }
        
        if (!cacThuTrungGv.isEmpty()) {
            String errorMsg = String.format(
                "Lỗi: Giáo viên '%s' đã có lớp khác trong môn này vào %s. " +
                "Một giáo viên không thể dạy nhiều lớp cùng lúc.",
                lopMoi.getTenGiaoVien(), String.join(", ", cacThuTrungGv)
            );
            return new ValidationResult(false, errorMsg);
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Kiểm tra xem giáo viên của lớp mới có trùng giờ với lớp khác không
     */
    public static ValidationResult kiemTraTrungGiaoVien(LopHoc lopMoi, java.util.Map<String, MonHoc> allCourses, String excludeLopId) {
        // Tìm tất cả các lớp có cùng giáo viên
        List<LopHoc> cacLopCungGv = new ArrayList<>();
        for (MonHoc monHoc : allCourses.values()) {
            for (LopHoc lop : monHoc.getCacLopHoc()) {
                // Bỏ qua lớp hiện tại nếu đang sửa
                if (excludeLopId != null && lop.getId().equals(excludeLopId)) {
                    continue;
                }
                // So sánh tên giáo viên (không phân biệt hoa thường)
                if (lop.getTenGiaoVien().trim().equalsIgnoreCase(lopMoi.getTenGiaoVien().trim())) {
                    cacLopCungGv.add(lop);
                }
            }
        }
        
        // Nếu không có lớp nào cùng giáo viên, cho phép thêm
        if (cacLopCungGv.isEmpty()) {
            return new ValidationResult(true, null);
        }
        
        // Kiểm tra xem có trùng giờ trong cùng 1 ngày không
        List<String> cacThuTrung = new ArrayList<>();
        for (LopHoc lopCungGv : cacLopCungGv) {
            for (ThoiGianHoc gioMoi : lopMoi.getCacKhungGio()) {
                for (ThoiGianHoc gioCu : lopCungGv.getCacKhungGio()) {
                    // Nếu cùng thứ và trùng giờ
                    if (gioMoi.getThu() == gioCu.getThu()) {
                        if (kiemTraXungDotGio(gioMoi, gioCu)) {
                            String tenThu = Constants.TEN_THU_TRONG_TUAN.getOrDefault(gioMoi.getThu(), "Thứ " + gioMoi.getThu());
                            if (!cacThuTrung.contains(tenThu)) {
                                cacThuTrung.add(tenThu);
                            }
                        }
                    }
                }
            }
        }
        
        // Nếu có trùng giờ, báo lỗi
        if (!cacThuTrung.isEmpty()) {
            String errorMsg = String.format(
                "Lỗi: Giáo viên '%s' đã có lớp khác vào %s. " +
                "Một giáo viên không thể dạy nhiều lớp cùng lúc.",
                lopMoi.getTenGiaoVien(), String.join(", ", cacThuTrung)
            );
            return new ValidationResult(false, errorMsg);
        }
        
        // Không trùng giờ, cho phép thêm
        return new ValidationResult(true, null);
    }
    
    /**
     * Kết quả tìm kiếm thời khóa biểu
     */
    public static class ScheduleResult {
        private final List<List<LopHoc>> schedules;
        private final String errorMessage;
        
        public ScheduleResult(List<List<LopHoc>> schedules, String errorMessage) {
            this.schedules = schedules;
            this.errorMessage = errorMessage;
        }
        
        public List<List<LopHoc>> getSchedules() {
            return schedules;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    /**
     * Kết quả kiểm tra validation
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

