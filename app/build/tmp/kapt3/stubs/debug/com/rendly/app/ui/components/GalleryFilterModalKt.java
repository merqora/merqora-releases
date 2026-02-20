package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.window.DialogProperties;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000L\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001aJ\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0011\u0010\u0012\u001a\u0010\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\nH\u0003\u001aJ\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00052\u0006\u0010\u0017\u001a\u00020\u00182\u000e\b\u0002\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00030\u001a2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00010\u001dH\u0007\u001a8\u0010\u001e\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001f"}, d2 = {"AlbumItem", "", "album", "Lcom/rendly/app/ui/components/AlbumOption;", "isSelected", "", "onClick", "Lkotlin/Function0;", "FilterChipItem", "label", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "accentColor", "Landroidx/compose/ui/graphics/Color;", "modifier", "Landroidx/compose/ui/Modifier;", "FilterChipItem-Bx497Mc", "(Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;ZJLkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;)V", "FilterSection", "title", "GalleryFilterModal", "isVisible", "currentFilter", "Lcom/rendly/app/ui/components/GalleryFilterState;", "albums", "", "onDismiss", "onFilterChange", "Lkotlin/Function1;", "SortOptionChip", "app_debug"})
public final class GalleryFilterModalKt {
    
    @androidx.compose.runtime.Composable
    public static final void GalleryFilterModal(boolean isVisible, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.components.GalleryFilterState currentFilter, @org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.ui.components.AlbumOption> albums, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.GalleryFilterState, kotlin.Unit> onFilterChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void FilterSection(java.lang.String title) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SortOptionChip(java.lang.String label, androidx.compose.ui.graphics.vector.ImageVector icon, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AlbumItem(com.rendly.app.ui.components.AlbumOption album, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}