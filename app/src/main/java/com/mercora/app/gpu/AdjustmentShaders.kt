package com.mercora.app.gpu

/**
 * Shaders GLSL profesionales para ajustes de imagen nivel Instagram/Lightroom
 * Pipeline de color en espacio lineal con conversiÃ³n sRGB correcta
 * Optimizado para 60+ FPS en tiempo real
 */
object AdjustmentShaders {

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // VERTEX SHADER - Simple passthrough para texturas
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    const val VERTEX_SHADER = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    """

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // FRAGMENT SHADER PROFESIONAL - Calidad Instagram/Lightroom
    // Procesamiento en espacio lineal, grain fotogrÃ¡fico, curvas S
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    const val FRAGMENT_SHADER = """
        precision highp float;
        
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        
        // ParÃ¡metros de ajuste (uniforms para cambio instantÃ¡neo)
        uniform float uBrightness;    // -1.0 a 1.0
        uniform float uContrast;      // -1.0 a 1.0
        uniform float uSaturation;    // -1.0 a 1.0
        uniform float uExposure;      // -2.0 a 2.0
        uniform float uHighlights;    // -1.0 a 1.0
        uniform float uShadows;       // -1.0 a 1.0
        uniform float uTemperature;   // -1.0 a 1.0
        uniform float uTint;          // -1.0 a 1.0
        uniform float uGrain;         // 0.0 a 1.0
        uniform float uTime;          // Para grain estable
        uniform vec2 uResolution;     // Para grain dependiente de posiciÃ³n
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // CONVERSIÃ“N sRGB â†” LINEAR (crÃ­tico para calidad fotogrÃ¡fica)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        vec3 srgbToLinear(vec3 srgb) {
            vec3 low = srgb / 12.92;
            vec3 high = pow((srgb + 0.055) / 1.055, vec3(2.4));
            return mix(low, high, step(vec3(0.04045), srgb));
        }
        
        vec3 linearToSrgb(vec3 linear) {
            vec3 low = linear * 12.92;
            vec3 high = 1.055 * pow(linear, vec3(1.0/2.4)) - 0.055;
            return mix(low, high, step(vec3(0.0031308), linear));
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // LUMINANCIA PERCEPTUAL (Rec. 709)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        float getLuminance(vec3 color) {
            return dot(color, vec3(0.2126, 0.7152, 0.0722));
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // CURVA S PARA CONTRASTE (mÃ¡s natural que lineal)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        float sCurve(float x, float contrast) {
            float midpoint = 0.5;
            float slope = 1.0 + contrast * 2.0;
            
            if (x < midpoint) {
                return midpoint * pow(x / midpoint, slope);
            } else {
                return 1.0 - (1.0 - midpoint) * pow((1.0 - x) / (1.0 - midpoint), slope);
            }
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // HIGHLIGHTS & SHADOWS (control independiente - nivel PRO)
        // Curvas adaptativas con protecciÃ³n de detalle
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        vec3 applyHighlightsShadows(vec3 color, float highlights, float shadows) {
            float lum = getLuminance(color);
            
            // MÃ¡scaras suaves con curvas mÃ¡s naturales
            float highlightMask = smoothstep(0.35, 0.85, lum);
            float shadowMask = 1.0 - smoothstep(0.15, 0.65, lum);
            
            // Highlight recovery: compresiÃ³n suave de highlights
            if (highlights < 0.0) {
                float recovery = -highlights * highlightMask;
                color = mix(color, color * (1.0 - recovery * 0.4), highlightMask);
            } else {
                color += color * highlights * highlightMask * 0.3;
            }
            
            // Shadow lift: recuperaciÃ³n de sombras con preservaciÃ³n de detalle
            if (shadows > 0.0) {
                float lift = shadows * shadowMask * 0.4;
                color = color + (1.0 - color) * lift * shadowMask;
            } else {
                color *= 1.0 + shadows * shadowMask * 0.5;
            }
            
            return color;
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // FILMIC TONE MAPPING (roll-off suave en highlights)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        vec3 filmicTonemap(vec3 color) {
            // ACES-inspired filmic curve simplificada
            float a = 2.51;
            float b = 0.03;
            float c = 2.43;
            float d = 0.59;
            float e = 0.14;
            return clamp((color * (a * color + b)) / (color * (c * color + d) + e), 0.0, 1.0);
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // TEMPERATURA Y TINTE (balance de blancos)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        vec3 applyTemperatureTint(vec3 color, float temperature, float tint) {
            // Temperatura: cÃ¡lido (amarillo/naranja) â†” frÃ­o (azul)
            color.r += temperature * 0.1;
            color.b -= temperature * 0.1;
            
            // Tinte: verde â†” magenta
            color.g += tint * 0.05;
            color.r -= tint * 0.025;
            color.b -= tint * 0.025;
            
            return color;
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // SATURACIÃ“N PERCEPTUAL (no RGB directo)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        vec3 applySaturation(vec3 color, float saturation) {
            float lum = getLuminance(color);
            vec3 grey = vec3(lum);
            return mix(grey, color, 1.0 + saturation);
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // GRAIN FOTOGRÃFICO PROFESIONAL
        // - Dependiente de luminancia (mÃ¡s en sombras, menos en highlights)
        // - Hash-based para estabilidad
        // - Blue noise pattern para aspecto natural
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        float hash(vec2 p) {
            vec3 p3 = fract(vec3(p.xyx) * 0.1031);
            p3 += dot(p3, p3.yzx + 33.33);
            return fract((p3.x + p3.y) * p3.z);
        }
        
        float blueNoise(vec2 uv) {
            // AproximaciÃ³n de blue noise con hash multicapa
            float n = hash(uv * 1.0);
            n += hash(uv * 2.0) * 0.5;
            n += hash(uv * 4.0) * 0.25;
            return (n / 1.75) * 2.0 - 1.0;
        }
        
        vec3 applyGrain(vec3 color, vec2 uv, float grain) {
            if (grain < 0.001) return color;
            
            float lum = getLuminance(color);
            
            // Grain mÃ¡s fuerte en sombras, casi invisible en highlights
            float grainStrength = grain * 0.15 * (1.0 - lum * 0.7);
            
            // Noise estable basado en posiciÃ³n (no cambia con el tiempo)
            float noise = blueNoise(uv * uResolution);
            
            // Aplicar grain con preservaciÃ³n de color
            vec3 grainColor = color + vec3(noise * grainStrength);
            
            return grainColor;
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // TEMPERATURA/TINTE CON APROXIMACIÃ“N LMS (mÃ¡s preciso)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        vec3 applyTemperatureTintLMS(vec3 color, float temperature, float tint) {
            // Matriz RGB a LMS (aproximaciÃ³n Bradford)
            mat3 rgbToLms = mat3(
                0.4122214708, 0.5363325363, 0.0514459929,
                0.2119034982, 0.6806995451, 0.1073969566,
                0.0883024619, 0.2817188376, 0.6299787005
            );
            
            // Matriz LMS a RGB
            mat3 lmsToRgb = mat3(
                4.0767416621, -3.3077115913, 0.2309699292,
                -1.2684380046, 2.6097574011, -0.3413193965,
                -0.0041960863, -0.7034186147, 1.7076147010
            );
            
            // Convertir a LMS
            vec3 lms = rgbToLms * color;
            
            // Ajustar temperatura (L/M balance)
            float tempScale = 1.0 + temperature * 0.15;
            lms.x *= tempScale;
            lms.z /= tempScale;
            
            // Ajustar tinte (verde/magenta)
            float tintScale = 1.0 + tint * 0.1;
            lms.y *= tintScale;
            
            // Convertir de vuelta a RGB
            return lmsToRgb * lms;
        }
        
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // SHADER PRINCIPAL - Pipeline Instagram/Lightroom
        // Orden correcto para calidad mÃ¡xima
        // PASS-THROUGH cuando no hay ajustes para evitar cambio de tono
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        void main() {
            vec4 texColor = texture2D(uTexture, vTexCoord);
            vec3 color = texColor.rgb;
            
            // Detectar si hay algÃºn ajuste activo
            bool hasAdjustments = abs(uBrightness) > 0.001 || 
                                  abs(uContrast) > 0.001 || 
                                  abs(uSaturation) > 0.001 || 
                                  abs(uExposure) > 0.001 || 
                                  abs(uHighlights) > 0.001 || 
                                  abs(uShadows) > 0.001 || 
                                  abs(uTemperature) > 0.001 || 
                                  abs(uTint) > 0.001 || 
                                  uGrain > 0.001;
            
            // Si no hay ajustes, PASS-THROUGH directo (sin cambio de tono)
            if (!hasAdjustments) {
                gl_FragColor = texColor;
                return;
            }
            
            // 1. sRGB â†’ Linear (crÃ­tico para calidad)
            color = srgbToLinear(color);
            
            // 2. ExposiciÃ³n (multiplicador EV)
            if (abs(uExposure) > 0.001) {
                float exposureMultiplier = pow(2.0, uExposure);
                color *= exposureMultiplier;
            }
            
            // 3. Filmic Tone Mapping solo si hay exposiciÃ³n positiva
            // Suaviza highlights, previene clipping
            if (uExposure > 0.1) {
                color = filmicTonemap(color);
            }
            
            // 4. Highlights & Shadows
            if (abs(uHighlights) > 0.001 || abs(uShadows) > 0.001) {
                color = applyHighlightsShadows(color, uHighlights, uShadows);
            }
            
            // 5. Temperatura y Tinte (en espacio LMS para precisiÃ³n)
            if (abs(uTemperature) > 0.001 || abs(uTint) > 0.001) {
                color = applyTemperatureTintLMS(color, uTemperature, uTint);
            }
            
            // 6. Contraste con curva S
            if (abs(uContrast) > 0.001) {
                color.r = sCurve(clamp(color.r, 0.0, 1.0), uContrast);
                color.g = sCurve(clamp(color.g, 0.0, 1.0), uContrast);
                color.b = sCurve(clamp(color.b, 0.0, 1.0), uContrast);
            }
            
            // 7. Brillo (gamma-aware)
            if (abs(uBrightness) > 0.001) {
                float brightnessFactor = 1.0 + uBrightness * 0.5;
                color *= brightnessFactor;
            }
            
            // 8. SaturaciÃ³n perceptual
            if (abs(uSaturation) > 0.001) {
                color = applySaturation(color, uSaturation);
            }
            
            // 9. Clamp antes de conversiÃ³n
            color = clamp(color, 0.0, 1.0);
            
            // 10. Linear â†’ sRGB
            color = linearToSrgb(color);
            
            // 11. Grain fotogrÃ¡fico (despuÃ©s de sRGB)
            if (uGrain > 0.001) {
                color = applyGrain(color, vTexCoord, uGrain);
            }
            
            // 12. Clamp final
            color = clamp(color, 0.0, 1.0);
            
            gl_FragColor = vec4(color, texColor.a);
        }
    """

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // NOMBRES DE UNIFORMS PARA ACCESO RÃPIDO
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    object Uniforms {
        const val TEXTURE = "uTexture"
        const val BRIGHTNESS = "uBrightness"
        const val CONTRAST = "uContrast"
        const val SATURATION = "uSaturation"
        const val EXPOSURE = "uExposure"
        const val HIGHLIGHTS = "uHighlights"
        const val SHADOWS = "uShadows"
        const val TEMPERATURE = "uTemperature"
        const val TINT = "uTint"
        const val GRAIN = "uGrain"
        const val TIME = "uTime"
        const val RESOLUTION = "uResolution"
    }

    object Attributes {
        const val POSITION = "aPosition"
        const val TEX_COORD = "aTexCoord"
    }
}
