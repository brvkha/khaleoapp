param(
    [string]$RepoRoot = "C:\Workspace\FPT\github\khaleoapp",
    [string]$SecretsFile = "C:\Workspace\FPT\github\khaleoapp\docs\manual-e2e\staging-secrets.local.env",
    [string]$GitHubRepo = ""
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    throw "GitHub CLI (gh) not found. Install from https://cli.github.com/ and run 'gh auth login'."
}

if (-not (Test-Path $SecretsFile)) {
    throw "Secrets file not found: $SecretsFile. Run scripts/setup-staging-config.ps1 first."
}

Set-Location $RepoRoot

if ([string]::IsNullOrWhiteSpace($GitHubRepo)) {
    $remoteUrl = git config --get remote.origin.url
    if ([string]::IsNullOrWhiteSpace($remoteUrl)) {
        throw "Cannot detect remote.origin.url. Pass -GitHubRepo owner/repo explicitly."
    }
    if ($remoteUrl -match "github.com[:/](.+?)(?:\.git)?$") {
        $GitHubRepo = $Matches[1]
    }
    else {
        throw "Remote URL is not a GitHub repository: $remoteUrl"
    }
}

$lines = Get-Content -Path $SecretsFile | Where-Object { $_ -and -not $_.StartsWith("#") }
foreach ($line in $lines) {
    $parts = $line.Split("=", 2)
    if ($parts.Count -ne 2) { continue }

    $name = $parts[0].Trim()
    $value = $parts[1]

    if ([string]::IsNullOrWhiteSpace($name)) { continue }

    if ($name -eq "EC2_SSH_PRIVATE_KEY_PATH") {
        if (-not (Test-Path $value)) {
            throw "EC2_SSH_PRIVATE_KEY_PATH does not exist: $value"
        }
        $name = "EC2_SSH_PRIVATE_KEY"
        $value = Get-Content -Path $value -Raw
    }

    $temp = [System.IO.Path]::GetTempFileName()
    try {
        Set-Content -Path $temp -Value $value -NoNewline -Encoding UTF8
        gh secret set $name --repo $GitHubRepo --env staging --body "$(Get-Content -Raw $temp)" | Out-Null
        Write-Host "Set secret: $name"
    }
    finally {
        Remove-Item -Path $temp -ErrorAction SilentlyContinue
    }
}

Write-Host "Done setting staging secrets in GitHub environment 'staging' for repo $GitHubRepo."
