package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00004\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a.\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0003\u001a.\u0010\r\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0003\u001a.\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0003\u001aB\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u0013H\u0007\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\u00a8\u0006\u0014"}, d2 = {"CAROUSEL_MODES", "", "Lcom/rendly/app/ui/components/CarouselMode;", "getCAROUSEL_MODES", "()Ljava/util/List;", "CenteredSingleMode", "", "currentIndex", "", "onModeSelected", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "FloatingRightMode", "FullModeCarousel", "ModeCarousel", "visible", "", "style", "Lcom/rendly/app/ui/components/CarouselStyle;", "app_debug"})
public final class ModeCarouselKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.CarouselMode> CAROUSEL_MODES = null;
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.CarouselMode> getCAROUSEL_MODES() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    public static final void ModeCarousel(int currentIndex, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, boolean visible, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.components.CarouselStyle style) {
    }
    
    /**
     * Modo centrado simple - Solo muestra el modo actual con swipe para cambiar
     * Usado en HISTORIA y REND debajo del botón central
     */
    @androidx.compose.runtime.Composable
    private static final void CenteredSingleMode(int currentIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Modo flotante a la derecha - Para PUBLICACIÓN
     * Fondo azul oscuro, pegado a la derecha, border radius solo a la izquierda
     */
    @androidx.compose.runtime.Composable
    private static final void FloatingRightMode(int currentIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Carrusel completo - Muestra todos los modos
     * OPTIMIZADO: Animaciones simplificadas para mejor rendimiento
     */
    @androidx.compose.runtime.Composable
    private static final void FullModeCarousel(int currentIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, androidx.compose.ui.Modifier modifier) {
    }
}