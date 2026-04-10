param(
    [string]$RepoRoot = "C:\Workspace\FPT\github\khaleoapp",
    [string]$SecretsFile = "C:\Workspace\FPT\github\khaleoapp\docs\manual-e2e\staging-secrets.local.env"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    throw "GitHub CLI (gh) not found. Install from https://cli.github.com/ and run 'gh auth login'."
}

if (-not (Test-Path $SecretsFile)) {
    throw "Secrets file not found: $SecretsFile. Run scripts/setup-staging-config.ps1 first."
}

Set-Location $RepoRoot

$lines = Get-Content -Path $SecretsFile | Where-Object { $_ -and -not $_.StartsWith("#") }
foreach ($line in $lines) {
    $parts = $line.Split("=", 2)
    if ($parts.Count -ne 2) { continue }

    $name = $parts[0].Trim()
    $value = $parts[1]

    if ([string]::IsNullOrWhiteSpace($name)) { continue }

    $temp = [System.IO.Path]::GetTempFileName()
    try {
        Set-Content -Path $temp -Value $value -NoNewline -Encoding UTF8
        gh secret set $name --repo "FPT/khaleoapp" --env staging --body "$(Get-Content -Raw $temp)" | Out-Null
        Write-Host "Set secret: $name"
    }
    finally {
        Remove-Item -Path $temp -ErrorAction SilentlyContinue
    }
}

Write-Host "Done setting staging secrets in GitHub environment 'staging'."

