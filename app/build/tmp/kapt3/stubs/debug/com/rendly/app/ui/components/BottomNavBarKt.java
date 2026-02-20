package com.rendly.app.ui.components;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material.icons.rounded.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\u001aJ\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\n2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00060\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0007\u001a\u001e\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00060\fH\u0003\u001a@\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u00022\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00172\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00060\fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0019\u0010\u001a\u001a8\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u00172\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00060\fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001f"}, d2 = {"navItems", "", "Lcom/rendly/app/ui/components/NavItem;", "getNavItems", "()Ljava/util/List;", "BottomNavBar", "", "currentRoute", "", "onNavigate", "Lkotlin/Function1;", "onHomeReclick", "Lkotlin/Function0;", "userAvatarUrl", "modifier", "Landroidx/compose/ui/Modifier;", "CreativeCenterButton", "isSelected", "", "onClick", "NavBarItem", "item", "activeColor", "Landroidx/compose/ui/graphics/Color;", "inactiveColor", "NavBarItem-OoHUuok", "(Lcom/rendly/app/ui/components/NavItem;ZJJLkotlin/jvm/functions/Function0;)V", "ProfileAvatarNavItem", "avatarUrl", "ProfileAvatarNavItem-9LQNqLg", "(Ljava/lang/String;ZJLkotlin/jvm/functions/Function0;)V", "app_debug"})
public final class BottomNavBarKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.NavItem> navItems = null;
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.NavItem> getNavItems() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    public static final void BottomNavBar(@org.jetbrains.annotations.NotNull
    java.lang.String currentRoute, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigate, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onHomeReclick, @org.jetbrains.annotations.Nullable
    java.lang.String userAvatarUrl, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CreativeCenterButton(boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}