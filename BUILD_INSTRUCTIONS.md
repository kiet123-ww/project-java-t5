# Hướng dẫn Build và Chạy

## Yêu cầu
- Java 17+ (đã cài đặt: Java 25)
- Maven (không cần cài - sử dụng Maven Wrapper)

## Build dự án

### Cách 1: Sử dụng script PowerShell (Khuyến nghị)
```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 clean compile
```

### Cách 2: Sử dụng Maven Wrapper trực tiếp
```powershell
# Set JAVA_HOME trước
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
.\mvnw.cmd clean compile
```

## Chạy ứng dụng

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 javafx:run
```

Hoặc:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
.\mvnw.cmd javafx:run
```

## Package thành JAR

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 package
```

## Lưu ý

- Script `build.ps1` sẽ tự động tìm và set JAVA_HOME
- Maven Wrapper sẽ tự động tải Maven nếu chưa có
- Dự án hiện tại có UI cơ bản, cần phát triển thêm các UI components đầy đủ

