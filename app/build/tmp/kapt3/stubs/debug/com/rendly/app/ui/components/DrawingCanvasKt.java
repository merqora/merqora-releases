package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.SliderDefaults;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.*;
import androidx.compose.ui.graphics.drawscope.Stroke;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000Z\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0007\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a \u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00012\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001a@\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u00102\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0011\u0010\u0012\u001a\u0094\u0001\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u00172\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00182\b\b\u0002\u0010\t\u001a\u00020\n2\u0012\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00060\u00102\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00060\u001b2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00060\u001b2\u0018\u0010\u001d\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0001\u0012\u0004\u0012\u00020\u00060\u0010H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001e\u0010\u001f\u001a@\u0010 \u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\f\u001a\u00020\u00022\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00060\u00102\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\"\u0010#\u001a&\u0010$\u001a\u00020\u00062\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u00152\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00060\u001bH\u0003\u001a\u0014\u0010)\u001a\u00020\u0006*\u00020*2\u0006\u0010+\u001a\u00020\bH\u0002\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006,"}, d2 = {"DRAWING_COLORS", "", "Landroidx/compose/ui/graphics/Color;", "getDRAWING_COLORS", "()Ljava/util/List;", "DrawingCanvasStatic", "", "strokes", "Lcom/rendly/app/ui/components/DrawingStroke;", "modifier", "Landroidx/compose/ui/Modifier;", "DrawingColorCarousel", "selectedColor", "selectedTool", "Lcom/rendly/app/ui/components/DrawingTool;", "onColorSelected", "Lkotlin/Function1;", "DrawingColorCarousel-Iv8Zu3U", "(JLcom/rendly/app/ui/components/DrawingTool;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;)V", "DrawingOverlay", "visible", "", "strokeWidth", "", "", "onToolChange", "onUndo", "Lkotlin/Function0;", "onApply", "onStrokesChanged", "DrawingOverlay-oC9nPe0", "(ZJLcom/rendly/app/ui/components/DrawingTool;FLjava/util/List;Landroidx/compose/ui/Modifier;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;)V", "DrawingStrokeSliderVertical", "onStrokeWidthChange", "DrawingStrokeSliderVertical-RPmYEkk", "(FJLkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;)V", "DrawingToolButtonIndividual", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "selected", "onClick", "drawStrokeWithTool", "Landroidx/compose/ui/graphics/drawscope/DrawScope;", "stroke", "app_debug"})
public final class DrawingCanvasKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<androidx.compose.ui.graphics.Color> DRAWING_COLORS = null;
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<androidx.compose.ui.graphics.Color> getDRAWING_COLORS() {
        return null;
    }
    
    private static final void drawStrokeWithTool(androidx.compose.ui.graphics.drawscope.DrawScope $this$drawStrokeWithTool, com.rendly.app.ui.components.DrawingStroke stroke) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void DrawingToolButtonIndividual(androidx.compose.ui.graphics.vector.ImageVector icon, boolean selected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void DrawingCanvasStatic(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.ui.components.DrawingStroke> strokes, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
}