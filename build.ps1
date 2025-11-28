# Script để build dự án với Maven Wrapper
# Tự động tìm và set JAVA_HOME

# Tìm JAVA_HOME từ java -XshowSettings
$javaOutput = java -XshowSettings:properties -version 2>&1 | Out-String
$javaHomeMatch = [regex]::Match($javaOutput, 'java\.home\s*=\s*(.+)')
if ($javaHomeMatch.Success) {
    $javaHome = $javaHomeMatch.Groups[1].Value.Trim()
    $env:JAVA_HOME = $javaHome
    Write-Host "JAVA_HOME set to: $javaHome" -ForegroundColor Green
} else {
    # Fallback: tìm trong các thư mục thông thường
    $possiblePaths = @(
        "$env:ProgramFiles\Java\jdk-*",
        "$env:ProgramFiles\Eclipse Adoptium\jdk-*",
        "$env:ProgramFiles\Microsoft\jdk-*"
    )
    
    $found = $false
    foreach ($pathPattern in $possiblePaths) {
        $jdkPath = Get-ChildItem -Path $pathPattern -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($jdkPath) {
            $env:JAVA_HOME = $jdkPath.FullName
            Write-Host "JAVA_HOME set to: $env:JAVA_HOME" -ForegroundColor Green
            $found = $true
            break
        }
    }
    
    if (-not $found) {
        Write-Host "Error: Could not find JAVA_HOME. Please set it manually." -ForegroundColor Red
        Write-Host "Example: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'" -ForegroundColor Yellow
        exit 1
    }
}

# Chạy Maven Wrapper
if (Test-Path ".\mvnw.cmd") {
    Write-Host "Running Maven Wrapper..." -ForegroundColor Green
    & .\mvnw.cmd $args
} else {
    Write-Host "Error: mvnw.cmd not found" -ForegroundColor Red
    exit 1
}
