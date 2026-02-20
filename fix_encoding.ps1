$basePath = "c:\Users\Rodrigo\Documents\Rendly\app\src\main\java\com\rendly\app"
$rc = [char]0xFFFD
$utf8 = New-Object System.Text.UTF8Encoding($false)
$files = Get-ChildItem -Path $basePath -Recurse -Filter "*.kt"

# PHASE 1: Fix mojibake (double-encoded UTF-8)
Write-Host "=== PHASE 1: Mojibake ==="
foreach ($f in $files) {
    $c = [System.IO.File]::ReadAllText($f.FullName, $utf8)
    $o = $c
    $c=$c.Replace([string][char]0xC3+[char]0xA1,"a`u0301")  # wrong approach
    # Use direct string literals instead
    $c = $o
    $c = $c.Replace("Ã¡","á").Replace("Ã©","é").Replace("Ã­","í").Replace("Ã³","ó").Replace("Ãº","ú")
    $c = $c.Replace("Ã±","ñ").Replace("Ã¼","ü").Replace("Â¿","¿").Replace("Â¡","¡")
    if ($c -ne $o) {
        [System.IO.File]::WriteAllText($f.FullName, $c, $utf8)
        Write-Host "  Moji: $($f.Name)"
    }
}

# PHASE 2: Fix remaining U+FFFD
Write-Host "=== PHASE 2: FFFD ==="
# Reload files after phase 1
foreach ($f in $files) {
    $c = [System.IO.File]::ReadAllText($f.FullName, $utf8)
    if (-not $c.Contains($rc)) { continue }
    $o = $c
    $before = ($c.ToCharArray()|?{$_ -eq $rc}).Count
    
    # ción patterns (most common)
    $c = $c -replace "cci${rc}n","cción"; $c = $c -replace "aci${rc}n","ación"
    $c = $c -replace "uci${rc}n","ución"; $c = $c -replace "ici${rc}n","ición"
    $c = $c -replace "isi${rc}n","isión"; $c = $c -replace "ecci${rc}n","ección"
    $c = $c -replace "Cci${rc}n","Cción"; $c = $c -replace "Aci${rc}n","Ación"
    
    # á patterns
    $c = $c -replace "m${rc}s\b","más"; $c = $c -replace "M${rc}s\b","Más"
    $c = $c -replace "est${rc}\b","está"; $c = $c -replace "Est${rc}\b","Está"
    $c = $c -replace "ser${rc}\b","será"; $c = $c -replace "har${rc}\b","hará"
    $c = $c -replace "tendr${rc}\b","tendrá"; $c = $c -replace "podr${rc}\b","podrá"
    $c = $c -replace "categor${rc}a","categoría"; $c = $c -replace "Categor${rc}a","Categoría"
    $c = $c -replace "garant${rc}a","garantía"; $c = $c -replace "Garant${rc}a","Garantía"
    $c = $c -replace "pol${rc}tica","política"; $c = $c -replace "Pol${rc}tica","Política"
    $c = $c -replace "autom${rc}tic","automátic"; $c = $c -replace "din${rc}mic","dinámic"
    $c = $c -replace "gr${rc}fic","gráfic"; $c = $c -replace "cl${rc}sic","clásic"
    $c = $c -replace "b${rc}sic","básic"; $c = $c -replace "pr${rc}ctic","práctic"
    $c = $c -replace "r${rc}pid","rápid"; $c = $c -replace "v${rc}lid","válid"
    $c = $c -replace "p${rc}gina","página"; $c = $c -replace "P${rc}gina","Página"
    $c = $c -replace "c${rc}mara","cámara"; $c = $c -replace "C${rc}mara","Cámara"
    $c = $c -replace "car${rc}cter","carácter"
    $c = $c -replace "adem${rc}s","además"; $c = $c -replace "atr${rc}s","atrás"
    $c = $c -replace "m${rc}xim","máxim"; $c = $c -replace "M${rc}xim","Máxim"
    $c = $c -replace "tambi${rc}n","también"; $c = $c -replace "Tambi${rc}n","También"
    $c = $c -replace "inform${rc}tic","informátic"
    $c = $c -replace "tem${rc}tic","temátic"
    $c = $c -replace "problem${rc}tic","problemátic"
    $c = $c -replace "sistem${rc}tic","sistemátic"
    $c = $c -replace "est${rc}ndar","estándar"
    $c = $c -replace "di${rc}logo","diálogo"
    $c = $c -replace "${rc}rea\b","área"
    
    # ó patterns
    $c = $c -replace "c${rc}digo","código"; $c = $c -replace "C${rc}digo","Código"
    $c = $c -replace "c${rc}mo","cómo"; $c = $c -replace "C${rc}mo","Cómo"
    $c = $c -replace "t${rc}tulo","título"; $c = $c -replace "T${rc}tulo","Título"
    $c = $c -replace "bot${rc}n","botón"; $c = $c -replace "Bot${rc}n","Botón"
    $c = $c -replace "m${rc}vil","móvil"; $c = $c -replace "M${rc}vil","Móvil"
    $c = $c -replace "m${rc}dulo","módulo"
    $c = $c -replace "Pr${rc}xim","Próxim"; $c = $c -replace "pr${rc}xim","próxim"
    $c = $c -replace "Electr${rc}nic","Electrónic"; $c = $c -replace "electr${rc}nic","electrónic"
    $c = $c -replace "tel${rc}fono","teléfono"; $c = $c -replace "Tel${rc}fono","Teléfono"
    $c = $c -replace "peri${rc}dic","periódic"
    
    # í patterns
    $c = $c -replace "a${rc}n\b","aún"; $c = $c -replace "A${rc}n\b","Aún"
    $c = $c -replace "espec${rc}fic","específic"; $c = $c -replace "Espec${rc}fic","Específic"
    $c = $c -replace "n${rc}mero","número"; $c = $c -replace "N${rc}mero","Número"
    $c = $c -replace "m${rc}nim","mínim"; $c = $c -replace "M${rc}nim","Mínim"
    $c = $c -replace "aut${rc}ntic","auténtic"
    $c = $c -replace "art${rc}culo","artículo"; $c = $c -replace "Art${rc}culo","Artículo"
    $c = $c -replace "per${rc}odo","período"
    $c = $c -replace "env${rc}o","envío"; $c = $c -replace "Env${rc}o","Envío"
    $c = $c -replace "vac${rc}o","vacío"; $c = $c -replace "vac${rc}a","vacía"
    $c = $c -replace "d${rc}as","días"; $c = $c -replace "D${rc}as","Días"
    $c = $c -replace "d${rc}a\b","día"; $c = $c -replace "D${rc}a\b","Día"
    $c = $c -replace "gu${rc}a","guía"; $c = $c -replace "Gu${rc}a","Guía"
    $c = $c -replace "tecnolog${rc}a","tecnología"; $c = $c -replace "Tecnolog${rc}a","Tecnología"
    $c = $c -replace "biograf${rc}a","biografía"; $c = $c -replace "Biograf${rc}a","Biografía"
    $c = $c -replace "fotograf${rc}a","fotografía"; $c = $c -replace "Fotograf${rc}a","Fotografía"
    $c = $c -replace "energ${rc}a","energía"; $c = $c -replace "Energ${rc}a","Energía"
    $c = $c -replace "econom${rc}a","economía"
    $c = $c -replace "joyer${rc}a","joyería"; $c = $c -replace "Joyer${rc}a","Joyería"
    $c = $c -replace "mercanc${rc}a","mercancía"
    $c = $c -replace "estad${rc}stic","estadístic"; $c = $c -replace "Estad${rc}stic","Estadístic"
    $c = $c -replace "c${rc}rculo","círculo"
    $c = $c -replace "s${rc}lo\b","sólo"
    
    # ú patterns
    $c = $c -replace "m${rc}ltiple","múltiple"
    $c = $c -replace "b${rc}squeda","búsqueda"; $c = $c -replace "B${rc}squeda","Búsqueda"
    $c = $c -replace "${rc}ltim","últim"; $c = $c -replace "${rc}nic","únic"
    $c = $c -replace "${rc}til\b","útil"
    $c = $c -replace "com${rc}n","común"; $c = $c -replace "seg${rc}n","según"; $c = $c -replace "Seg${rc}n","Según"
    $c = $c -replace "ning${rc}n","ningún"; $c = $c -replace "alg${rc}n","algún"
    $c = $c -replace "men${rc}\b","menú"
    
    # ñ patterns
    $c = $c -replace "espa${rc}ol","español"; $c = $c -replace "Espa${rc}ol","Español"
    $c = $c -replace "a${rc}o\b","año"; $c = $c -replace "A${rc}o\b","Año"
    $c = $c -replace "dise${rc}o","diseño"; $c = $c -replace "Dise${rc}o","Diseño"
    $c = $c -replace "peque${rc}o","pequeño"; $c = $c -replace "peque${rc}a","pequeña"
    $c = $c -replace "tama${rc}o","tamaño"; $c = $c -replace "Tama${rc}o","Tamaño"
    $c = $c -replace "se${rc}al","señal"; $c = $c -replace "Se${rc}al","Señal"
    $c = $c -replace "due${rc}o","dueño"; $c = $c -replace "ni${rc}o","niño"; $c = $c -replace "ni${rc}a","niña"
    $c = $c -replace "sue${rc}o","sueño"; $c = $c -replace "ba${rc}o","baño"; $c = $c -replace "da${rc}o","daño"
    $c = $c -replace "contrase${rc}a","contraseña"; $c = $c -replace "Contrase${rc}a","Contraseña"
    $c = $c -replace "rese${rc}a","reseña"; $c = $c -replace "Rese${rc}a","Reseña"
    $c = $c -replace "pesta${rc}a","pestaña"; $c = $c -replace "campa${rc}a","campaña"
    $c = $c -replace "cumplea${rc}os","cumpleaños"
    $c = $c -replace "cari${rc}o","cariño"; $c = $c -replace "enga${rc}o","engaño"
    $c = $c -replace "oto${rc}o","otoño"; $c = $c -replace "pi${rc}a","piña"
    
    # ¿ patterns
    $c = $c -replace "${rc}Qu${rc}\b","¿Qué"
    $c = $c -replace "${rc}Qu","¿Qu"; $c = $c -replace "${rc}qu","¿qu"
    $c = $c -replace "${rc}C${rc}mo","¿Cómo"
    $c = $c -replace "${rc}D${rc}nde","¿Dónde"
    $c = $c -replace "${rc}Cu${rc}ndo","¿Cuándo"; $c = $c -replace "${rc}Cu${rc}l","¿Cuál"
    $c = $c -replace "${rc}Cu${rc}nto","¿Cuánto"
    $c = $c -replace "${rc}Sab${rc}as","¿Sabías"
    $c = $c -replace "${rc}Por qu","¿Por qu"
    $c = $c -replace "${rc}Es\b","¿Es"; $c = $c -replace "${rc}Seguro","¿Seguro"
    $c = $c -replace "${rc}Necesitas","¿Necesitas"; $c = $c -replace "${rc}Deseas","¿Deseas"
    $c = $c -replace "${rc}Te\b","¿Te"; $c = $c -replace "${rc}Tienes","¿Tienes"
    $c = $c -replace "${rc}Listo","¿Listo"; $c = $c -replace "${rc}No\b","¿No"
    $c = $c -replace "${rc}Ya\b","¿Ya"; $c = $c -replace "${rc}Olvidaste","¿Olvidaste"
    $c = $c -replace "${rc}Quieres","¿Quieres"; $c = $c -replace "${rc}Puedo","¿Puedo"
    $c = $c -replace "${rc}Ha\b","¿Ha"; $c = $c -replace "${rc}Hay\b","¿Hay"
    $c = $c -replace "${rc}Pod","¿Pod"; $c = $c -replace "${rc}Pued","¿Pued"
    $c = $c -replace "${rc}Funciona","¿Funciona"; $c = $c -replace "${rc}Recib","¿Recib"
    $c = $c -replace "${rc}Desea\b","¿Desea"; $c = $c -replace "${rc}Fue\b","¿Fue"
    $c = $c -replace "${rc}Conoces","¿Conoces"; $c = $c -replace "${rc}Recuerdas","¿Recuerdas"
    
    # Sé
    $c = $c -replace "S${rc} el primero","Sé el primero"
    
    # qu + é
    $c = $c -replace "qu${rc}\b","qué"; $c = $c -replace "Qu${rc}\b","Qué"
    $c = $c -replace "t${rc}rmino","término"; $c = $c -replace "T${rc}rmino","Término"
    
    if ($c -ne $o) {
        $after = ($c.ToCharArray()|?{$_ -eq $rc}).Count
        $fixed = $before - $after
        [System.IO.File]::WriteAllText($f.FullName, $c, $utf8)
        Write-Host "  FFFD $($f.Name): $fixed/$before, $after left"
    }
}

# Final check
Write-Host "`n=== Remaining ==="
foreach ($f in $files) {
    $c = [System.IO.File]::ReadAllText($f.FullName, $utf8)
    if ($c.Contains($rc)) {
        $n = ($c.ToCharArray()|?{$_ -eq $rc}).Count
        Write-Host "  $($f.Name): $n left"
        $lines = $c.Split("`n")
        $ln = 0
        foreach ($line in $lines) {
            $ln++
            if ($line.Contains($rc)) {
                $t = $line.Trim()
                if ($t.Length -gt 90) { $t = $t.Substring(0, 90) + "..." }
                Write-Host "    L${ln}: $t"
            }
        }
    }
}
