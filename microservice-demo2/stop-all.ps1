$ErrorActionPreference = 'Stop'

$tempRoot = Join-Path $env:TEMP 'microservice-demo2'
$stateDir = Join-Path $tempRoot 'state'
$stateFile = Join-Path $stateDir 'service-processes.json'

$knownServices = @(
	[pscustomobject]@{
		Name    = 'eureka-server'
		JarName = 'eureka-server-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'category-service'
		JarName = 'category-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'user-service'
		JarName = 'user-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'product-service'
		JarName = 'product-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'order-service'
		JarName = 'order-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'post-service'
		JarName = 'post-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'api-gateway'
		JarName = 'api-gateway-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'Auth-service'
		JarName = 'Auth-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'Cart-service'
		JarName = 'Cart-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'Search-service'
		JarName = 'Search-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'Inventory-service'
		JarName = 'Inventory-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'Payment-service'
		JarName = 'Payment-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'Customer-service'
		JarName = 'Customer-service-0.0.1-SNAPSHOT.jar'
	},
	[pscustomobject]@{
		Name    = 'Address-service'
		JarName = 'Address-service-0.0.1-SNAPSHOT.jar'
	}
)

function Stop-TrackedProcess {
	param(
		[int]$ProcessId,
		[string]$ServiceName,
		[string]$JarName,
		[switch]$AllowNonJava,
		[System.Collections.Generic.HashSet[int]]$StoppedIds
	)

	if ($ProcessId -le 0) {
		return
	}

	if ($StoppedIds -and $StoppedIds.Contains($ProcessId)) {
		return
	}

	$process = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction SilentlyContinue
	if (-not $process) {
		Write-Host "$ServiceName process $ProcessId is not running."
		return
	}

	if (-not $AllowNonJava) {
		if ($process.Name -ne 'java.exe') {
			Write-Warning "Skipping $ServiceName process $ProcessId because it is $($process.Name), not java.exe."
			return
		}

		if ($JarName -and $process.CommandLine -notlike "*$JarName*") {
			Write-Warning "Skipping $ServiceName process $ProcessId because the command line does not match $JarName."
			return
		}
	}

	try {
		Stop-Process -Id $ProcessId -Force -ErrorAction Stop
		if ($StoppedIds) {
			[void]$StoppedIds.Add($ProcessId)
		}
		Write-Host "Stopped $ServiceName process $ProcessId"
	}
	catch {
		Write-Warning "Could not stop $ServiceName process ${ProcessId}: $($_.Exception.Message)"
	}
}

function Stop-ProcessesByJarName {
	param(
		[string]$ServiceName,
		[string]$JarName,
		[System.Collections.Generic.HashSet[int]]$StoppedIds
	)

	$javaProcesses = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" | Where-Object {
		$_.CommandLine -like "*$JarName*"
	}

	foreach ($javaProcess in $javaProcesses) {
		if ($StoppedIds.Contains([int]$javaProcess.ProcessId)) {
			continue
		}

		try {
			Stop-Process -Id $javaProcess.ProcessId -Force -ErrorAction Stop
			[void]$StoppedIds.Add([int]$javaProcess.ProcessId)
			Write-Host "Stopped $ServiceName java process $($javaProcess.ProcessId)"
		}
		catch {
			Write-Warning "Could not stop $ServiceName java process $($javaProcess.ProcessId): $($_.Exception.Message)"
		}
	}
}

$services = $knownServices
if (Test-Path $stateFile) {
	try {
		$services = @(Get-Content -Path $stateFile -Raw | ConvertFrom-Json)
	}
	catch {
		Write-Warning "Could not read saved service state. Falling back to known jar names: $($_.Exception.Message)"
		$services = $knownServices
	}
}
else {
	Write-Host 'No saved service state found. Falling back to known jar names.'
}

$stoppedIds = New-Object 'System.Collections.Generic.HashSet[int]'

foreach ($service in $services) {
	$serviceName = $service.Name
	$jarName = $service.JarName

	if ($service.Pid) {
		Stop-TrackedProcess -ProcessId ([int]$service.Pid) -ServiceName $serviceName -JarName $jarName -StoppedIds $stoppedIds
	}

	if ($service.WindowPid -and ([int]$service.WindowPid -ne [int]$service.Pid)) {
		Stop-TrackedProcess -ProcessId ([int]$service.WindowPid) -ServiceName $serviceName -AllowNonJava -StoppedIds $stoppedIds
	}

	if ($jarName) {
		Stop-ProcessesByJarName -ServiceName $serviceName -JarName $jarName -StoppedIds $stoppedIds
	}
}

if (Test-Path $stateFile) {
	Remove-Item -Path $stateFile -Force
	Write-Host 'Service state removed.'
}
