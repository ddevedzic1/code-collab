[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Name
)

$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$safeName = $Name -replace '[^A-Za-z0-9_]', '_'

Write-Output "V${timestamp}__${safeName}.sql"
