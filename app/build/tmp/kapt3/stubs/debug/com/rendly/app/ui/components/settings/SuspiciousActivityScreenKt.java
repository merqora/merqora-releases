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
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.SecurityRepository;
import com.rendly.app.ui.theme.*;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000&\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\b\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0003\u001a\u001e\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0003\u001a\u001e\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a\u0012\u0010\r\u001a\u00020\u00032\b\u0010\u000e\u001a\u0004\u0018\u00010\u0003H\u0002\u001a\u0010\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0003H\u0002\u001a\u0010\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u0003H\u0002\u00a8\u0006\u0013"}, d2 = {"SecurityTip", "", "text", "", "SuspiciousActivityItem", "activity", "Lcom/rendly/app/data/repository/SecurityRepository$SuspiciousActivity;", "onResolve", "Lkotlin/Function0;", "SuspiciousActivityScreen", "isVisible", "", "onDismiss", "formatDate", "dateString", "getRiskLevelText", "level", "getSuspiciousActivityTitle", "type", "app_debug"})
public final class SuspiciousActivityScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void SuspiciousActivityScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SecurityTip(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SuspiciousActivityItem(com.rendly.app.data.repository.SecurityRepository.SuspiciousActivity activity, kotlin.jvm.functions.Function0<kotlin.Unit> onResolve) {
    }
    
    private static final java.lang.String getSuspiciousActivityTitle(java.lang.String type) {
        return null;
    }
    
    private static final java.lang.String getRiskLevelText(java.lang.String level) {
        return null;
    }
    
    private static final java.lang.String formatDate(java.lang.String dateString) {
        return null;
    }
}