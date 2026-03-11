# Test Agent Skill Manager API

$baseUrl = "http://localhost:14578/api/agent-skills"

Write-Host "=== Testing Agent Skill Manager API ===" -ForegroundColor Green
Write-Host ""

# Test 1: Get all skill names
Write-Host "1. Get all skill names:" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "$baseUrl/names" -Method Get
    $result -join ", "
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 2: Get all skills
Write-Host "2. Get all skills:" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "$baseUrl/all" -Method Get
    $result.skills | ForEach-Object { Write-Host "  - $($_.name): $($_.description)" }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Execute datetime skill
Write-Host "3. Execute datetime skill:" -ForegroundColor Yellow
try {
    $body = @{
        parameters = @{
            operation = "current_datetime"
        }
    } | ConvertTo-Json -Depth 3
    
    $result = Invoke-RestMethod -Uri "$baseUrl/execute/datetime" -Method Post -Body $body -ContentType "application/json"
    Write-Host "  Success: $($result.data.datetime)" -ForegroundColor Green
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
    Write-Host "  Response: $($_.ErrorDetails.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 4: Get skill details
Write-Host "4. Get datetime skill details:" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "$baseUrl/datetime" -Method Get
    Write-Host "  Name: $($result.name)"
    Write-Host "  Version: $($result.version)"
    Write-Host "  Required Params: $($result.requiredParameters.Count)"
    Write-Host "  Optional Params: $($result.optionalParameters.Count)"
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== API Test Complete ===" -ForegroundColor Green
