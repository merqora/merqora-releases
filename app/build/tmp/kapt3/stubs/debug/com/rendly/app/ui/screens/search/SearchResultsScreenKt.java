package com.rendly.app.ui.screens.search;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.repository.CartRepository;
import com.rendly.app.data.repository.ExploreItem;
import com.rendly.app.data.repository.ExploreRepository;
import com.rendly.app.ui.components.ProductCardData;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a4\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00040\tH\u0003\u001a:\u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\u00072\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00040\t2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0003\u001aH\u0010\u0011\u001a\u00020\u00042\b\b\u0002\u0010\u0012\u001a\u00020\u00072\b\b\u0002\u0010\u000f\u001a\u00020\u00102\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u000e2\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00040\t2\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0007\u001a\u0010\u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u0019H\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"SEARCH_CATEGORIES", "", "Lcom/rendly/app/ui/screens/search/SearchCategory;", "CategoryCarousel", "", "categories", "selectedCategory", "", "onCategorySelected", "Lkotlin/Function1;", "MinimalSearchHeader", "query", "onQueryChange", "onBack", "Lkotlin/Function0;", "isCategory", "", "SearchResultsScreen", "initialQuery", "onProductClick", "Lcom/rendly/app/data/model/Post;", "modifier", "Landroidx/compose/ui/Modifier;", "exploreItemToPost", "item", "Lcom/rendly/app/data/repository/ExploreItem;", "app_debug"})
public final class SearchResultsScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.search.SearchCategory> SEARCH_CATEGORIES = null;
    
    @androidx.compose.runtime.Composable
    public static final void SearchResultsScreen(@org.jetbrains.annotations.NotNull
    java.lang.String initialQuery, boolean isCategory, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onProductClick, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void MinimalSearchHeader(java.lang.String query, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQueryChange, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, boolean isCategory) {
    }
    
    private static final com.rendly.app.data.model.Post exploreItemToPost(com.rendly.app.data.repository.ExploreItem item) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void CategoryCarousel(java.util.List<com.rendly.app.ui.screens.search.SearchCategory> categories, java.lang.String selectedCategory, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onCategorySelected) {
    }
}