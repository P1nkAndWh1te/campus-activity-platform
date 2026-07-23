$ErrorActionPreference = "Stop"

$base = if ($env:API_BASE) { $env:API_BASE } else { "http://localhost:8080" }
$stamp = Get-Date -Format "yyyyMMddHHmmss"

function Write-Step($Title) {
    Write-Host ""
    Write-Host "============================================================"
    Write-Host $Title
    Write-Host "============================================================"
}

function Write-Json($Value) {
    $Value | ConvertTo-Json -Depth 10
}

function Mask-Token($Token) {
    if (-not $Token -or $Token.Length -lt 20) {
        return $Token
    }
    return $Token.Substring(0, 16) + "...masked..." + $Token.Substring($Token.Length - 8)
}

function New-Headers($Token = $null) {
    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    return $headers
}

function Post-Json($Path, $Body, $Token = $null) {
    Invoke-RestMethod `
        -Method Post `
        -Uri "$base$Path" `
        -ContentType "application/json; charset=utf-8" `
        -Headers (New-Headers $Token) `
        -Body ($Body | ConvertTo-Json -Depth 10)
}

function Get-Api($Path, $Token = $null) {
    Invoke-RestMethod `
        -Method Get `
        -Uri "$base$Path" `
        -Headers (New-Headers $Token)
}

Write-Step "00 Health Check"
$health = Get-Api "/api/health"
Write-Json $health

Write-Step "01 Admin Login - 截图证明管理员登录成功"
$adminLogin = Post-Json "/api/auth/login" @{
    phone = "13900001111"
    password = "test123"
}
$adminToken = $adminLogin.data.token
$adminLogin.data.token = Mask-Token $adminToken
Write-Json $adminLogin

Write-Step "02 Create Activity - 截图证明管理员创建活动成功"
$activityBody = @{
    categoryId = 1
    title = "截图验证活动 $stamp"
    description = "用于项目截图证据采集"
    location = "教学楼A101"
    totalQuota = 1
    startTime = "2026-08-01T14:00:00"
    endTime = "2026-08-01T16:00:00"
    reservationStartTime = "2026-07-23T09:00:00"
    reservationEndTime = "2026-08-01T12:00:00"
}
$created = Post-Json "/api/admin/activities" $activityBody $adminToken
$activityId = $created.data.id
Write-Json $created

Write-Step "03 User Login - 截图证明普通用户登录成功"
$userLogin = Post-Json "/api/auth/login" @{
    phone = "13800001111"
    password = "test123"
}
$userToken = $userLogin.data.token
$userLogin.data.token = Mask-Token $userToken
Write-Json $userLogin

Write-Step "04 Activity Detail - 截图证明活动详情可查询，Redis 缓存链路可触发"
$detail = Get-Api "/api/activities/$activityId"
Write-Json $detail

Write-Step "05 Reserve Activity - 截图证明用户预约成功并返回预约码"
$reserve = Post-Json "/api/activities/$activityId/reservations" @{} $userToken
$reservationCode = $reserve.data.reservationCode
Write-Json $reserve

Write-Step "06 Repeat Reservation - 截图证明重复预约被拦截"
$repeatReserve = Post-Json "/api/activities/$activityId/reservations" @{} $userToken
Write-Json $repeatReserve

Write-Step "07 Admin Find Reservation By Code - 截图证明管理员可按预约码查询"
$reservationDetail = Get-Api "/api/admin/reservations/by-code/$reservationCode" $adminToken
Write-Json $reservationDetail

Write-Step "08 Verify Reservation - 截图证明管理员核销成功"
$verify = Post-Json "/api/admin/verifications" @{
    reservationCode = $reservationCode
} $adminToken
Write-Json $verify

Write-Step "09 Repeat Verification - 截图证明重复核销被拦截"
$repeatVerify = Post-Json "/api/admin/verifications" @{
    reservationCode = $reservationCode
} $adminToken
Write-Json $repeatVerify

Write-Step "10 Verification List - 截图证明核销记录已写入"
$verificationList = Get-Api "/api/admin/verifications" $adminToken
Write-Json $verificationList

Write-Step "DONE"
Write-Host "截图证据采集完成。建议截图 01、02、05、06、08、09 和 10。"
