package com.rendly.app.ui.components;

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
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000H\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0003\u001a*\u0010\u0006\u001a\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u0005H\u0007\u001a*\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001a\u001a\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u000e2\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001a2\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00122\u0006\u0010\u0019\u001a\u00020\u00122\u0006\u0010\u001a\u001a\u00020\u001bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001c\u0010\u001d\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001e"}, d2 = {"BadgeItem", "", "badge", "Lcom/rendly/app/ui/components/ProfileBadge;", "showLabel", "", "ProfileInfoBadges", "badges", "", "modifier", "Landroidx/compose/ui/Modifier;", "showLabels", "ProfileStatsBadges", "salesCount", "", "rating", "", "responseTime", "", "SellerTrustIndicator", "trustScore", "StatBadge", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "value", "label", "color", "Landroidx/compose/ui/graphics/Color;", "StatBadge-g2O1Hgs", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;J)V", "app_debug"})
public final class ProfileInfoBadgesKt {
    
    @androidx.compose.runtime.Composable
    public static final void ProfileInfoBadges(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.ui.components.ProfileBadge> badges, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, boolean showLabels) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void BadgeItem(com.rendly.app.ui.components.ProfileBadge badge, boolean showLabel) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void ProfileStatsBadges(int salesCount, float rating, @org.jetbrains.annotations.NotNull
    java.lang.String responseTime, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void SellerTrustIndicator(int trustScore, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
}