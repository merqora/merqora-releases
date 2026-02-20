package com.rendly.app.ui.components.settings;

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
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000B\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000f\u001a \u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u0003\u001a \u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003H\u0003\u001a\u0010\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\u0003H\u0003\u001a \u0010\r\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0003H\u0003\u001a\u0010\u0010\u0010\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\u0003H\u0003\u001a\u001e\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00062\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u0014H\u0007\u001aP\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0017\u001a\u00020\u00182\u001c\u0010\u0019\u001a\u0018\u0012\u0004\u0012\u00020\u001b\u0012\u0004\u0012\u00020\u00010\u001a\u00a2\u0006\u0002\b\u001c\u00a2\u0006\u0002\b\u001dH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001e\u0010\u001f\u001a \u0010 \u001a\u00020\u00012\u0006\u0010!\u001a\u00020\u00032\u0006\u0010\"\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\u0003H\u0003\u001a \u0010$\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003H\u0003\u001a \u0010%\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003H\u0003\u001a*\u0010&\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\'\u001a\u00020\u0018H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b(\u0010)\u001a\u0010\u0010*\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\u0003H\u0003\u001a\u0018\u0010+\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\u0003H\u0003\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006,"}, d2 = {"CookieType", "", "name", "", "description", "canDisable", "", "DataTypeItem", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "NotificationMethod", "text", "PrivacyContactCard", "label", "value", "PrivacyParagraph", "PrivacyPolicyScreen", "isVisible", "onDismiss", "Lkotlin/Function0;", "PrivacySection", "number", "iconColor", "Landroidx/compose/ui/graphics/Color;", "content", "Lkotlin/Function1;", "Landroidx/compose/foundation/layout/ColumnScope;", "Landroidx/compose/runtime/Composable;", "Lkotlin/ExtensionFunctionType;", "PrivacySection-42QJj7c", "(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JLkotlin/jvm/functions/Function1;)V", "RetentionItem", "type", "period", "reason", "RightItem", "SecurityFeature", "ShareItem", "color", "ShareItem-mxwnekA", "(Ljava/lang/String;Ljava/lang/String;J)V", "TransferSafeguard", "UsageItem", "app_debug"})
public final class PrivacyPolicyScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void PrivacyPolicyScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PrivacyParagraph(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void DataTypeItem(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String description) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UsageItem(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SecurityFeature(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String description) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RightItem(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String description) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RetentionItem(java.lang.String type, java.lang.String period, java.lang.String reason) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CookieType(java.lang.String name, java.lang.String description, boolean canDisable) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TransferSafeguard(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void NotificationMethod(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PrivacyContactCard(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, java.lang.String value) {
    }
}