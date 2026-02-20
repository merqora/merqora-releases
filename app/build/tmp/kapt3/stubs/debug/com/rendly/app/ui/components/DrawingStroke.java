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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0017\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B9\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u00a2\u0006\u0002\u0010\rJ\u000f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u0016\u0010\u0019\u001a\u00020\u0006H\u00c6\u0003\u00f8\u0001\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001a\u0010\u000fJ\t\u0010\u001b\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001c\u001a\u00020\nH\u00c6\u0003J\t\u0010\u001d\u001a\u00020\fH\u00c6\u0003JK\u0010\u001e\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00c6\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001f\u0010 J\u0013\u0010!\u001a\u00020\f2\b\u0010\"\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010#\u001a\u00020$H\u00d6\u0001J\t\u0010%\u001a\u00020&H\u00d6\u0001R\u0019\u0010\u0005\u001a\u00020\u0006\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0010\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\u0011R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u0006\'"}, d2 = {"Lcom/rendly/app/ui/components/DrawingStroke;", "", "points", "", "Landroidx/compose/ui/geometry/Offset;", "color", "Landroidx/compose/ui/graphics/Color;", "strokeWidth", "", "tool", "Lcom/rendly/app/ui/components/DrawingTool;", "isEraser", "", "(Ljava/util/List;JFLcom/rendly/app/ui/components/DrawingTool;ZLkotlin/jvm/internal/DefaultConstructorMarker;)V", "getColor-0d7_KjU", "()J", "J", "()Z", "getPoints", "()Ljava/util/List;", "getStrokeWidth", "()F", "getTool", "()Lcom/rendly/app/ui/components/DrawingTool;", "component1", "component2", "component2-0d7_KjU", "component3", "component4", "component5", "copy", "copy-iJQMabo", "(Ljava/util/List;JFLcom/rendly/app/ui/components/DrawingTool;Z)Lcom/rendly/app/ui/components/DrawingStroke;", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
public final class DrawingStroke {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<androidx.compose.ui.geometry.Offset> points = null;
    private final long color = 0L;
    private final float strokeWidth = 0.0F;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.ui.components.DrawingTool tool = null;
    private final boolean isEraser = false;
    
    private DrawingStroke(java.util.List<androidx.compose.ui.geometry.Offset> points, long color, float strokeWidth, com.rendly.app.ui.components.DrawingTool tool, boolean isEraser) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<androidx.compose.ui.geometry.Offset> getPoints() {
        return null;
    }
    
    public final float getStrokeWidth() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.DrawingTool getTool() {
        return null;
    }
    
    public final boolean isEraser() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<androidx.compose.ui.geometry.Offset> component1() {
        return null;
    }
    
    public final float component3() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.DrawingTool component4() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}