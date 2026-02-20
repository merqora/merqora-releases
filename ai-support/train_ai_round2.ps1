$baseUrl = "https://merqora-releases-production.up.railway.app/ai/support/message"
$headers = @{ "Content-Type" = "application/json" }

$messages = @(
    # ═══ RONDA 2: PREGUNTAS COMPLEJAS Y CONVERSACIONALES ═══
    
    # --- Compras avanzadas ---
    "compre un producto hace 3 dias y todavia dice pendiente, que pasa?",
    "pague con mercado pago pero la app dice que no se proceso el pago",
    "me cobraron pero el vendedor dice que no le llego el pago",
    "quiero comprar algo pero me da error al poner la tarjeta",
    "puedo comprar desde otro pais?",
    "como se si un vendedor es confiable?",
    "el precio cambio despues de que lo agregue al carrito",
    "puedo hacer ofertas a los vendedores?",
    "hay alguna garantia por las compras?",
    "que pasa si el vendedor no envia el producto?",
    "cuanto tiempo tiene el vendedor para enviar?",
    "puedo cancelar el pedido si el vendedor no envio?",
    "como funciona la proteccion al comprador?",
    "me llego un producto falso que hago?",
    "el producto tiene un defecto de fabrica",
    "quiero cambiar el producto por otra talla",
    "me equivoque de producto como lo cambio?",
    "puedo comprar varios productos de diferentes vendedores?",
    "el carrito me muestra productos agotados",
    "como se si un producto tiene stock?",
    
    # --- Ventas avanzadas ---
    "como pongo envio gratis en mis productos?",
    "puedo ofrecer descuentos en mis publicaciones?",
    "como creo una oferta o promocion?",
    "que porcentaje se queda Rendly de mis ventas?",
    "cuando depositan el dinero de mis ventas?",
    "como configuro el envio de mis productos?",
    "puedo vender servicios ademas de productos?",
    "como destaco mi publicacion para que la vean mas?",
    "mis publicaciones no aparecen en la busqueda",
    "como mejoro la visibilidad de mis productos?",
    "puedo programar una publicacion?",
    "como agrego variantes a mi producto? como tallas o colores",
    "se me borro una publicacion sin razon",
    "por que me eliminaron una publicacion?",
    "cuales son las reglas para publicar?",
    "puedo publicar productos de dropshipping?",
    "como calculo los costos de envio?",
    "me llego una queja de un comprador que hago?",
    "el comprador dice que no le llego pero si envie",
    "como me protejo de compradores falsos?",
    
    # --- Cuenta y seguridad avanzada ---
    "como activo la verificacion en dos pasos?",
    "perdi el telefono y no puedo acceder a mi cuenta",
    "quiero cambiar el email de mi cuenta",
    "me llegan correos sospechosos de Rendly",
    "alguien creo una cuenta con mis datos",
    "como reporto una cuenta falsa?",
    "me estan suplantando la identidad",
    "como vinculo mi cuenta de Rendly con redes sociales?",
    "puedo tener dos cuentas?",
    "como transfiero mis datos a otra cuenta?",
    "la verificacion por SMS no me llega",
    "no me llega el correo de verificacion",
    "mi cuenta fue suspendida por que?",
    "como apelo una suspension de cuenta?",
    "que pasa con mis ventas si suspenden mi cuenta?",
    
    # --- Chat y comunicacion avanzada ---
    "como envio fotos por el chat?",
    "puedo enviar archivos PDF por el chat?",
    "como envio mi ubicacion por el chat?",
    "se puede crear un grupo de chat?",
    "como comparto un producto por el chat?",
    "el vendedor me pidio que pague fuera de la app, esta bien?",
    "me estan acosando por el chat que hago?",
    "como reporto mensajes ofensivos?",
    "puedo programar mensajes automaticos?",
    "como se si el vendedor vio mi mensaje?",
    "hay doble check como en whatsapp?",
    "puedo grabar un mensaje de voz?",
    
    # --- Rends y contenido avanzado ---
    "como hago un rend con varias fotos y video?",
    "puedo subir un rend desde la galeria?",
    "que resolucion recomiendan para los videos?",
    "como hago un timelapse en Rendly?",
    "se puede hacer slowmotion?",
    "como agrego transiciones entre clips?",
    "puedo usar mi propia musica en un rend?",
    "hay copyright en la musica de la app?",
    "como hago que mi rend se haga viral?",
    "puedo compartir mi rend en otras redes?",
    "como descargo mi propio rend?",
    "alguien subio mi rend sin permiso",
    "como reporto contenido robado?",
    "se puede colaborar en un rend con otro usuario?",
    "como hago un dueto o remix?",
    "que es el feed de rends?",
    "como funciona el algoritmo de Rendly?",
    "por que mis rends tienen pocas vistas?",
    "como gano mas seguidores?",
    
    # --- Historias avanzadas ---
    "como agrego una encuesta a mi historia?",
    "se puede responder a una historia?",
    "como veo quien vio mi historia?",
    "puedo ocultar mi historia de ciertas personas?",
    "como destaco una historia en mi perfil?",
    "las historias destacadas desaparecen?",
    "puedo compartir una historia como publicacion?",
    
    # --- Handshake y presencial ---
    "como funciona el sistema de reputacion?",
    "que significan las estrellas del vendedor?",
    "como mejoro mi reputacion como vendedor?",
    "el comprador no confirmo el handshake que hago?",
    "nos encontramos pero el QR no funciona",
    "la app no detecta que estamos cerca",
    "puedo hacer handshake sin GPS?",
    "que pasa si no llegamos al punto de encuentro?",
    "es seguro encontrarse en persona para compras?",
    "donde es mejor encontrarse para una compra presencial?",
    
    # --- Zonas y localizacion ---
    "como busco productos cerca de mi?",
    "puedo filtrar por zona o ciudad?",
    "como cambio mi zona de busqueda?",
    "no aparecen productos en mi zona",
    "puedo comprar de otra ciudad?",
    
    # --- Problemas tecnicos avanzados ---
    "la app no funciona con mi version de Android",
    "que version minima de Android necesito?",
    "puedo usar Rendly en tablet?",
    "la app consume muchos datos moviles",
    "como reduzco el consumo de datos?",
    "los videos se ven pixelados",
    "la app no tiene sonido",
    "el editor se cierra cuando agrego muchos efectos",
    "como limpio la cache de la app?",
    "la app pide permisos que no quiero dar",
    "por que la app necesita acceso a la camara?",
    "por que la app necesita acceso al microfono?",
    "la app se actualizo y perdí mis datos",
    "como reporto un bug?",
    "hay una version beta de la app?",
    
    # --- Categorias especificas de productos ---
    "como busco ropa en Rendly?",
    "hay seccion de electronica?",
    "donde estan los productos de belleza?",
    "como filtro por categoria?",
    "como ordeno los resultados por precio?",
    "puedo guardar busquedas?",
    "como uso los filtros de busqueda?",
    "no encuentro lo que busco",
    
    # --- Preguntas sobre Rendly como empresa ---
    "Rendly tiene oficinas fisicas?",
    "como contacto a Rendly por telefono?",
    "tienen email de soporte?",
    "donde puedo dejar una sugerencia?",
    "hay programa de afiliados?",
    "puedo trabajar en Rendly?",
    "Rendly tiene app para iPhone?",
    "cuando sale la version para iOS?",
    "Rendly tiene pagina web?",
    
    # --- Situaciones de emergencia ---
    "me robaron y usaron mi cuenta para comprar",
    "encontre un producto ilegal en la app",
    "hay un vendedor vendiendo cosas robadas",
    "me amenazaron por el chat",
    "un menor esta vendiendo en la app",
    "encontre contenido inapropiado",
    
    # --- Preguntas con emociones ---
    "estoy muy frustrado con la app nada funciona",
    "llevo dias esperando respuesta del vendedor estoy harto",
    "excelente app me encanta Rendly!",
    "quiero felicitar a un vendedor como hago?",
    "la peor experiencia de compra de mi vida",
    "me siento estafado nadie me ayuda",
    "esto es una estafa? parece demasiado barato",
    "muchas gracias me resolvieron rapido el problema",
    
    # --- Preguntas combinadas ---
    "compre algo ayer y el vendedor me bloqueo que hago?",
    "quiero vender mi celular usado como lo publico y cuanto me cobran?",
    "se me borro la app y perdi el acceso a mi cuenta y tengo pedidos pendientes",
    "como le doy seguimiento a mi envio y cuando puedo reclamar si no llega?",
    "quiero empezar a vender pero no se que necesito ni como funciona el pago",
    "mi hijo menor quiere vender en Rendly tiene permitido?",
    "vivo en el exterior puedo comprar y que me envien a mi pais?"
)

$total = $messages.Count
$success = 0
$errors = 0
$okCount = 0
$escCount = 0
$i = 0

Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ENTRENAMIENTO IA RENDLY - RONDA 2 (AVANZADO)" -ForegroundColor Cyan
Write-Host "  Total de mensajes: $total" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

foreach ($msg in $messages) {
    $i++
    $sessionId = "train2_session_$(Get-Random -Minimum 1000 -Maximum 9999)"
    $body = @{
        user_id = "training_bot_$(Get-Random -Minimum 1 -Maximum 100)"
        message = $msg
        session_id = $sessionId
    } | ConvertTo-Json -Compress
    
    try {
        $response = Invoke-WebRequest -Uri $baseUrl -Method POST -Headers $headers -Body $body -UseBasicParsing -TimeoutSec 30
        $json = $response.Content | ConvertFrom-Json
        $confidence = if ($json.analysis) { $json.analysis.confidence_score } else { "?" }
        $escalated = if ($json.escalated) { "ESC" } else { "OK" }
        $intent = if ($json.analysis) { $json.analysis.detected_intent } else { "?" }
        
        if ($escalated -eq "OK") { $okCount++ } else { $escCount++ }
        
        Write-Host "[$i/$total] " -NoNewline -ForegroundColor Gray
        if ($escalated -eq "OK") {
            Write-Host "$escalated " -NoNewline -ForegroundColor Green
        } else {
            Write-Host "$escalated " -NoNewline -ForegroundColor Yellow
        }
        Write-Host "C:$confidence " -NoNewline -ForegroundColor Cyan
        Write-Host "I:$intent " -NoNewline -ForegroundColor Magenta
        Write-Host "| $($msg.Substring(0, [Math]::Min(55, $msg.Length)))" -ForegroundColor White
        
        $success++
    } catch {
        Write-Host "[$i/$total] ERR | $($msg.Substring(0, [Math]::Min(40, $msg.Length))) - $($_.Exception.Message)" -ForegroundColor Red
        $errors++
    }
    
    Start-Sleep -Milliseconds 200
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  RONDA 2 COMPLETADA" -ForegroundColor Green
Write-Host "  Exitosos: $success / $total" -ForegroundColor Green
Write-Host "  Respondidos por IA: $okCount" -ForegroundColor Green
Write-Host "  Escalados: $escCount" -ForegroundColor Yellow
Write-Host "  Errores: $errors" -ForegroundColor $(if ($errors -gt 0) { "Red" } else { "Green" })
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan

# Obtener stats finales
try {
    $statsResp = Invoke-WebRequest -Uri "https://merqora-releases-production.up.railway.app/ai/support/stats" -UseBasicParsing
    $stats = $statsResp.Content | ConvertFrom-Json
    Write-Host ""
    Write-Host "═══ STATS FINALES DE LA IA ═══" -ForegroundColor Magenta
    Write-Host "  Cache: $($stats.response_cache_size) respuestas" -ForegroundColor White
    Write-Host "  Patrones: $($stats.learned_patterns_count) aprendidos" -ForegroundColor White
    Write-Host "  Modelo local: $($stats.local_ai.training_examples) ejemplos" -ForegroundColor White
    Write-Host "  Intents: $($stats.local_ai.intents_count) diferentes" -ForegroundColor White
} catch {
    Write-Host "No se pudieron obtener stats" -ForegroundColor Gray
}
