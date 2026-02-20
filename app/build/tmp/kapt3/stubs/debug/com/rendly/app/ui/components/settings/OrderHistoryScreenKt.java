package com.rendly.app.ui.components.settings;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
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
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Order;
import com.rendly.app.data.model.OrderStatus;
import com.rendly.app.data.repository.OrderRepository;
import com.rendly.app.data.repository.TransactionsSummary;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\\\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\b\u001aJ\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0014\u0010\b\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\n\u0012\u0004\u0012\u00020\u00010\t2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u001e\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a\u0010\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0003\u001a\"\u0010\u000f\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\n2\b\b\u0002\u0010\u0012\u001a\u00020\u0005H\u0003\u001aB\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\u001aH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001b\u0010\u001c\u001a\u0018\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0005H\u0003\u001a&\u0010!\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001aJ\u0010#\u001a\u00020\u00012\u0006\u0010$\u001a\u00020\n2\u0006\u0010%\u001a\u00020\u001a2\u0006\u0010&\u001a\u00020\u00052\u0006\u0010\'\u001a\u00020\u00172\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010(\u001a\u00020)H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b*\u0010+\u001aJ\u0010,\u001a\u00020\u00012\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00030.2\u0006\u0010/\u001a\u00020\u00152\u0006\u00100\u001a\u00020\n2\u0006\u00101\u001a\u00020\n2\u0006\u00102\u001a\u00020\u00052\u0012\u00103\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a\u0010\u00104\u001a\u00020\u00012\u0006\u00105\u001a\u000206H\u0003\u001a4\u00107\u001a\u00020\u00012\u0006\u00108\u001a\u00020\u001a2\u0006\u00109\u001a\u00020\u001a2\u0006\u0010:\u001a\u00020\u001a2\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a\u0010\u0010<\u001a\u00020\n2\u0006\u0010=\u001a\u00020\nH\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006>"}, d2 = {"OrderDetailModal", "", "order", "Lcom/rendly/app/data/model/Order;", "isSale", "", "onDismiss", "Lkotlin/Function0;", "onMarkAsShipped", "Lkotlin/Function1;", "", "onMarkAsDelivered", "OrderHistoryScreen", "isVisible", "OrderTimeline", "SummaryRow", "label", "value", "isHighlighted", "SummaryStatItem", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "iconColor", "Landroidx/compose/ui/graphics/Color;", "subValue", "pendingCount", "", "SummaryStatItem-3IgeMak", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", "TimelineItem", "step", "Lcom/rendly/app/ui/components/settings/TimelineStep;", "isLast", "TransactionCard", "onClick", "TransactionTab", "title", "count", "isSelected", "color", "modifier", "Landroidx/compose/ui/Modifier;", "TransactionTab-Bx497Mc", "(Ljava/lang/String;IZJLkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;)V", "TransactionsList", "orders", "", "emptyIcon", "emptyTitle", "emptySubtitle", "isSales", "onOrderClick", "TransactionsSummaryCard", "summary", "Lcom/rendly/app/data/repository/TransactionsSummary;", "TransactionsTabs", "selectedTab", "purchasesCount", "salesCount", "onTabSelected", "formatTimestamp", "timestamp", "app_debug"})
public final class OrderHistoryScreenKt {
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MIS TRANSACCIONES - Pantalla completa de historial de compras y ventas
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void OrderHistoryScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TransactionsSummaryCard(com.rendly.app.data.repository.TransactionsSummary summary) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TransactionsTabs(int selectedTab, int purchasesCount, int salesCount, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onTabSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TransactionsList(java.util.List<com.rendly.app.data.model.Order> orders, androidx.compose.ui.graphics.vector.ImageVector emptyIcon, java.lang.String emptyTitle, java.lang.String emptySubtitle, boolean isSales, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Order, kotlin.Unit> onOrderClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TransactionCard(com.rendly.app.data.model.Order order, boolean isSale, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OrderDetailModal(com.rendly.app.data.model.Order order, boolean isSale, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onMarkAsShipped, kotlin.jvm.functions.Function0<kotlin.Unit> onMarkAsDelivered) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OrderTimeline(com.rendly.app.data.model.Order order) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TimelineItem(com.rendly.app.ui.components.settings.TimelineStep step, boolean isLast) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SummaryRow(java.lang.String label, java.lang.String value, boolean isHighlighted) {
    }
    
    private static final java.lang.String formatTimestamp(java.lang.String timestamp) {
        return null;
    }
}