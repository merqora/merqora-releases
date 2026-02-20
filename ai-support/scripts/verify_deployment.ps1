#!/usr/bin/env pwsh
# ════════════════════════════════════════════════════════
# MERQORA AI - Post-Deployment Verification Script
# ════════════════════════════════════════════════════════
# Verifica que el sistema de training esté funcionando
# ════════════════════════════════════════════════════════

param(
    [string]$BaseUrl = "https://merqora-releases-production.up.railway.app",
    [string]$AdminUrl = "https://rendly-admin.netlify.app"
)

$ErrorCount = 0
$WarningCount = 0
$headers = @{ "Content-Type" = "application/json" }

Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  MERQORA AI TRAINING - DEPLOYMENT VERIFICATION" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# ═══ Test 1: API Health Check ═══
Write-Host "Test 1: API Health Check" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/health" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ API is online and healthy" -ForegroundColor Green
    } else {
        Write-Host "  ❌ API returned status $($response.StatusCode)" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ❌ API not reachable: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# ═══ Test 2: Training Metrics Endpoint ═══
Write-Host ""
Write-Host "Test 2: Training Metrics Endpoint" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/metrics?hours=24" -Method GET -UseBasicParsing -TimeoutSec 15
    if ($response.StatusCode -eq 200) {
        $metrics = $response.Content | ConvertFrom-Json
        Write-Host "  ✅ Training metrics endpoint working" -ForegroundColor Green
        
        if ($metrics.pipeline_stats.model_trained) {
            Write-Host "    ✓ Model trained: YES" -ForegroundColor Green
            Write-Host "    ✓ Model intents: $($metrics.pipeline_stats.model_intents)" -ForegroundColor Gray
        } else {
            Write-Host "    ⚠️  Model not trained yet" -ForegroundColor Yellow
            $WarningCount++
        }
        
        Write-Host "    • Total messages: $($metrics.live_metrics.total_messages)" -ForegroundColor Gray
        Write-Host "    • AI resolved: $($metrics.live_metrics.ai_resolved)" -ForegroundColor Gray
        Write-Host "    • Escalated: $($metrics.live_metrics.escalated)" -ForegroundColor Gray
    } else {
        Write-Host "  ❌ Metrics endpoint error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ❌ Metrics endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# ═══ Test 3: Dataset Stats ═══
Write-Host ""
Write-Host "Test 3: Dataset Stats" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/dataset/stats" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        $stats = $response.Content | ConvertFrom-Json
        Write-Host "  ✅ Dataset stats endpoint working" -ForegroundColor Green
        Write-Host "    • Total samples: $($stats.total_samples)" -ForegroundColor Gray
        Write-Host "    • Unique intents: $($stats.unique_intents)" -ForegroundColor Gray
        
        if ($stats.total_samples -lt 20) {
            Write-Host "    ⚠️  Less than 20 samples (need minimum for training)" -ForegroundColor Yellow
            $WarningCount++
        }
    } else {
        Write-Host "  ❌ Dataset stats error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ❌ Dataset stats failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# ═══ Test 4: Send Test Message ═══
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
        Write-Host "  ✅ Message processing working" -ForegroundColor Green
        Write-Host "    • Intent: $($result.analysis.detected_intent)" -ForegroundColor Gray
        Write-Host "    • Confidence: $($result.analysis.confidence_score)%" -ForegroundColor Gray
        Write-Host "    • Escalated: $($result.escalated)" -ForegroundColor Gray
        
        if ($result.analysis.confidence_score -lt 50) {
            Write-Host "    ⚠️  Low confidence score" -ForegroundColor Yellow
            $WarningCount++
        }
    } else {
        Write-Host "  ❌ Message processing error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ❌ Message send failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# ═══ Test 5: Predict Intent ═══
Write-Host ""
Write-Host "Test 5: Predict Intent (ML Model)" -ForegroundColor Yellow
try {
    $testMessage = [System.Web.HttpUtility]::UrlEncode("quiero devolver un producto")
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/predict?message=$testMessage" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        $prediction = $response.Content | ConvertFrom-Json
        Write-Host "  ✅ ML prediction working" -ForegroundColor Green
        Write-Host "    • Predicted intent: $($prediction.predicted_intent)" -ForegroundColor Gray
        Write-Host "    • Confidence: $([math]::Round($prediction.confidence * 100, 1))%" -ForegroundColor Gray
        Write-Host "    • Model trained: $($prediction.model_trained)" -ForegroundColor Gray
        
        if (-not $prediction.model_trained) {
            Write-Host "    ⚠️  Model not trained - predictions will be low quality" -ForegroundColor Yellow
            $WarningCount++
        }
    } else {
        Write-Host "  ❌ Prediction endpoint error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ❌ Prediction failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# ═══ Test 6: Flush Buffer ═══
Write-Host ""
Write-Host "Test 6: Buffer Flush" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/flush" -Method POST -UseBasicParsing -TimeoutSec 15
    if ($response.StatusCode -eq 200) {
        $flush = $response.Content | ConvertFrom-Json
        Write-Host "  ✅ Buffer flush working" -ForegroundColor Green
        Write-Host "    • Flushed: $($flush.flushed) records" -ForegroundColor Gray
        Write-Host "    • Remaining: $($flush.remaining_buffer)" -ForegroundColor Gray
    } else {
        Write-Host "  ❌ Flush endpoint error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ❌ Flush failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# ═══ Test 7: Training Runs History ═══
Write-Host ""
Write-Host "Test 7: Training Runs History" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/ai/training/runs?limit=5" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        $runs = $response.Content | ConvertFrom-Json
        Write-Host "  ✅ Training runs endpoint working" -ForegroundColor Green
        Write-Host "    • Total runs: $($runs.data.Count)" -ForegroundColor Gray
        
        if ($runs.data.Count -eq 0) {
            Write-Host "    ⚠️  No training runs yet - execute first training" -ForegroundColor Yellow
            $WarningCount++
        } else {
            $latest = $runs.data[0]
            Write-Host "    • Latest run: $($latest.run_name)" -ForegroundColor Gray
            Write-Host "    • Status: $($latest.status)" -ForegroundColor Gray
            if ($latest.intent_accuracy) {
                Write-Host "    • Accuracy: $([math]::Round($latest.intent_accuracy * 100, 1))%" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "  ❌ Training runs error" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ❌ Training runs failed: $($_.Exception.Message)" -ForegroundColor Red
    $ErrorCount++
}

# ═══ Test 8: Admin Dashboard ═══
Write-Host ""
Write-Host "Test 8: Admin Dashboard (Frontend)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$AdminUrl/admin/training-pipeline" -Method GET -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ Admin dashboard accessible" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Admin dashboard returned status $($response.StatusCode)" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ⚠️  Admin dashboard check skipped (may need login)" -ForegroundColor Yellow
    $WarningCount++
}

# ═══ Test 9: Check Local Files ═══
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
        Write-Host "  ✓ $($file.Split('\')[-1])" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $($file.Split('\')[-1]) MISSING" -ForegroundColor Red
        $missingFiles += $file
        $ErrorCount++
    }
}

if ($missingFiles.Count -eq 0) {
    Write-Host "  ✅ All required files present" -ForegroundColor Green
}

# ═══ Test 10: Python Dependencies ═══
Write-Host ""
Write-Host "Test 10: Python Dependencies" -ForegroundColor Yellow
try {
    $sklearnCheck = python -c "import sklearn; print(sklearn.__version__)" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ scikit-learn installed: $sklearnCheck" -ForegroundColor Green
    } else {
        Write-Host "  ❌ scikit-learn not installed" -ForegroundColor Red
        $ErrorCount++
    }
    
    $numpyCheck = python -c "import numpy; print(numpy.__version__)" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ numpy installed: $numpyCheck" -ForegroundColor Green
    } else {
        Write-Host "  ❌ numpy not installed" -ForegroundColor Red
        $ErrorCount++
    }
} catch {
    Write-Host "  ⚠️  Python check failed (may not be in PATH)" -ForegroundColor Yellow
    $WarningCount++
}

# ═══ Summary ═══
Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan

if ($ErrorCount -eq 0 -and $WarningCount -eq 0) {
    Write-Host ""
    Write-Host "  ✅✅✅ ALL TESTS PASSED ✅✅✅" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Sistema completamente funcional y listo para producción." -ForegroundColor Green
    Write-Host ""
} elseif ($ErrorCount -eq 0) {
    Write-Host ""
    Write-Host "  ✅ Tests passed with $WarningCount warning(s)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  El sistema está funcional pero tiene advertencias menores." -ForegroundColor Yellow
    Write-Host "  Revisar los warnings arriba." -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "  ❌ Tests failed with $ErrorCount error(s) and $WarningCount warning(s)" -ForegroundColor Red
    Write-Host ""
    Write-Host "  El sistema tiene errores críticos. Revisar los errores arriba." -ForegroundColor Red
    Write-Host ""
    exit 1
}

# ═══ Next Steps ═══
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  NEXT STEPS" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

if ($WarningCount -gt 0) {
    Write-Host "Recommended actions:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Ejecutar primer entrenamiento:" -ForegroundColor White
    Write-Host "   .\scripts\train_model.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Verificar métricas en admin dashboard:" -ForegroundColor White
    Write-Host "   $AdminUrl/admin/training-pipeline" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "Monitoreo continuo:" -ForegroundColor Cyan
Write-Host "  - Dashboard: $AdminUrl/admin/training-pipeline" -ForegroundColor Gray
Write-Host "  - Metrics API: $BaseUrl/ai/training/metrics" -ForegroundColor Gray
Write-Host "  - Training runs: $BaseUrl/ai/training/runs" -ForegroundColor Gray
Write-Host ""

exit 0
