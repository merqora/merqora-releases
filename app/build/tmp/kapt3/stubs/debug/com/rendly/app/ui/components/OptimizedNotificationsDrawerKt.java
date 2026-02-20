package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import com.rendly.app.data.repository.FollowersRepository;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.input.pointer.util.VelocityTracker;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Notification;
import com.rendly.app.data.model.NotificationType;
import com.rendly.app.data.repository.NotificationRepository;
import com.rendly.app.data.repository.SystemNotificationRepository;
import com.rendly.app.data.repository.SystemNotification;
import com.rendly.app.ui.theme.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000X\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0003\u001ab\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001aR\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001aR\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001at\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00162\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001a\u0010\u0010\u0018\u001a\u00020\u00012\u0006\u0010\u0019\u001a\u00020\u0005H\u0003\u001aT\u0010\u001a\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001a\u0016\u0010\u001b\u001a\u00020\u00012\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001a(\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\n2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\b\b\u0002\u0010 \u001a\u00020!H\u0007\u001a\u0084\u0002\u0010\"\u001a\u00020\u00012\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\b0$2\u0018\u0010%\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0$0&2\u0006\u0010\t\u001a\u00020\n2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00050(2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u0012\u0010*\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00162\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00162\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00162\u0018\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010+2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00162\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00162\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u0006\u0010.\u001a\u00020\nH\u0003\u001a\u0010\u0010/\u001a\u00020\u00052\u0006\u00100\u001a\u00020\u0005H\u0002\u001a\u0010\u00101\u001a\u00020\u00052\u0006\u00100\u001a\u00020\u0005H\u0002\u00a8\u00062"}, d2 = {"BenefitChip", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "text", "", "ClientAcceptedNotificationItem", "notification", "Lcom/rendly/app/ui/components/NotificationItem;", "isSelectionMode", "", "isSelected", "onSelect", "Lkotlin/Function0;", "onMarkAsRead", "onLongPress", "onViewBenefits", "ClientPendingNotificationItem", "ClientRejectedNotificationItem", "ClientRequestNotificationItem", "onAccept", "onReject", "Lkotlin/Function1;", "onViewHistory", "DayHeader", "title", "NotificationItemView", "NotificationsContent", "onClose", "OptimizedNotificationsDrawer", "isVisible", "onDismiss", "modifier", "Landroidx/compose/ui/Modifier;", "SolicitudesView", "solicitudes", "", "groupedSolicitudes", "", "selectedNotifications", "", "onBack", "onSelectNotification", "Lkotlin/Function2;", "onCancelSelection", "onDeleteSelected", "isDeleting", "formatTimeAgo", "isoDate", "getDayGroup", "app_debug"})
public final class OptimizedNotificationsDrawerKt {
    
    private static final java.lang.String formatTimeAgo(java.lang.String isoDate) {
        return null;
    }
    
    private static final java.lang.String getDayGroup(java.lang.String isoDate) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    public static final void OptimizedNotificationsDrawer(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void NotificationsContent(kotlin.jvm.functions.Function0<kotlin.Unit> onClose) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void DayHeader(java.lang.String title) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void NotificationItemView(com.rendly.app.ui.components.NotificationItem notification, boolean isSelectionMode, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onMarkAsRead, kotlin.jvm.functions.Function0<kotlin.Unit> onLongPress) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void ClientRequestNotificationItem(com.rendly.app.ui.components.NotificationItem notification, boolean isSelectionMode, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onAccept, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onReject, kotlin.jvm.functions.Function0<kotlin.Unit> onViewHistory, kotlin.jvm.functions.Function0<kotlin.Unit> onLongPress) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void ClientAcceptedNotificationItem(com.rendly.app.ui.components.NotificationItem notification, boolean isSelectionMode, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onMarkAsRead, kotlin.jvm.functions.Function0<kotlin.Unit> onLongPress, kotlin.jvm.functions.Function0<kotlin.Unit> onViewBenefits) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void BenefitChip(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String text) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void ClientRejectedNotificationItem(com.rendly.app.ui.components.NotificationItem notification, boolean isSelectionMode, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onMarkAsRead, kotlin.jvm.functions.Function0<kotlin.Unit> onLongPress) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void ClientPendingNotificationItem(com.rendly.app.ui.components.NotificationItem notification, boolean isSelectionMode, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onMarkAsRead, kotlin.jvm.functions.Function0<kotlin.Unit> onLongPress) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void SolicitudesView(java.util.List<com.rendly.app.ui.components.NotificationItem> solicitudes, java.util.Map<java.lang.String, ? extends java.util.List<com.rendly.app.ui.components.NotificationItem>> groupedSolicitudes, boolean isSelectionMode, java.util.Set<java.lang.String> selectedNotifications, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSelectNotification, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onLongPress, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.NotificationItem, kotlin.Unit> onAccept, kotlin.jvm.functions.Function2<? super com.rendly.app.ui.components.NotificationItem, ? super java.lang.String, kotlin.Unit> onReject, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.NotificationItem, kotlin.Unit> onViewBenefits, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onMarkAsRead, kotlin.jvm.functions.Function0<kotlin.Unit> onCancelSelection, kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteSelected, boolean isDeleting) {
    }
}