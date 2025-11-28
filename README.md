# TKB Planner Pro - Java Edition

Công cụ sắp xếp thời khóa biểu (TKB) tự động với giao diện đồ họa JavaFX.

## Mô tả

TKB Planner Pro là ứng dụng giúp sinh viên tìm kiếm và sắp xếp thời khóa biểu học tập một cách tự động. Ứng dụng có thể:

- Quản lý danh sách môn học và các lớp học
- Xác định các giờ bận (không thể học)
- Tìm tất cả các thời khóa biểu hợp lệ không bị trùng lịch
- Hỗ trợ môn tiên quyết và môn bắt buộc
- Hiển thị thời khóa biểu dạng lưới trực quan
- Lưu và tải dữ liệu từ file JSON

## Yêu cầu hệ thống

- Java 17 trở lên
- Maven 3.6+
- JavaFX 21

## Cài đặt

### 1. Cài đặt Java

Đảm bảo bạn đã cài đặt Java 17 trở lên. Kiểm tra bằng lệnh:

```bash
java -version
```

### 2. Cài đặt Maven

Đảm bảo bạn đã cài đặt Maven. Kiểm tra bằng lệnh:

```bash
mvn -version
```

### 3. Build dự án

```bash
cd java-t5
mvn clean install
```

## Cách chạy

### Sử dụng Maven

```bash
mvn javafx:run
```

### Hoặc chạy trực tiếp

```bash
mvn compile exec:java -Dexec.mainClass="com.tkbplanner.Main"
```

## Cấu trúc dự án

```
java-t5/
├── src/main/java/com/tkbplanner/
│   ├── Main.java                    # Entry point
│   ├── constants/                   # Các hằng số
│   ├── models/                      # Các class model
│   ├── scheduler/                   # Logic tìm kiếm TKB
│   ├── data/                        # Xử lý lưu/tải dữ liệu JSON
│   └── ui/                          # Giao diện người dùng
├── src/main/resources/              # Tài nguyên (CSS, images)
├── pom.xml                          # Maven configuration
└── README.md                        # File này
```

## Tính năng

- ✅ Quản lý môn học và lớp học
- ✅ Kiểm tra xung đột lịch học
- ✅ Hỗ trợ môn tiên quyết
- ✅ Hỗ trợ môn bắt buộc
- ✅ Quản lý giờ bận
- ✅ Tìm kiếm tất cả TKB hợp lệ
- ✅ Hiển thị lịch dạng lưới với ngày tháng
- ✅ Lưu/tải dữ liệu JSON
- ✅ Lưu TKB ra file text
- ✅ Tìm kiếm và lọc môn học
- ✅ Sửa/xóa môn học
- ✅ Chế độ sáng/tối (Dark/Light mode)

## Phiên bản

Version 3.0.0 (Java Edition)

## Giấy phép

Dự án này được phát triển cho mục đích giáo dục.

