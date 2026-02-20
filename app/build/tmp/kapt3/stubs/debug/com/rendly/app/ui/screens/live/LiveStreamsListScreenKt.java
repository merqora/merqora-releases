package com.rendly.app.ui.screens.live;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.repository.LiveStream;
import com.rendly.app.data.repository.LiveStreamRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\b\u0010\u0000\u001a\u00020\u0001H\u0003\u001a&\u0010\u0002\u001a\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0003\u001a$\u0010\t\u001a\u00020\u00012\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0003\u001a4\u0010\f\u001a\u00020\u00012\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0007\u00a8\u0006\u0011"}, d2 = {"EmptyStreamsContent", "", "LiveStreamItem", "stream", "Lcom/rendly/app/data/repository/LiveStream;", "pulseAlpha", "", "onClick", "Lkotlin/Function0;", "LiveStreamsHeader", "onBack", "onRefresh", "LiveStreamsListScreen", "onStreamClick", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "app_debug"})
public final class LiveStreamsListScreenKt {
    
    /**
     * Pantalla que muestra todas las transmisiones en vivo activas
     * Con botón de refresh para actualizar manualmente
     */
    @androidx.compose.runtime.Composable
    public static final void LiveStreamsListScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.repository.LiveStream, kotlin.Unit> onStreamClick, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Header de la pantalla con botón de refresh
     */
    @androidx.compose.runtime.Composable
    private static final void LiveStreamsHeader(kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onRefresh) {
    }
    
    /**
     * Item de stream en la grilla
     */
    @androidx.compose.runtime.Composable
    private static final void LiveStreamItem(com.rendly.app.data.repository.LiveStream stream, float pulseAlpha, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    /**
     * Contenido cuando no hay streams
     */
    @androidx.compose.runtime.Composable
    private static final void EmptyStreamsContent() {
    }
}