$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
try {
    $javaCommand = (Get-Command java -ErrorAction Stop).Source
    $javacCommand = (Get-Command javac -ErrorAction Stop).Source
    $fxPaths = @('base','graphics','controls','fxml') | ForEach-Object {
        $jarPath = Join-Path $env:USERPROFILE ".m2/repository/org/openjfx/javafx-$_/21.0.6/javafx-$_-21.0.6-win.jar"
        if (!(Test-Path -LiteralPath $jarPath)) { throw "Dependência JavaFX ausente. Abra pom.xml no IntelliJ e carregue o Maven primeiro." }
        $jarPath
    }
    $modulePath = $fxPaths -join ';'
    $classesPath = Join-Path $PSScriptRoot 'build/classes'
    New-Item -ItemType Directory -Path $classesPath -Force | Out-Null
    $sources = Get-ChildItem -LiteralPath 'src/main/java' -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
    & $javacCommand --module-path $modulePath --add-modules javafx.controls,javafx.fxml -encoding UTF-8 -d $classesPath $sources
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível compilar a prévia.' }
    & $javaCommand "-Djavafx.cachedir=$(Join-Path $PSScriptRoot 'build/javafx-cache')" --module-path $modulePath --add-modules javafx.controls,javafx.fxml -cp "$classesPath;src/main/resources" br.com.nucitalanches.preview.NucitaPreviewApp
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível abrir a prévia.' }
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    Read-Host 'Pressione Enter para fechar'
}
