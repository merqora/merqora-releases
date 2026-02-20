$baseUrl = "https://merqora-releases-production.up.railway.app/ai/support/message"
$headers = @{ "Content-Type" = "application/json" }

$messages = @(
    # ═══ COMPRAS Y PAGOS ═══
    "Como puedo comprar un producto en Rendly?",
    "Quiero comprar algo pero no se como",
    "Como agrego algo al carrito?",
    "Donde veo mi carrito de compras?",
    "Como pago un producto?",
    "Que metodos de pago aceptan?",
    "Puedo pagar con tarjeta de credito?",
    "Aceptan Mercado Pago?",
    "Como pago con tarjeta de debito?",
    "Puedo pagar en cuotas?",
    "Cuantas cuotas puedo elegir?",
    "El pago con tarjeta es seguro?",
    "Me cobraron dos veces por una compra",
    "No me llega la confirmacion de pago",
    "El pago fue rechazado que hago?",
    "Como cancelo una compra?",
    "Quiero un reembolso",
    "Como pido que me devuelvan el dinero?",
    "Cuanto tarda en llegar el reembolso?",
    "Mi pago quedo pendiente",
    
    # ═══ ENVIOS Y DIRECCIONES ═══
    "Como agrego una direccion de envio?",
    "Donde pongo mi direccion?",
    "Como cambio mi direccion de entrega?",
    "Puedo tener varias direcciones guardadas?",
    "Como elimino una direccion?",
    "Cuanto tarda el envio?",
    "Tiene envio gratis?",
    "Como hago seguimiento de mi pedido?",
    "Donde veo el estado de mi envio?",
    "Mi pedido no llega",
    "El paquete llego danado",
    "Me enviaron un producto equivocado",
    "Como hago una devolucion?",
    "Cuanto cuesta el envio?",
    "Hacen envios a todo el pais?",
    
    # ═══ CUENTA Y PERFIL ═══
    "Como creo una cuenta en Rendly?",
    "Como me registro?",
    "No puedo iniciar sesion",
    "Olvide mi contrasena",
    "Como cambio mi contrasena?",
    "Como cambio mi nombre de usuario?",
    "Como cambio mi foto de perfil?",
    "Como edito mi perfil?",
    "Como elimino mi cuenta?",
    "Quiero borrar mi cuenta",
    "Como verifico mi cuenta?",
    "Que es la verificacion de cuenta?",
    "Como activo la autenticacion de dos factores?",
    "Que es 2FA?",
    "Como protejo mi cuenta?",
    "Alguien entro a mi cuenta sin permiso",
    "Creo que hackearon mi cuenta",
    "Como cierro sesion en todos los dispositivos?",
    
    # ═══ VENDER PRODUCTOS ═══
    "Como vendo en Rendly?",
    "Quiero publicar un producto para vender",
    "Como publico un articulo?",
    "Como subo fotos de mi producto?",
    "Cuantas fotos puedo subir?",
    "Como pongo el precio a mi producto?",
    "Como edito una publicacion?",
    "Como elimino una publicacion?",
    "Donde veo mis ventas?",
    "Como veo el historial de ventas?",
    "Como marco un pedido como enviado?",
    "Cuando me pagan por mis ventas?",
    "Como recibo el dinero de mis ventas?",
    "Como me convierto en vendedor verificado?",
    "Que beneficios tiene ser vendedor verificado?",
    
    # ═══ CHAT Y MENSAJES ═══
    "Como le envio un mensaje a un vendedor?",
    "Donde estan mis mensajes?",
    "No me llegan las notificaciones de mensajes",
    "Como hago una consulta sobre un producto?",
    "El vendedor no me responde",
    "Como bloqueo a un usuario?",
    "Como reporto a un usuario?",
    "Me estan enviando spam",
    "Como silencio una conversacion?",
    "Como borro un mensaje?",
    "Se puede hacer llamadas en Rendly?",
    "Como llamo a un vendedor?",
    
    # ═══ HISTORIAS Y RENDS ═══
    "Que son las historias en Rendly?",
    "Como subo una historia?",
    "Cuanto duran las historias?",
    "Las historias desaparecen?",
    "Que es un Rend?",
    "Como creo un Rend?",
    "Como edito un video en Rendly?",
    "Se puede agregar musica a los Rends?",
    "Como agrego filtros a mi video?",
    "Como recorto un video?",
    "Que herramientas tiene el editor de video?",
    "Como agrego texto a un Rend?",
    "Se pueden agregar stickers?",
    
    # ═══ SEGURIDAD Y PRIVACIDAD ═══
    "Mis datos estan seguros en Rendly?",
    "Como cambio la privacidad de mi perfil?",
    "Puedo hacer mi perfil privado?",
    "Quien puede ver mis publicaciones?",
    "Como oculto mi actividad?",
    "No quiero que vean cuando estoy en linea",
    "Como desactivo el estado en linea?",
    "Que informacion recopila Rendly?",
    "Como protejo mi informacion personal?",
    
    # ═══ NOTIFICACIONES ═══
    "No me llegan notificaciones",
    "Como activo las notificaciones?",
    "Recibo demasiadas notificaciones",
    "Como desactivo las notificaciones?",
    "Como configuro que notificaciones recibir?",
    "No me avisa cuando me escriben",
    "Las notificaciones de compras no funcionan",
    
    # ═══ HANDSHAKE Y COMPRA PRESENCIAL ═══
    "Que es un handshake en Rendly?",
    "Como funciona la compra presencial?",
    "Como confirmo una compra en persona?",
    "Que es el codigo QR para compras?",
    "El handshake no funciona sin internet",
    "Como se hace un handshake offline?",
    
    # ═══ TRANSACCIONES ═══
    "Donde veo mis transacciones?",
    "Como veo el historial de compras?",
    "Quiero ver todas mis compras anteriores",
    "Como veo el detalle de una orden?",
    "Como confirmo que recibi un producto?",
    "El vendedor dice que envio pero no me llega",
    "Como marco como recibido?",
    "Donde esta el numero de seguimiento?",
    
    # ═══ LIKES, SAVES, SEGUIDORES ═══
    "Como le doy like a un producto?",
    "Donde veo mis productos guardados?",
    "Como guardo un producto para despues?",
    "Como sigo a un vendedor?",
    "Donde veo a quien sigo?",
    "Donde estan mis seguidores?",
    "Como dejo de seguir a alguien?",
    
    # ═══ REVIEWS Y CALIFICACIONES ═══
    "Como califico a un vendedor?",
    "Como dejo una resena de un producto?",
    "Donde veo las resenas de un producto?",
    "El vendedor tiene malas resenas",
    "Quiero cambiar mi calificacion",
    "Como se calculan las estrellas?",
    
    # ═══ PROBLEMAS TECNICOS ═══
    "La app se cierra sola",
    "La aplicacion esta muy lenta",
    "No carga la pagina principal",
    "Las fotos no se ven",
    "No puedo subir fotos",
    "La camara no funciona dentro de la app",
    "No puedo reproducir videos",
    "El editor de video no responde",
    "La app consume mucha bateria",
    "La app ocupa mucho espacio",
    "Como actualizo la app?",
    "Tengo la ultima version?",
    
    # ═══ LIVES Y STREAMING ═══
    "Como hago un en vivo en Rendly?",
    "Se puede hacer streaming?",
    "Que necesito para hacer un live?",
    "Cuantas personas pueden ver mi live?",
    
    # ═══ GENERAL ═══
    "Que es Rendly?",
    "Para que sirve Rendly?",
    "Es gratis usar Rendly?",
    "Rendly cobra comision?",
    "Como contacto al soporte de Rendly?",
    "Necesito ayuda urgente",
    "Hola, necesito ayuda",
    "Tienen atencion al cliente?",
    "Cuales son los terminos de uso?",
    "Tienen politica de privacidad?",
    "En que paises esta disponible Rendly?",
    "Hay version web de Rendly?",
    "Se puede usar en computadora?",
    
    # ═══ SEGUNDA RONDA - VARIACIONES NATURALES ═══
    "oye como hago para comprar algo?",
    "necesito saber como pagar",
    "me podrias explicar como funciona el pago?",
    "tengo un problema con mi compra",
    "hice una compra y no aparece",
    "pague pero el pedido sigue pendiente",
    "como hago para que me devuelvan la plata?",
    "donde esta mi pedido?",
    "ya pague hace dias y no me llega nada",
    "el vendedor no envia mi producto",
    "quiero reclamar por un producto",
    "el producto que recibi no es lo que pedi",
    "me estafaron en una compra",
    "como denuncio a un vendedor?",
    "como puedo vender mis cosas?",
    "quiero empezar a vender",
    "cuanto me cobran por vender?",
    "hay comision por venta?",
    "como le pongo precio a lo que vendo?",
    "puedo vender productos usados?",
    "se puede vender ropa usada?",
    "que tipo de productos puedo vender?",
    "hay productos prohibidos?",
    "como me pagan cuando vendo algo?",
    "como funciona el chat?",
    "no me deja enviar mensajes",
    "como hablo con el vendedor?",
    "el chat no carga",
    "se borraron mis mensajes",
    "como hago videollamada?",
    "como cambio mi foto?",
    "no me acuerdo la clave",
    "como recupero mi cuenta?",
    "me hackearon la cuenta que hago?",
    "como pongo perfil privado?",
    "no quiero que me sigan desconocidos",
    "como subo un video?",
    "el video no se sube",
    "cuanto puede durar un rend?",
    "que formatos de video acepta?",
    "como corto un video?",
    "puedo dividir un video en dos partes?",
    "como le pongo filtros al video?",
    "como ajusto el brillo del video?",
    "se puede extraer el audio de un video?",
    "como duplico un clip?",
    "como elimino parte de un video?",
    "como cambio la velocidad del video?",
    "la app se traba mucho",
    "se congela cuando abro la camara",
    "no tengo espacio para la app",
    "como libero espacio?",
    "las notificaciones no suenan",
    "no me llegan los mensajes push",
    "como activo las push notifications?",
    "donde configuro las notificaciones?",
    "hola",
    "buenas",
    "buenos dias",
    "buenas tardes",
    "necesito ayuda con algo",
    "tengo una consulta",
    "me pueden ayudar?",
    "hay alguien ahi?",
    "gracias por la ayuda",
    "eso era todo gracias",
    "ya me funciono gracias",
    "excelente me sirvio la info"
)

$total = $messages.Count
$success = 0
$errors = 0
$i = 0

Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ENTRENAMIENTO MASIVO DE IA RENDLY" -ForegroundColor Cyan
Write-Host "  Total de mensajes: $total" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

foreach ($msg in $messages) {
    $i++
    $sessionId = "training_session_$(Get-Random -Minimum 1000 -Maximum 9999)"
    $body = @{
        user_id = "training_bot_$(Get-Random -Minimum 1 -Maximum 50)"
        message = $msg
        session_id = $sessionId
    } | ConvertTo-Json -Compress
    
    try {
        $response = Invoke-WebRequest -Uri $baseUrl -Method POST -Headers $headers -Body $body -UseBasicParsing -TimeoutSec 30
        $json = $response.Content | ConvertFrom-Json
        $confidence = if ($json.analysis) { $json.analysis.confidence_score } else { "?" }
        $escalated = if ($json.escalated) { "ESC" } else { "OK" }
        $intent = if ($json.analysis) { $json.analysis.detected_intent } else { "?" }
        
        Write-Host "[$i/$total] " -NoNewline -ForegroundColor Gray
        if ($escalated -eq "OK") {
            Write-Host "$escalated " -NoNewline -ForegroundColor Green
        } else {
            Write-Host "$escalated " -NoNewline -ForegroundColor Yellow
        }
        Write-Host "C:$confidence " -NoNewline -ForegroundColor Cyan
        Write-Host "I:$intent " -NoNewline -ForegroundColor Magenta
        Write-Host "| $($msg.Substring(0, [Math]::Min(50, $msg.Length)))" -ForegroundColor White
        
        $success++
    } catch {
        Write-Host "[$i/$total] ERR | $($msg.Substring(0, [Math]::Min(40, $msg.Length))) - $($_.Exception.Message)" -ForegroundColor Red
        $errors++
    }
    
    # Pequena pausa para no saturar
    Start-Sleep -Milliseconds 200
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ENTRENAMIENTO COMPLETADO" -ForegroundColor Green
Write-Host "  Exitosos: $success / $total" -ForegroundColor Green
Write-Host "  Errores: $errors" -ForegroundColor $(if ($errors -gt 0) { "Red" } else { "Green" })
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
