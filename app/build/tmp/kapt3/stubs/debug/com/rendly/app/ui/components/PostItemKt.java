package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.animation.core.CubicBezierEasing;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.SpanStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.unit.Dp;
import coil.request.ImageRequest;
import coil.request.CachePolicy;
import com.rendly.app.data.model.Post;
import com.rendly.app.ui.theme.*;
import com.rendly.app.ui.components.VerifiedBadge;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000N\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u001aL\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\b\b\u0002\u0010\f\u001a\u00020\tH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\r\u0010\u000e\u001a*\u0010\u000f\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u00072\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u0003\u001a\u00a2\u0001\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\u000e\b\u0002\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\u000e\b\u0002\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\u000e\b\u0002\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\u000e\b\u0002\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\u000e\b\u0002\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u0007\u001a\u0016\u0010\"\u001a\u00020\u00012\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0003\u001a\u001a\u0010#\u001a\u00020\u00012\u0006\u0010$\u001a\u00020%2\b\b\u0002\u0010\u0013\u001a\u00020\u0007H\u0003\u001a\u0010\u0010&\u001a\u00020\'2\u0006\u0010\u0004\u001a\u00020\u0005H\u0002\u001a\u0010\u0010(\u001a\u00020\'2\u0006\u0010)\u001a\u00020\'H\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006*"}, d2 = {"ActionButton", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "count", "", "isActive", "", "activeColor", "Landroidx/compose/ui/graphics/Color;", "onClick", "Lkotlin/Function0;", "inactiveColor", "ActionButton-Ym9LsEM", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/Integer;ZJLkotlin/jvm/functions/Function0;J)V", "AnimatedDataItem", "dataItem", "Lcom/rendly/app/ui/components/InfoDataItem;", "index", "visible", "modifier", "Landroidx/compose/ui/Modifier;", "PostItem", "post", "Lcom/rendly/app/data/model/Post;", "onLikeClick", "onSaveClick", "onCommentClick", "onPostClick", "onInfoClick", "onConsultClick", "onProfileClick", "onOptionsClick", "onShareClick", "PremiumShareButton", "ProductInfoPanel", "producto", "Lcom/rendly/app/data/model/Producto;", "formatCount", "", "formatTimeAgo", "dateString", "app_debug"})
public final class PostItemKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void PostItem(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.Post post, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onLikeClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onSaveClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onCommentClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onPostClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onInfoClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onConsultClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onProfileClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onOptionsClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onShareClick, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PremiumShareButton(kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductInfoPanel(com.rendly.app.data.model.Producto producto, boolean visible) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AnimatedDataItem(com.rendly.app.ui.components.InfoDataItem dataItem, int index, boolean visible, androidx.compose.ui.Modifier modifier) {
    }
    
    private static final java.lang.String formatCount(int count) {
        return null;
    }
    
    private static final java.lang.String formatTimeAgo(java.lang.String dateString) {
        return null;
    }
}