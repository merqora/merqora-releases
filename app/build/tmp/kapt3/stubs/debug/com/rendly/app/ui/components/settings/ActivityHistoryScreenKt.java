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
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.SecurityRepository;
import com.rendly.app.ui.theme.*;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000:\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0010\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0003\u001a:\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00032\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0010\u0010\u0011\u001a\u0012\u0010\u0012\u001a\u00020\u000b2\b\u0010\u0013\u001a\u0004\u0018\u00010\u000bH\u0002\u001a\u0010\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u000bH\u0002\u001a*\u0010\u0016\u001a\u0014\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0\u00172\u0006\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0019\u001a\u00020\u0003H\u0002\u001a\u0010\u0010\u001a\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u000bH\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001b"}, d2 = {"ActivityHistoryScreen", "", "isVisible", "", "onDismiss", "Lkotlin/Function0;", "ActivityLogItem", "log", "Lcom/rendly/app/data/repository/SecurityRepository$ActivityLog;", "FilterChipButton", "text", "", "isSelected", "onClick", "color", "Landroidx/compose/ui/graphics/Color;", "FilterChipButton-g2O1Hgs", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function0;J)V", "formatDate", "dateString", "getActivityDescription", "type", "getActivityIcon", "Lkotlin/Triple;", "Landroidx/compose/ui/graphics/vector/ImageVector;", "isSuspicious", "getActivityTitle", "app_debug"})
public final class ActivityHistoryScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void ActivityHistoryScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ActivityLogItem(com.rendly.app.data.repository.SecurityRepository.ActivityLog log) {
    }
    
    private static final kotlin.Triple<androidx.compose.ui.graphics.vector.ImageVector, androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> getActivityIcon(java.lang.String type, boolean isSuspicious) {
        return null;
    }
    
    private static final java.lang.String getActivityTitle(java.lang.String type) {
        return null;
    }
    
    private static final java.lang.String getActivityDescription(java.lang.String type) {
        return null;
    }
    
    private static final java.lang.String formatDate(java.lang.String dateString) {
        return null;
    }
}