$ErrorActionPreference = "Stop"

$base = if ($env:API_BASE) { $env:API_BASE } else { "http://localhost:8080" }
if (-not $env:MYSQL_PASSWORD) {
    throw "MYSQL_PASSWORD is required for promoting the temporary admin user."
}

$stamp = Get-Date -Format "HHmmss"
$userPhone = "136" + $stamp + "01"
$adminPhone = "137" + $stamp + "02"
$password = "test123"
$results = New-Object System.Collections.Generic.List[object]

function Add-Result($Name, $Passed, $Detail) {
    $script:results.Add([pscustomobject]@{
        name = $Name
        passed = $Passed
        detail = $Detail
    }) | Out-Null
}

function Post-Json($Path, $Body, $Token = $null) {
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    Invoke-RestMethod `
        -Method Post `
        -Uri "$base$Path" `
        -ContentType "application/json; charset=utf-8" `
        -Headers $headers `
        -Body ($Body | ConvertTo-Json -Depth 10)
}

function Get-Api($Path, $Token = $null) {
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    Invoke-RestMethod -Method Get -Uri "$base$Path" -Headers $headers
}

function Delete-Api($Path, $Token = $null) {
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    Invoke-RestMethod -Method Delete -Uri "$base$Path" -Headers $headers
}

function Expect-Code($Name, $Response, $Code = 200) {
    $ok = ($Response.code -eq $Code)
    Add-Result $Name $ok "code=$($Response.code), message=$($Response.message)"
    if (-not $ok) {
        throw "$Name failed: code=$($Response.code), message=$($Response.message)"
    }
}

try {
    $health = Get-Api "/api/health"
    Add-Result "health" ($health.status -eq "ok") "status=$($health.status)"

    Expect-Code "register user" (Post-Json "/api/auth/register" @{
        phone = $userPhone
        password = $password
        nickname = "VerifyUser"
    })

    Expect-Code "register admin seed" (Post-Json "/api/auth/register" @{
        phone = $adminPhone
        password = $password
        nickname = "VerifyAdmin"
    })

    $env:MYSQL_PWD = $env:MYSQL_PASSWORD
    mysql -uroot --default-character-set=utf8mb4 campus_activity -e "UPDATE app_user SET role='ADMIN' WHERE phone='$adminPhone';" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "mysql admin promotion failed: exit=$LASTEXITCODE"
    }
    Add-Result "promote admin by sql" $true "phone=$adminPhone"

    $loginUser = Post-Json "/api/auth/login" @{ phone = $userPhone; password = $password }
    Expect-Code "login user" $loginUser
    $userToken = $loginUser.data.token

    $loginAdmin = Post-Json "/api/auth/login" @{ phone = $adminPhone; password = $password }
    Expect-Code "login admin" $loginAdmin
    $adminToken = $loginAdmin.data.token

    Expect-Code "current user" (Get-Api "/api/users/me" $userToken)
    Expect-Code "list categories" (Get-Api "/api/activity-categories")

    $activityBody = @{
        categoryId = 1
        title = "API Verify Activity $stamp"
        description = "Created by verification script"
        location = "Room 101"
        totalQuota = 2
        startTime = "2026-08-10T10:00:00"
        endTime = "2026-08-10T12:00:00"
        reservationStartTime = "2026-07-22T00:00:00"
        reservationEndTime = "2026-08-09T23:59:59"
    }

    $created = Post-Json "/api/admin/activities" $activityBody $adminToken
    Expect-Code "admin create activity" $created
    $activityId = $created.data.id

    Expect-Code "activity detail with redis" (Get-Api "/api/activities/$activityId")

    $reserve = Post-Json "/api/activities/$activityId/reservations" @{} $userToken
    Expect-Code "reserve activity" $reserve
    $reservationCode = $reserve.data.reservationCode

    $repeat = Post-Json "/api/activities/$activityId/reservations" @{} $userToken
    Add-Result "repeat reservation blocked" ($repeat.code -ne 200) "code=$($repeat.code), message=$($repeat.message)"

    Expect-Code "my reservations" (Get-Api "/api/users/me/reservations" $userToken)
    Expect-Code "admin find by code" (Get-Api "/api/admin/reservations/by-code/$reservationCode" $adminToken)
    Expect-Code "admin verify reservation" (Post-Json "/api/admin/verifications" @{ reservationCode = $reservationCode } $adminToken)

    $verifyAgain = Post-Json "/api/admin/verifications" @{ reservationCode = $reservationCode } $adminToken
    Add-Result "repeat verification blocked" ($verifyAgain.code -ne 200) "code=$($verifyAgain.code), message=$($verifyAgain.message)"

    Expect-Code "list verifications" (Get-Api "/api/admin/verifications" $adminToken)

    $activityBody2 = $activityBody.Clone()
    $activityBody2.title = "API Cancel Activity $stamp"
    $created2 = Post-Json "/api/admin/activities" $activityBody2 $adminToken
    Expect-Code "admin create cancel activity" $created2
    $activityId2 = $created2.data.id

    $reserve2 = Post-Json "/api/activities/$activityId2/reservations" @{} $userToken
    Expect-Code "reserve cancel activity" $reserve2
    $reservationId2 = $reserve2.data.id

    Expect-Code "cancel reservation" (Delete-Api "/api/reservations/$reservationId2" $userToken)
} catch {
    Add-Result "script exception" $false $_.Exception.Message
}

$results | ConvertTo-Json -Depth 5
