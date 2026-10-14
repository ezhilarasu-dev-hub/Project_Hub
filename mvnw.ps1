if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot") {
        $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
    }
}

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn $args
    exit $LASTEXITCODE
}

$userProfile = [Environment]::GetFolderPath("UserProfile")
$mavenCmd = "$userProfile\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"

if (Test-Path $mavenCmd) {
    & $mavenCmd $args
    exit $LASTEXITCODE
} else {
    Write-Error "Maven executable not found in PATH or ~/.m2 wrapper cache."
    exit 1
}
