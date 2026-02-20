#!/usr/bin/env pwsh
# ════════════════════════════════════════════════════════
# MERQORA AI - Training Pipeline Script
# ════════════════════════════════════════════════════════
# This script:
# 1. Sends test messages to collect training data
# 2. Flushes the buffer to Supabase
# 3. Triggers a full ML training run
# 4. Shows the training report
# ════════════════════════════════════════════════════════

param(
    [string]$BaseUrl = "https://merqora-releases-production.up.railway.app",
    [switch]$SkipMessages,
    [switch]$OnlyTrain,
    [switch]$OnlyFlush,
    [int]$MessageDelay = 200
)

$headers = @{ "Content-Type" = "application/json" }

# ═══ Step 1: Send training messages ═══
if (-not $SkipMessages -and -not $OnlyTrain -and -not $OnlyFlush) {
    $messages = @(
        # COMPRAS Y PAGOS
        @{ msg = "Como puedo comprar un producto?"; intent = "purchase_status" },
        @{ msg = "Quiero comprar algo pero no se como"; intent = "purchase_status" },
        @{ msg = "Como agrego algo al carrito?"; intent = "purchase_status" },
        @{ msg = "Que metodos de pago aceptan?"; intent = "payment_methods" },
        @{ msg = "Puedo pagar con tarjeta de credito?"; intent = "payment_methods" },
        @{ msg = "Aceptan Mercado Pago?"; intent = "payment_methods" },
        @{ msg = "Me cobraron dos veces por una compra"; intent = "payment_problem" },
        @{ msg = "El pago fue rechazado que hago?"; intent = "payment_problem" },
        @{ msg = "Quiero un reembolso"; intent = "refund" },
        @{ msg = "Como pido que me devuelvan el dinero?"; intent = "refund" },
        
        # ENVIOS
        @{ msg = "Como agrego una direccion de envio?"; intent = "shipping_info" },
        @{ msg = "Cuanto tarda el envio?"; intent = "shipping_info" },
        @{ msg = "Mi pedido no llega"; intent = "shipping_problem" },
        @{ msg = "El paquete llego danado"; intent = "shipping_problem" },
        @{ msg = "Como hago una devolucion?"; intent = "return_process" },
        
        # CUENTA
        @{ msg = "Como creo una cuenta?"; intent = "account_access" },
        @{ msg = "No puedo iniciar sesion"; intent = "account_access" },
        @{ msg = "Olvide mi contrasena"; intent = "account_access" },
        @{ msg = "Como cambio mi nombre de usuario?"; intent = "account_settings" },
        @{ msg = "Como elimino mi cuenta?"; intent = "account_delete" },
        @{ msg = "Como verifico mi cuenta?"; intent = "account_verify" },
        
        # VENDER
        @{ msg = "Como vendo en Rendly?"; intent = "sell_how" },
        @{ msg = "Quiero publicar un producto para vender"; intent = "sell_how" },
        @{ msg = "Cuando me pagan por mis ventas?"; intent = "sell_payment" },
        @{ msg = "Como edito una publicacion?"; intent = "product_manage" },
        
        # CHAT
        @{ msg = "Como le envio un mensaje a un vendedor?"; intent = "chat_info" },
        @{ msg = "El vendedor no me responde"; intent = "chat_info" },
        @{ msg = "Como bloqueo a un usuario?"; intent = "security_report" },
        @{ msg = "Me estan enviando spam"; intent = "security_report" },
        
        # HISTORIAS Y RENDS
        @{ msg = "Que son las historias en Rendly?"; intent = "stories_info" },
        @{ msg = "Como subo una historia?"; intent = "stories_info" },
        @{ msg = "Que es un Rend?"; intent = "rends_info" },
        @{ msg = "Como creo un Rend?"; intent = "rends_info" },
        
        # SEGURIDAD
        @{ msg = "Mis datos estan seguros?"; intent = "privacy_info" },
        @{ msg = "Creo que hackearon mi cuenta"; intent = "security_verify" },
        
        # NOTIFICACIONES
        @{ msg = "No me llegan notificaciones"; intent = "notification_settings" },
        @{ msg = "Como configuro las notificaciones?"; intent = "notification_settings" },
        
        # HANDSHAKE
        @{ msg = "Que es un handshake?"; intent = "handshake_info" },
        @{ msg = "Como funciona la compra presencial?"; intent = "handshake_info" },
        
        # TRANSACCIONES
        @{ msg = "Donde veo mis transacciones?"; intent = "purchase_status" },
        @{ msg = "Como confirmo que recibi un producto?"; intent = "purchase_status" },
        
        # SOCIAL
        @{ msg = "Como le doy like a un producto?"; intent = "social_info" },
        @{ msg = "Como sigo a un vendedor?"; intent = "social_info" },
        
        # REVIEWS
        @{ msg = "Como califico a un vendedor?"; intent = "review_info" },
        @{ msg = "Como dejo una resena?"; intent = "review_info" },
        
        # PROBLEMAS TECNICOS
        @{ msg = "La app se cierra sola"; intent = "app_bug" },
        @{ msg = "La aplicacion esta muy lenta"; intent = "app_bug" },
        @{ msg = "No puedo subir fotos"; intent = "app_bug" },
        
        # GENERAL
        @{ msg = "Que es Rendly?"; intent = "general_info" },
        @{ msg = "Es gratis usar Rendly?"; intent = "general_info" },
        @{ msg = "Hola, necesito ayuda"; intent = "greeting" },
        @{ msg = "Gracias por la ayuda"; intent = "farewell" },
        
        # VARIACIONES NATURALES
        @{ msg = "oye como hago para comprar algo?"; intent = "purchase_status" },
        @{ msg = "tengo un problema con mi compra"; intent = "purchase_problem" },
        @{ msg = "pague pero el pedido sigue pendiente"; intent = "payment_problem" },
        @{ msg = "donde esta mi pedido?"; intent = "shipping_info" },
        @{ msg = "quiero empezar a vender"; intent = "sell_how" },
        @{ msg = "no me deja enviar mensajes"; intent = "chat_info" },
        @{ msg = "como recupero mi cuenta?"; intent = "account_access" },
        @{ msg = "la app se traba mucho"; intent = "app_bug" },
        @{ msg = "como subo un video?"; intent = "rends_info" },
        @{ msg = "necesito ayuda con algo"; intent = "greeting" }
    )

    $total = $messages.Count
    $success = 0
    $errors = 0
    $i = 0

    Write-Host ""
    Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "  STEP 1: Sending training messages ($total)" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan

    foreach ($item in $messages) {
        $i++
        $sessionId = "training_$(Get-Random -Minimum 1000 -Maximum 9999)"
        $body = @{
            user_id = "training_bot_$(Get-Random -Minimum 1 -Maximum 50)"
            message = $item.msg
            session_id = $sessionId
        } | ConvertTo-Json -Compress

        try {
            $response = Invoke-WebRequest -Uri "$BaseUrl/ai/support/message" -Method POST -Headers $headers -Body $body -UseBasicParsing -TimeoutSec 30
            $json = $response.Content | ConvertFrom-Json
            $confidence = if ($json.analysis) { $json.analysis.confidence_score } else { "?" }
            $escalated = if ($json.escalated) { "ESC" } else { "OK" }
            $detectedIntent = if ($json.analysis) { $json.analysis.detected_intent } else { "?" }

            $intentMatch = if ($detectedIntent -eq $item.intent) { "MATCH" } else { "MISS" }
            $matchColor = if ($intentMatch -eq "MATCH") { "Green" } else { "Yellow" }

            Write-Host "[$i/$total] " -NoNewline -ForegroundColor Gray
            Write-Host "$escalated " -NoNewline -ForegroundColor $(if ($escalated -eq "OK") { "Green" } else { "Yellow" })
            Write-Host "C:$confidence " -NoNewline -ForegroundColor Cyan
            Write-Host "$intentMatch " -NoNewline -ForegroundColor $matchColor
            Write-Host "| $($item.msg.Substring(0, [Math]::Min(45, $item.msg.Length)))" -ForegroundColor White

            $success++
        } catch {
            Write-Host "[$i/$total] ERR | $($item.msg.Substring(0, [Math]::Min(40, $item.msg.Length)))" -ForegroundColor Red
            $errors++
        }

        Start-Sleep -Milliseconds $MessageDelay
    }

    Write-Host ""
    Write-Host "Messages sent: $success/$total (Errors: $errors)" -ForegroundColor $(if ($errors -gt 0) { "Yellow" } else { "Green" })
}

# ═══ Step 2: Flush buffer to Supabase ═══
if (-not $OnlyTrain) {
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "  STEP 2: Flushing training buffer to Supabase" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan

    try {
        $flushResponse = Invoke-WebRequest -Uri "$BaseUrl/ai/training/flush" -Method POST -Headers $headers -UseBasicParsing -TimeoutSec 30
        $flushJson = $flushResponse.Content | ConvertFrom-Json
        Write-Host "Flushed: $($flushJson.flushed) records" -ForegroundColor Green
        Write-Host "Remaining in buffer: $($flushJson.remaining_buffer)" -ForegroundColor Gray
    } catch {
        Write-Host "Flush error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

if ($OnlyFlush) { exit 0 }

# ═══ Step 3: Trigger training run ═══
Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  STEP 3: Training ML model (TF-IDF + SVM)" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "This may take a minute..." -ForegroundColor Gray

try {
    $trainBody = @{ run_type = "full" } | ConvertTo-Json -Compress
    $trainResponse = Invoke-WebRequest -Uri "$BaseUrl/ai/training/run" -Method POST -Headers $headers -Body $trainBody -UseBasicParsing -TimeoutSec 120
    $trainJson = $trainResponse.Content | ConvertFrom-Json

    Write-Host ""
    Write-Host "═══════════════════════════════════════════════" -ForegroundColor Green
    Write-Host "  TRAINING COMPLETED" -ForegroundColor Green
    Write-Host "═══════════════════════════════════════════════" -ForegroundColor Green
    Write-Host "  Status:        $($trainJson.status)" -ForegroundColor White
    Write-Host "  Run ID:        $($trainJson.run_id)" -ForegroundColor Gray
    Write-Host "  Duration:      $($trainJson.duration_seconds)s" -ForegroundColor White

    Write-Host ""
    Write-Host "  Dataset:" -ForegroundColor Cyan
    Write-Host "    Train:       $($trainJson.training_samples)" -ForegroundColor White
    Write-Host "    Validation:  $($trainJson.validation_samples)" -ForegroundColor White
    Write-Host "    Test:        $($trainJson.test_samples)" -ForegroundColor White
    Write-Host "    Intents:     $($trainJson.total_intents)" -ForegroundColor White

    if ($trainJson.final_metrics) {
        $fm = $trainJson.final_metrics
        Write-Host ""
        Write-Host "  Metrics:" -ForegroundColor Cyan
        
        $accuracyColor = if ($fm.accuracy -ge 0.8) { "Green" } elseif ($fm.accuracy -ge 0.6) { "Yellow" } else { "Red" }
        Write-Host "    Accuracy:    $([math]::Round($fm.accuracy * 100, 1))%" -ForegroundColor $accuracyColor
        Write-Host "    F1 Score:    $([math]::Round($fm.f1 * 100, 1))%" -ForegroundColor $accuracyColor
        Write-Host "    Precision:   $([math]::Round($fm.precision * 100, 1))%" -ForegroundColor White
        Write-Host "    Recall:      $([math]::Round($fm.recall * 100, 1))%" -ForegroundColor White
        Write-Host "    Misclassified: $($fm.misclassified_count)" -ForegroundColor $(if ($fm.misclassified_count -gt 0) { "Yellow" } else { "Green" })
    }

    if ($trainJson.baseline_metrics) {
        $bm = $trainJson.baseline_metrics
        Write-Host ""
        Write-Host "  Baseline (previous model):" -ForegroundColor Cyan
        Write-Host "    Accuracy:    $([math]::Round($bm.accuracy * 100, 1))%" -ForegroundColor Gray
        Write-Host "    F1 Score:    $([math]::Round($bm.f1 * 100, 1))%" -ForegroundColor Gray
    }

    if ($trainJson.improvement -and $trainJson.improvement.accuracy) {
        $imp = $trainJson.improvement
        $impColor = if ($imp.accuracy -ge 0) { "Green" } else { "Red" }
        Write-Host ""
        Write-Host "  Improvement:" -ForegroundColor Cyan
        Write-Host "    Accuracy:  $(if ($imp.accuracy -ge 0) { '+' })$([math]::Round($imp.accuracy * 100, 1))%" -ForegroundColor $impColor
        Write-Host "    F1:        $(if ($imp.f1 -ge 0) { '+' })$([math]::Round($imp.f1 * 100, 1))%" -ForegroundColor $impColor
    }

    Write-Host ""
    Write-Host "  Files:" -ForegroundColor Cyan
    Write-Host "    Model:   $($trainJson.model_path)" -ForegroundColor Gray
    Write-Host "    Dataset: $($trainJson.dataset_path)" -ForegroundColor Gray
    Write-Host "    Report:  $($trainJson.report_path)" -ForegroundColor Gray

} catch {
    Write-Host "Training error: $($_.Exception.Message)" -ForegroundColor Red
}

# ═══ Step 4: Show current metrics ═══
Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  CURRENT PIPELINE STATUS" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Cyan

try {
    $metricsResponse = Invoke-WebRequest -Uri "$BaseUrl/ai/training/metrics?hours=24" -Method GET -Headers $headers -UseBasicParsing -TimeoutSec 15
    $metricsJson = $metricsResponse.Content | ConvertFrom-Json
    $lm = $metricsJson.live_metrics
    $ps = $metricsJson.pipeline_stats

    Write-Host "  Messages (24h):    $($lm.total_messages)" -ForegroundColor White
    Write-Host "  AI Resolved:       $($lm.ai_resolved)" -ForegroundColor Green
    Write-Host "  Escalated:         $($lm.escalated)" -ForegroundColor Yellow
    Write-Host "  Avg Confidence:    $($lm.avg_confidence)%" -ForegroundColor Cyan
    Write-Host "  Escalation Rate:   $($lm.escalation_rate)%" -ForegroundColor $(if ($lm.escalation_rate -gt 30) { "Red" } else { "Green" })
    Write-Host "  Model Trained:     $($ps.model_trained)" -ForegroundColor $(if ($ps.model_trained) { "Green" } else { "Red" })
    Write-Host "  Model Intents:     $($ps.model_intents)" -ForegroundColor White
} catch {
    Write-Host "Metrics fetch error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Green
Write-Host "  DONE" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
