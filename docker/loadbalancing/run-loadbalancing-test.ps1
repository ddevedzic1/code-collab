<#
.SYNOPSIS
    Load-balancing demonstration for user-service behind the API gateway.

    Sends N requests to GET /api/v1/instance through the gateway (lb://user-service,
    Spring Cloud LoadBalancer) and reports how many requests each running instance
    served, plus total/average processing time.

.DESCRIPTION
    Run the "without load balancing" baseline with a single instance:
        docker compose up -d --scale user-service=1
        ./run-loadbalancing-test.ps1

    Then the "with load balancing" run with two instances:
        docker compose up -d --scale user-service=2
        ./run-loadbalancing-test.ps1

.PARAMETER Requests
    Number of requests to send (default 100).

.PARAMETER Url
    Target endpoint (default http://localhost:8080/api/v1/instance).
#>
param(
    [int]$Requests = 100,
    [string]$Url = "http://localhost:8080/api/v1/instance"
)

Write-Host "Sending $Requests requests to $Url ..." -ForegroundColor Cyan

$counts = @{}
$durations = New-Object System.Collections.Generic.List[double]
$failures = 0

$total = [System.Diagnostics.Stopwatch]::StartNew()
for ($i = 1; $i -le $Requests; $i++) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $resp = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 10
        $sw.Stop()
        $durations.Add($sw.Elapsed.TotalMilliseconds)
        $key = "$($resp.instanceId.Substring(0,8))  (port $($resp.port), host $($resp.hostname))"
        if ($counts.ContainsKey($key)) { $counts[$key]++ } else { $counts[$key] = 1 }
    }
    catch {
        $sw.Stop()
        $failures++
    }
}
$total.Stop()

Write-Host ""
Write-Host "=== Distribution across instances ===" -ForegroundColor Green
foreach ($entry in $counts.GetEnumerator() | Sort-Object Name) {
    $pct = [math]::Round(($entry.Value / $Requests) * 100, 1)
    "{0,-55} {1,4} req  ({2}%)" -f $entry.Key, $entry.Value, $pct
}
if ($failures -gt 0) {
    Write-Host ("Failures: {0}" -f $failures) -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Timing ===" -ForegroundColor Green
$succeeded = $durations.Count
if ($succeeded -gt 0) {
    $avg = [math]::Round(($durations | Measure-Object -Average).Average, 2)
    $min = [math]::Round(($durations | Measure-Object -Minimum).Minimum, 2)
    $max = [math]::Round(($durations | Measure-Object -Maximum).Maximum, 2)
    "Instances serving traffic : {0}" -f $counts.Count
    "Successful requests       : {0}" -f $succeeded
    "Total wall-clock time     : {0} ms" -f [math]::Round($total.Elapsed.TotalMilliseconds, 2)
    "Average per request       : {0} ms" -f $avg
    "Min / Max per request     : {0} ms / {1} ms" -f $min, $max
}
