#!/usr/bin/env pwsh
# â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
# MERQORA AI - Post-Deployment Verification Script
# â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
# Verifica que el sistema de training estÃ© funcionando
# â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

param(
    [string]$BaseUrl = "https://mercora-releases-production.up.railway.app",
    [string]$AdminUrl = "https://mercora-admin.netlify.app"
)

$ErrorCount = 0
$WarningCount = 0
$headers = @{ "Content-Type" = "application/json" }

Write-Host ""
Write-Host "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•" -ForegroundColor Cyan
Write-Host "  MERQORA AI TRAINING - DEPLOYMENT VERIFICATION" -ForegroundColor Cyan
Write-Host "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•" -ForegroundColor Cyan
Write-Host ""

# â•â•â• Test 1: API Health Check â•â•â•
Write-Host "Test 1: API Health Check" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/health" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  âœ… API is online and healthy" -ForegroundColor Green
    } else {
        Write-Host "  âŒ API returned status $($response.StatusCode)" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âŒ API not reachable: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# â•â•â• Test 2: Training Metrics Endpoint â•â•â•
Write-Host ""
Write-Host "Test 2: Training Metrics Endpoint" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/metrics?hours=24" -Method GET -UseBasicParsing -TimeoutSec 15
    if ($response.StatusCode -eq 200) {
        $metrics = $response.Content | ConvertFrom-Json
        Write-Host "  âœ… Training metrics endpoint working" -ForegroundColor Green
        
        if ($metrics.pipeline_stats.model_trained) {
            Write-Host "    âœ“ Model trained: YES" -ForegroundColor Green
            Write-Host "    âœ“ Model intents: $($metrics.pipeline_stats.model_intents)" -ForegroundColor Gray
        } else {
            Write-Host "    âš ï¸  Model not trained yet" -ForegroundColor Yellow
            $WarningCount++
        }
        
        Write-Host "    â€¢ Total messages: $($metrics.live_metrics.total_messages)" -ForegroundColor Gray
        Write-Host "    â€¢ AI resolved: $($metrics.live_metrics.ai_resolved)" -ForegroundColor Gray
        Write-Host "    â€¢ Escalated: $($metrics.live_metrics.escalated)" -ForegroundColor Gray
    } else {
        Write-Host "  âŒ Metrics endpoint error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âŒ Metrics endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# â•â•â• Test 3: Dataset Stats â•â•â•
Write-Host ""
Write-Host "Test 3: Dataset Stats" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/dataset/stats" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        $stats = $response.Content | ConvertFrom-Json
        Write-Host "  âœ… Dataset stats endpoint working" -ForegroundColor Green
        Write-Host "    â€¢ Total samples: $($stats.total_samples)" -ForegroundColor Gray
        Write-Host "    â€¢ Unique intents: $($stats.unique_intents)" -ForegroundColor Gray
        
        if ($stats.total_samples -lt 20) {
            Write-Host "    âš ï¸  Less than 20 samples (need minimum for training)" -ForegroundColor Yellow
            $WarningCount++
        }
    } else {
        Write-Host "  âŒ Dataset stats error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âŒ Dataset stats failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# â•â•â• Test 4: Send Test Message â•â•â•
Write-Host ""
Write-Host "Test 4: Send Test Message" -ForegroundColor Yellow
$testBody = @{
    user_id = "verify_test_$(Get-Random -Minimum 1000 -Maximum 9999)"
    message = "Como puedo comprar un producto en Rendly?"
    session_id = "verify_session_$(Get-Date -Format 'yyyyMMddHHmmss')"
} | ConvertTo-Json -Compress

try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/support/message" -Method POST -Headers $headers -Body $testBody -UseBasicParsing -TimeoutSec 30
    if ($response.StatusCode -in @(200, 201)) {
        $result = $response.Content | ConvertFrom-Json
        Write-Host "  âœ… Message processing working" -ForegroundColor Green
        Write-Host "    â€¢ Intent: $($result.analysis.detected_intent)" -ForegroundColor Gray
        Write-Host "    â€¢ Confidence: $($result.analysis.confidence_score)%" -ForegroundColor Gray
        Write-Host "    â€¢ Escalated: $($result.escalated)" -ForegroundColor Gray
        
        if ($result.analysis.confidence_score -lt 50) {
            Write-Host "    âš ï¸  Low confidence score" -ForegroundColor Yellow
            $WarningCount++
        }
    } else {
        Write-Host "  âŒ Message processing error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âŒ Message send failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# â•â•â• Test 5: Predict Intent â•â•â•
Write-Host ""
Write-Host "Test 5: Predict Intent (ML Model)" -ForegroundColor Yellow
try {
    $testMessage = [System.Web.HttpUtility]::UrlEncode("quiero devolver un producto")
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/predict?message=$testMessage" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        $prediction = $response.Content | ConvertFrom-Json
        Write-Host "  âœ… ML prediction working" -ForegroundColor Green
        Write-Host "    â€¢ Predicted intent: $($prediction.predicted_intent)" -ForegroundColor Gray
        Write-Host "    â€¢ Confidence: $([math]::Round($prediction.confidence * 100, 1))%" -ForegroundColor Gray
        Write-Host "    â€¢ Model trained: $($prediction.model_trained)" -ForegroundColor Gray
        
        if (-not $prediction.model_trained) {
            Write-Host "    âš ï¸  Model not trained - predictions will be low quality" -ForegroundColor Yellow
            $WarningCount++
        }
    } else {
        Write-Host "  âŒ Prediction endpoint error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âŒ Prediction failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# â•â•â• Test 6: Flush Buffer â•â•â•
Write-Host ""
Write-Host "Test 6: Buffer Flush" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/flush" -Method POST -UseBasicParsing -TimeoutSec 15
    if ($response.StatusCode -eq 200) {
        $flush = $response.Content | ConvertFrom-Json
        Write-Host "  âœ… Buffer flush working" -ForegroundColor Green
        Write-Host "    â€¢ Flushed: $($flush.flushed) records" -ForegroundColor Gray
        Write-Host "    â€¢ Remaining: $($flush.remaining_buffer)" -ForegroundColor Gray
    } else {
        Write-Host "  âŒ Flush endpoint error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âŒ Flush failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# â•â•â• Test 7: Training Runs History â•â•â•
Write-Host ""
Write-Host "Test 7: Training Runs History" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/runs?limit=5" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        $runs = $response.Content | ConvertFrom-Json
        Write-Host "  âœ… Training runs endpoint working" -ForegroundColor Green
        Write-Host "    â€¢ Total runs: $($runs.data.Count)" -ForegroundColor Gray
        
        if ($runs.data.Count -eq 0) {
            Write-Host "    âš ï¸  No training runs yet - execute first training" -ForegroundColor Yellow
            $WarningCount++
        } else {
            $latest = $runs.data[0]
            Write-Host "    â€¢ Latest run: $($latest.run_name)" -ForegroundColor Gray
            Write-Host "    â€¢ Status: $($latest.status)" -ForegroundColor Gray
            if ($latest.intent_accuracy) {
                Write-Host "    â€¢ Accuracy: $([math]::Round($latest.intent_accuracy * 100, 1))%" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "  âŒ Training runs error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âŒ Training runs failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# â•â•â• Test 8: Admin Dashboard â•â•â•
Write-Host ""
Write-Host "Test 8: Admin Dashboard (Frontend)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$AdminUrl/admin/training-pipeline" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  âœ… Admin dashboard accessible" -ForegroundColor Green
    } else {
        Write-Host "  âŒ Admin dashboard returned status $($response.StatusCode)" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âš ï¸  Admin dashboard check skipped (may need login)" -ForegroundColor Yellow
    $WarningCount++
}

# â•â•â• Test 9: Check Local Files â•â•â•
Write-Host ""
Write-Host "Test 9: Local Files Check" -ForegroundColor Yellow

$requiredFiles = @(
    "c:\Users\Rodrigo\Documents\Rendly\SUPABASE_AI_TRAINING_PIPELINE.sql",
    "c:\Users\Rodrigo\Documents\Rendly\ai-support\python\training_pipeline.py",
    "c:\Users\Rodrigo\Documents\Rendly\ai-support\python\requirements.txt",
    "c:\Users\Rodrigo\Documents\Rendly\ai-support\scripts\train_model.ps1",
    "c:\Users\Rodrigo\Documents\Rendly\admin-web\src\pages\AITrainingDashboard.jsx"
)

$missingFiles = @()
foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "  âœ“ $($file.Split('\')[-1])" -ForegroundColor Green
    } else {
        Write-Host "  âœ— $($file.Split('\')[-1]) MISSING" -ForegroundColor Red
        $missingFiles += $file
        $ErrorCount++
    }
}

if ($missingFiles.Count -eq 0) {
    Write-Host "  âœ… All required files present" -ForegroundColor Green
}

# â•â•â• Test 10: Python Dependencies â•â•â•
Write-Host ""
Write-Host "Test 10: Python Dependencies" -ForegroundColor Yellow
try {
    $sklearnCheck = python -c "import sklearn; print(sklearn.__version__)" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  âœ… scikit-learn installed: $sklearnCheck" -ForegroundColor Green
    } else {
        Write-Host "  âŒ scikit-learn not installed" -ForegroundColor Red
        $ErrorCount++
    }
    
    $numpyCheck = python -c "import numpy; print(numpy.__version__)" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  âœ… numpy installed: $numpyCheck" -ForegroundColor Green
    } else {
        Write-Host "  âŒ numpy not installed" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  âš ï¸  Python check failed (may not be in PATH)" -ForegroundColor Yellow
    $WarningCount++
}

# â•â•â• Summary â•â•â•
Write-Host ""
Write-Host "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•" -ForegroundColor Cyan
Write-Host "  VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•" -ForegroundColor Cyan

if ($ErrorCount -eq 0 -and $WarningCount -eq 0) {
    Write-Host ""
    Write-Host "  âœ…âœ…âœ… ALL TESTS PASSED âœ…âœ…âœ…" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Sistema completamente funcional y listo para producciÃ³n." -ForegroundColor Green
    Write-Host ""
} elseif ($ErrorCount -eq 0) {
    Write-Host ""
    Write-Host "  âœ… Tests passed with $WarningCount warning(s)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  El sistema estÃ¡ funcional pero tiene advertencias menores." -ForegroundColor Yellow
    Write-Host "  Revisar los warnings arriba." -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "  âŒ Tests failed with $ErrorCount error(s) and $WarningCount warning(s)" -ForegroundColor Red
    Write-Host ""
    Write-Host "  El sistema tiene errores crÃ­ticos. Revisar los errores arriba." -ForegroundColor Red
    Write-Host ""
    exit 1
}

# â•â•â• Next Steps â•â•â•
Write-Host "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•" -ForegroundColor Cyan
Write-Host "  NEXT STEPS" -ForegroundColor Cyan
Write-Host "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•" -ForegroundColor Cyan
Write-Host ""

if ($WarningCount -gt 0) {
    Write-Host "Recommended actions:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Ejecutar primer entrenamiento:" -ForegroundColor White
    Write-Host "   .\scripts\train_model.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Verificar mÃ©tricas en admin dashboard:" -ForegroundColor White
    Write-Host "   $AdminUrl/admin/training-pipeline" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "Monitoreo continuo:" -ForegroundColor Cyan
Write-Host "  - Dashboard: $AdminUrl/admin/training-pipeline" -ForegroundColor Gray
Write-Host "  - Metrics API: $BaseUrl/ai/training/metrics" -ForegroundColor Gray
Write-Host "  - Training runs: $BaseUrl/ai/training/runs" -ForegroundColor Gray
Write-Host ""

exit 0
