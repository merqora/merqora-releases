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
import androidx.compose.ui.input.pointer.util.VelocityTracker;
import androidx.compose.ui.text.font.FontWeight;
import com.rendly.app.ui.theme.*;
import com.rendly.app.data.repository.ExploreRepository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0002\u001al\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\u0012\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00060\f2\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u0007\u001a\u001e\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u00042\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a \u0010\u0015\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u00032\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a\u0010\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019H\u0002\u001a \u0010\u001a\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00010\u00020\u0001H\u0007\"&\u0010\u0000\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00010\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"CATEGORY_DEFINITIONS", "", "Lkotlin/Pair;", "", "Lcom/rendly/app/ui/components/CategoryItem;", "CategoryDrawer", "", "isVisible", "", "onDismiss", "Lkotlin/Function0;", "onCategorySelected", "Lkotlin/Function1;", "onHelpCenter", "onPrivacyPolicy", "onTermsAndConditions", "modifier", "Landroidx/compose/ui/Modifier;", "CategoryRowPremium", "category", "onClick", "FooterLink", "text", "formatItemCount", "count", "", "rememberCategorySections", "app_debug"})
public final class CategoryDrawerKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<kotlin.Pair<java.lang.String, java.util.List<com.rendly.app.ui.components.CategoryItem>>> CATEGORY_DEFINITIONS = null;
    
    /**
     * Returns category sections with real item counts from the database
     */
    @androidx.compose.runtime.Composable
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<kotlin.Pair<java.lang.String, java.util.List<com.rendly.app.ui.components.CategoryItem>>> rememberCategorySections() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    public static final void CategoryDrawer(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onCategorySelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onHelpCenter, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onPrivacyPolicy, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onTermsAndConditions, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CategoryRowPremium(com.rendly.app.ui.components.CategoryItem category, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final java.lang.String formatItemCount(int count) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void FooterLink(java.lang.String text, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}