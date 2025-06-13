# Требуем права администратора
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Warning "Запустите скрипт от имени администратора!"
    Break
}

# Пути для исключения
$paths = @(
    "$env:USERPROFILE\.gradle",
    "$env:USERPROFILE\AndroidStudioProjects\InnerVoid",
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:LOCALAPPDATA\Google\AndroidStudio2024.3"
)

# Добавляем исключения
foreach ($path in $paths) {
    if (Test-Path $path) {
        Add-MpPreference -ExclusionPath $path
        Write-Host "Добавлено исключение для: $path" -ForegroundColor Green
    } else {
        Write-Host "Путь не существует: $path" -ForegroundColor Yellow
    }
}

Write-Host "`nГотово! Исключения добавлены в Microsoft Defender." -ForegroundColor Green
Write-Host "Перезапустите Android Studio для применения изменений." -ForegroundColor Cyan 