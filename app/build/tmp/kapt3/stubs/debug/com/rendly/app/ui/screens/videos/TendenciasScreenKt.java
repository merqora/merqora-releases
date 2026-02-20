package com.rendly.app.ui.screens.videos;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.ExperimentalMaterialApi;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.repository.TrendRepository;
import com.rendly.app.data.repository.TrendingPostItem;
import com.rendly.app.data.repository.TrendingTagItem;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000R\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000f\u001a^\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0012\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00040\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\u0006\u0010\u000f\u001a\u00020\u0010H\u0003\u001aD\u0010\u0011\u001a\u00020\u00042\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00040\f2\u0014\b\u0002\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00040\fH\u0007\u001a0\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00132\u0006\u0010\u0018\u001a\u00020\u00192\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0003\u001a*\u0010\u001d\u001a\u00020\u00042\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00130\u00012\u0012\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00040\fH\u0003\u001a2\u0010 \u001a\u00020\u00042\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u0006\u0010\"\u001a\u00020\n2\u0012\u0010#\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00040\fH\u0003\u001a\u001e\u0010$\u001a\u00020\u00042\u0006\u0010%\u001a\u00020\u00152\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0003\u001a*\u0010&\u001a\u00020\u00042\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00150\u00012\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00040\fH\u0003\u001a(\u0010(\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00132\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0003\u001a\u0010\u0010)\u001a\u00020\n2\u0006\u0010*\u001a\u00020\u0019H\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"TREND_CATEGORIES", "", "Lcom/rendly/app/ui/screens/videos/TrendCategory;", "TendenciasHeaderScrollable", "", "onBack", "Lkotlin/Function0;", "isSearchActive", "", "searchQuery", "", "onSearchQueryChange", "Lkotlin/Function1;", "onSearchActivate", "onSearchDeactivate", "focusRequester", "Landroidx/compose/ui/focus/FocusRequester;", "TendenciasScreen", "onTrendClick", "Lcom/rendly/app/data/repository/TrendingPostItem;", "onHashtagClick", "Lcom/rendly/app/data/repository/TrendingTagItem;", "TopTrendCard", "item", "rank", "", "onClick", "modifier", "Landroidx/compose/ui/Modifier;", "TopTrendsHero", "items", "onItemClick", "TrendCategoriesRow", "categories", "selectedCategory", "onCategorySelected", "TrendingHashtagCard", "hashtag", "TrendingHashtagsSection", "hashtags", "TrendingItemCard", "formatViewCount", "count", "app_debug"})
public final class TendenciasScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.videos.TrendCategory> TREND_CATEGORIES = null;
    
    @kotlin.OptIn(markerClass = {androidx.compose.material.ExperimentalMaterialApi.class})
    @androidx.compose.runtime.Composable
    public static final void TendenciasScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.repository.TrendingPostItem, kotlin.Unit> onTrendClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.repository.TrendingTagItem, kotlin.Unit> onHashtagClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TendenciasHeaderScrollable(kotlin.jvm.functions.Function0<kotlin.Unit> onBack, boolean isSearchActive, java.lang.String searchQuery, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSearchQueryChange, kotlin.jvm.functions.Function0<kotlin.Unit> onSearchActivate, kotlin.jvm.functions.Function0<kotlin.Unit> onSearchDeactivate, androidx.compose.ui.focus.FocusRequester focusRequester) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TrendingHashtagsSection(java.util.List<com.rendly.app.data.repository.TrendingTagItem> hashtags, kotlin.jvm.functions.Function1<? super com.rendly.app.data.repository.TrendingTagItem, kotlin.Unit> onHashtagClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TrendingHashtagCard(com.rendly.app.data.repository.TrendingTagItem hashtag, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TopTrendsHero(java.util.List<com.rendly.app.data.repository.TrendingPostItem> items, kotlin.jvm.functions.Function1<? super com.rendly.app.data.repository.TrendingPostItem, kotlin.Unit> onItemClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TopTrendCard(com.rendly.app.data.repository.TrendingPostItem item, int rank, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TrendCategoriesRow(java.util.List<com.rendly.app.ui.screens.videos.TrendCategory> categories, java.lang.String selectedCategory, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onCategorySelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TrendingItemCard(com.rendly.app.data.repository.TrendingPostItem item, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    private static final java.lang.String formatViewCount(int count) {
        return null;
    }
}