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

Write-Step "01 Admin Login - screenshot proof"
$adminLogin = Post-Json "/api/auth/login" @{
    phone = "13900001111"
    password = "test123"
}
$adminToken = $adminLogin.data.token
$adminLogin.data.token = Mask-Token $adminToken
Write-Json $adminLogin

Write-Step "02 Create Activity - screenshot proof"
$activityBody = @{
    categoryId = 1
    title = "Screenshot Evidence Activity $stamp"
    description = "Created for screenshot evidence"
    location = "Room A101"
    totalQuota = 2
    startTime = "2026-08-01T14:00:00"
    endTime = "2026-08-01T16:00:00"
    reservationStartTime = "2026-07-23T09:00:00"
    reservationEndTime = "2026-08-01T12:00:00"
}
$created = Post-Json "/api/admin/activities" $activityBody $adminToken
$activityId = $created.data.id
Write-Json $created

Write-Step "03 User Login - screenshot proof"
$userLogin = Post-Json "/api/auth/login" @{
    phone = "13800001111"
    password = "test123"
}
$userToken = $userLogin.data.token
$userLogin.data.token = Mask-Token $userToken
Write-Json $userLogin

Write-Step "04 Activity Detail - screenshot proof"
$detail = Get-Api "/api/activities/$activityId"
Write-Json $detail

Write-Step "05 Reserve Activity - screenshot proof"
$reserve = Post-Json "/api/activities/$activityId/reservations" @{} $userToken
$reservationCode = $reserve.data.reservationCode
Write-Json $reserve

Write-Step "06 Repeat Reservation - screenshot proof"
$repeatReserve = Post-Json "/api/activities/$activityId/reservations" @{} $userToken
Write-Json $repeatReserve

Write-Step "07 Admin Find Reservation By Code - screenshot proof"
$reservationDetail = Get-Api "/api/admin/reservations/by-code/$reservationCode" $adminToken
Write-Json $reservationDetail

Write-Step "08 Verify Reservation - screenshot proof"
$verify = Post-Json "/api/admin/verifications" @{
    reservationCode = $reservationCode
} $adminToken
Write-Json $verify

Write-Step "09 Repeat Verification - screenshot proof"
$repeatVerify = Post-Json "/api/admin/verifications" @{
    reservationCode = $reservationCode
} $adminToken
Write-Json $repeatVerify

Write-Step "10 Verification List - screenshot proof"
$verificationList = Get-Api "/api/admin/verifications" $adminToken
Write-Json $verificationList

Write-Step "DONE"
Write-Host "Screenshot evidence completed. Recommended screenshots: 01, 02, 05, 06, 08, 09, 10."
