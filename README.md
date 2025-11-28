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

- **Java 17 trở lên** (bắt buộc)
- **Maven Wrapper** (đã bao gồm trong dự án, không cần cài đặt)
- **JavaFX 21** (tự động tải qua Maven)

> **Lưu ý:** Dự án sử dụng Maven Wrapper, bạn không cần cài đặt Maven toàn cục. Maven sẽ được tải tự động khi chạy lần đầu.

## Cài đặt

### 1. Cài đặt Java

Đảm bảo bạn đã cài đặt Java 17 trở lên. Kiểm tra bằng lệnh:

```bash
java -version
```

**Lưu ý:** Dự án sử dụng Maven Wrapper, không cần cài đặt Maven toàn cục.

## Cách chạy

### Cách 1: Sử dụng Build Script (Khuyến nghị - Windows)

Script `build.ps1` sẽ tự động tìm và thiết lập JAVA_HOME:

```powershell
# Build dự án
powershell -ExecutionPolicy Bypass -File .\build.ps1 clean compile

# Chạy ứng dụng
powershell -ExecutionPolicy Bypass -File .\build.ps1 javafx:run

# Package thành JAR
powershell -ExecutionPolicy Bypass -File .\build.ps1 package
```

### Cách 2: Sử dụng Maven Wrapper trực tiếp

#### Trên Windows:

```powershell
# Thiết lập JAVA_HOME (nếu chưa có)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"  # Thay đổi đường dẫn phù hợp

# Build dự án
.\mvnw.cmd clean compile

# Chạy ứng dụng
.\mvnw.cmd javafx:run

# Package thành JAR
.\mvnw.cmd package
```

#### Trên Linux/Mac:

```bash
# Thiết lập JAVA_HOME (nếu chưa có)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk  # Thay đổi đường dẫn phù hợp

# Build dự án
./mvnw clean compile

# Chạy ứng dụng
./mvnw javafx:run

# Package thành JAR
./mvnw package
```

### Cách 3: Nếu đã cài Maven toàn cục

```bash
# Build dự án
mvn clean compile

# Chạy ứng dụng
mvn javafx:run

# Package thành JAR
mvn package
```

## Xử lý lỗi

### Lỗi: JAVA_HOME not found

Nếu gặp lỗi này, hãy thiết lập biến môi trường JAVA_HOME:

**Windows (PowerShell):**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"  # Thay đổi đường dẫn phù hợp
```

**Windows (CMD):**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

**Linux/Mac:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

Để tìm đường dẫn Java của bạn:
- Windows: `where java` hoặc kiểm tra trong `C:\Program Files\Java\`
- Linux/Mac: `which java` hoặc `readlink -f $(which java)`

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
├── .mvn/wrapper/                    # Maven Wrapper files
├── pom.xml                          # Maven configuration
├── mvnw.cmd                         # Maven Wrapper script (Windows)
├── mvnw                             # Maven Wrapper script (Linux/Mac)
├── build.ps1                        # Build script tự động (Windows)
├── BUILD_INSTRUCTIONS.md            # Hướng dẫn build chi tiết
└── README.md                        # File này
```

## Tính năng

### Đã hoàn thành ✅
- ✅ Quản lý môn học và lớp học (Backend)
- ✅ Kiểm tra xung đột lịch học
- ✅ Hỗ trợ môn tiên quyết
- ✅ Hỗ trợ môn bắt buộc
- ✅ Quản lý giờ bận (Backend)
- ✅ Tìm kiếm tất cả TKB hợp lệ
- ✅ Lưu/tải dữ liệu JSON
- ✅ Build system với Maven Wrapper

### Đang phát triển 🚧
- 🚧 Giao diện người dùng đầy đủ (UI components)
- 🚧 Hiển thị lịch dạng lưới với ngày tháng
- 🚧 Lưu TKB ra file text
- 🚧 Tìm kiếm và lọc môn học
- 🚧 Sửa/xóa môn học qua UI
- 🚧 Chế độ sáng/tối (Dark/Light mode)

## Phiên bản

Version 3.0.0 (Java Edition)

## Giấy phép

Dự án này được phát triển cho mục đích giáo dục.

