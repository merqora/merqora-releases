package com.rendly.app.ui.screens.profile;

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
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.window.DialogProperties;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000V\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0005\u001a\u0018\u0010\u0003\u001a\u00020\u00042\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0003\u001aN\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00040\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00040\u000eH\u0003\u001a\u0016\u0010\u0010\u001a\u00020\u00042\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0003\u001a>\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00152\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u00012\u000e\b\u0002\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0003\u001a\b\u0010\u001a\u001a\u00020\u0004H\u0003\u001a\b\u0010\u001b\u001a\u00020\u0004H\u0003\u001a\u001e\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\u000b2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0003\u001a \u0010\u001f\u001a\u00020\u00042\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\b\b\u0002\u0010 \u001a\u00020!H\u0007\u001a$\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u00152\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00040\u000eH\u0003\u001a&\u0010%\u001a\u00020\u00042\u0006\u0010&\u001a\u00020\u00022\u0006\u0010\'\u001a\u00020(2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0003\u001a*\u0010)\u001a\u00020\u00042\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\t0\u00012\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00040\u000eH\u0003\u001a\u001e\u0010,\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0003\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"SHOP_SECTIONS", "", "Lcom/rendly/app/ui/screens/profile/ShopSection;", "HeroBanner", "", "onProClick", "Lkotlin/Function0;", "ItemDetailModal", "item", "Lcom/rendly/app/ui/screens/profile/ShopItem;", "userCredits", "", "onDismiss", "onRedeem", "Lkotlin/Function1;", "onBuy", "ProBenefitsModal", "QuickActionCard", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "", "subtitle", "gradient", "Landroidx/compose/ui/graphics/Color;", "onClick", "QuickActionsRow", "RendshopFooter", "RendshopHeader", "credits", "onClose", "RendshopScreen", "modifier", "Landroidx/compose/ui/Modifier;", "SearchBarSection", "query", "onQueryChange", "SectionHeader", "section", "isExpanded", "", "SectionItems", "items", "onItemClick", "ShopItemCard", "app_debug"})
public final class RendshopScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.profile.ShopSection> SHOP_SECTIONS = null;
    
    @androidx.compose.runtime.Composable
    public static final void RendshopScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendshopHeader(int credits, kotlin.jvm.functions.Function0<kotlin.Unit> onClose) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void HeroBanner(kotlin.jvm.functions.Function0<kotlin.Unit> onProClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SearchBarSection(java.lang.String query, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQueryChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void QuickActionsRow() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void QuickActionCard(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle, java.util.List<androidx.compose.ui.graphics.Color> gradient, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SectionHeader(com.rendly.app.ui.screens.profile.ShopSection section, boolean isExpanded, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SectionItems(java.util.List<com.rendly.app.ui.screens.profile.ShopItem> items, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.screens.profile.ShopItem, kotlin.Unit> onItemClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ShopItemCard(com.rendly.app.ui.screens.profile.ShopItem item, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendshopFooter() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ItemDetailModal(com.rendly.app.ui.screens.profile.ShopItem item, int userCredits, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.screens.profile.ShopItem, kotlin.Unit> onRedeem, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.screens.profile.ShopItem, kotlin.Unit> onBuy) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProBenefitsModal(kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}