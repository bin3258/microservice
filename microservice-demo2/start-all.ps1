$ErrorActionPreference = 'Stop'

$repoRoot = $PSScriptRoot
$tempRoot = Join-Path $env:TEMP 'microservice-demo2'
$stateDir = Join-Path $tempRoot 'state'
$stateFile = Join-Path $stateDir 'service-processes.json'

$serviceDefinitions = @(
	[pscustomobject]@{
		Name     = 'eureka-server'
		Module   = 'eureka-server'
		JarName  = 'eureka-server-0.0.1-SNAPSHOT.jar'
		Port     = 8761
	},
	[pscustomobject]@{
		Name     = 'category-service'
		Module   = 'category-service'
		JarName  = 'category-service-0.0.1-SNAPSHOT.jar'
		Port     = 8084
	},
	[pscustomobject]@{
		Name     = 'user-service'
		Module   = 'user-service'
		JarName  = 'user-service-0.0.1-SNAPSHOT.jar'
		Port     = 8082
	},
	[pscustomobject]@{
		Name     = 'product-service'
		Module   = 'product-service'
		JarName  = 'product-service-0.0.1-SNAPSHOT.jar'
		Port     = 8081
	},
	[pscustomobject]@{
		Name     = 'order-service'
		Module   = 'order-service'
		JarName  = 'order-service-0.0.1-SNAPSHOT.jar'
		Port     = 8083
	},
	[pscustomobject]@{
		Name     = 'post-service'
		Module   = 'post-service'
		JarName  = 'post-service-0.0.1-SNAPSHOT.jar'
		Port     = 8085
	},
	[pscustomobject]@{
		Name     = 'api-gateway'
		Module   = 'api-gateway'
		JarName  = 'api-gateway-0.0.1-SNAPSHOT.jar'
		Port     = 8080
	},
	[pscustomobject]@{
		Name     = 'Auth-service'
		Module   = 'Auth-service'
		JarName  = 'Auth-service-0.0.1-SNAPSHOT.jar'
		Port     = 8086
	},
	[pscustomobject]@{
		Name     = 'Cart-service'
		Module   = 'Cart-service'
		JarName  = 'Cart-service-0.0.1-SNAPSHOT.jar'
		Port     = 8087
	},
	[pscustomobject]@{
		Name     = 'Search-service'
		Module   = 'Search-service'
		JarName  = 'Search-service-0.0.1-SNAPSHOT.jar'
		Port     = 8088
	},
	[pscustomobject]@{
		Name     = 'Inventory-service'
		Module   = 'Inventory-service'
		JarName  = 'Inventory-service-0.0.1-SNAPSHOT.jar'
		Port     = 8089
	},
	[pscustomobject]@{
		Name     = 'Payment-service'
		Module   = 'Payment-service'
		JarName  = 'Payment-service-0.0.1-SNAPSHOT.jar'
		Port     = 8090
	},
	[pscustomobject]@{
		Name     = 'Customer-service'
		Module   = 'Customer-service'
		JarName  = 'Customer-service-0.0.1-SNAPSHOT.jar'
		Port     = 8091
	},
	[pscustomobject]@{
		Name     = 'Address-service'
		Module   = 'Address-service'
		JarName  = 'Address-service-0.0.1-SNAPSHOT.jar'
		Port     = 8092
	}
)

function Resolve-JavaExecutable {
	$javaCommand = Get-Command java -ErrorAction SilentlyContinue
	if ($javaCommand) {
		return $javaCommand.Path
	}

	if ($env:JAVA_HOME) {
		$javaHomeExe = Join-Path $env:JAVA_HOME 'bin\java.exe'
		if (Test-Path $javaHomeExe) {
			return $javaHomeExe
		}
	}

	throw 'Java is not available on PATH and JAVA_HOME is not set.'
}

function Get-JarPath {
	param(
		[string]$ModuleName,
		[string]$JarName
	)

	return (Join-Path (Join-Path $repoRoot $ModuleName) "target\$JarName")
}

function Test-PortOpen {
	param(
		[string]$ComputerName,
		[int]$Port
	)

	$client = New-Object System.Net.Sockets.TcpClient
	try {
		$asyncResult = $client.BeginConnect($ComputerName, $Port, $null, $null)
		if (-not $asyncResult.AsyncWaitHandle.WaitOne(1000, $false)) {
			return $false
		}

		$client.EndConnect($asyncResult)
		return $client.Connected
	}
	catch {
		return $false
	}
	finally {
		$client.Close()
	}
}

function Wait-PortOpen {
	param(
		[string]$ComputerName,
		[int]$Port,
		[int]$TimeoutSeconds = 120,
		[int]$ProcessId = 0,
		[string]$ServiceName = 'service'
	)

	$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
	while ((Get-Date) -lt $deadline) {
		if ($ProcessId -gt 0) {
			$process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
			if (-not $process) {
				throw "$ServiceName exited before ${ComputerName}:$Port became ready."
			}
		}

		if (Test-PortOpen -ComputerName $ComputerName -Port $Port) {
			return
		}

		Start-Sleep -Seconds 2
	}

	throw "Timed out waiting for $ServiceName on ${ComputerName}:$Port."
}

function Get-ChildJavaProcessId {
	param(
		[int]$ParentPid
	)

	$deadline = (Get-Date).AddSeconds(5)
	while ((Get-Date) -lt $deadline) {
		$child = Get-CimInstance Win32_Process -Filter "ParentProcessId = $ParentPid" -ErrorAction SilentlyContinue | Where-Object { $_.Name -eq 'java.exe' }
		if ($child) {
			return $child.ProcessId
		}
		Start-Sleep -Milliseconds 200
	}
	return 0
}

function Save-State {
	param(
		[object[]]$Services
	)

	New-Item -ItemType Directory -Force -Path $stateDir | Out-Null
	@($Services) | ConvertTo-Json -Depth 6 | Set-Content -Path $stateFile -Encoding UTF8
}

function Start-Service {
	param(
		[pscustomobject]$Definition,
		[string]$JavaExecutable
	)

	$jarPath = Get-JarPath -ModuleName $Definition.Module -JarName $Definition.JarName
	if (-not (Test-Path $jarPath)) {
		throw "Missing jar for $($Definition.Name): $jarPath"
	}

	Write-Host "Starting $($Definition.Name)..."
	$cmdArguments = "/k title $($Definition.Name) && `"$JavaExecutable`" -jar `"$jarPath`""
	$windowProcess = Start-Process -FilePath 'cmd.exe' -ArgumentList $cmdArguments -WorkingDirectory $repoRoot -PassThru

	# Get the PID of the spawned java.exe child process
	$javaPid = Get-ChildJavaProcessId -ParentPid $windowProcess.Id

	return [pscustomobject]@{
		Name      = $Definition.Name
		Module    = $Definition.Module
		JarName   = $Definition.JarName
		JarPath   = $jarPath
		Port      = $Definition.Port
		Pid       = $javaPid
		WindowPid = $windowProcess.Id
		StartedAt = (Get-Date).ToString('o')
	}
}

$javaExecutable = Resolve-JavaExecutable

if (Get-Command mvn -ErrorAction SilentlyContinue) {
	Write-Host 'Building all modules...'
	& mvn -f (Join-Path $repoRoot 'pom.xml') -q -DskipTests package
	if ($LASTEXITCODE -ne 0) {
		throw "Maven build failed with exit code $LASTEXITCODE."
	}
}
else {
	Write-Warning 'Maven not found on PATH. Skipping build and using existing jars.'
}

$runningServices = @()

$eurekaDefinition = $serviceDefinitions[0]
$runningServices += Start-Service -Definition $eurekaDefinition -JavaExecutable $javaExecutable
Save-State -Services $runningServices

Write-Host "Waiting for $($eurekaDefinition.Name) on port $($eurekaDefinition.Port)..."
Wait-PortOpen -ComputerName 'localhost' -Port $eurekaDefinition.Port -ProcessId $runningServices[0].Pid -ServiceName $eurekaDefinition.Name
Write-Host "$($eurekaDefinition.Name) is ready."

for ($index = 1; $index -lt $serviceDefinitions.Count; $index++) {
	$runningServices += Start-Service -Definition $serviceDefinitions[$index] -JavaExecutable $javaExecutable
	Save-State -Services $runningServices
}

Write-Host ''
Write-Host 'All services started.'
Write-Host 'Each service is running in its own separate CMD terminal window.'
Write-Host "State: $stateFile"
