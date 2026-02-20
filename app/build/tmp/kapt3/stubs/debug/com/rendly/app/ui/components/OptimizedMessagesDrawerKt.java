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
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import android.widget.Toast;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.repository.ChatLabel;
import com.rendly.app.data.repository.ChatRepository;
import com.rendly.app.data.repository.Conversation;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000f\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a.\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a\b\u0010\n\u001a\u00020\u0004H\u0003\u001aD\u0010\u000b\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00022\u0006\u0010\u000f\u001a\u00020\u00102\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00022\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0012\u0010\u0013\u001a \u0010\u0014\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a\u001e\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a4\u0010\u001a\u001a\u00020\u00042\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00012\b\u0010\u001d\u001a\u0004\u0018\u00010\u00022\u0012\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00040\u001fH\u0003\u001a:\u0010 \u001a\u00020\u00042\u0006\u0010!\u001a\u00020\u00022\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00012\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00012\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a\u001e\u0010$\u001a\u00020\u00042\u0006\u0010%\u001a\u00020\u00182\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a,\u0010&\u001a\u00020\u00042\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u0014\b\u0002\u0010(\u001a\u000e\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00040\u001fH\u0003\u001a2\u0010*\u001a\u00020\u00042\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a2\u0010,\u001a\u00020\u00042\u0006\u0010%\u001a\u00020\u00182\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u0012\u0010-\u001a\u000e\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00040\u001fH\u0003\u001a>\u0010.\u001a\u00020\u00042\u0006\u0010%\u001a\u00020\u00182\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u0014\b\u0002\u0010(\u001a\u000e\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00040\u001f2\b\b\u0002\u0010/\u001a\u000200H\u0007\u001a\u001e\u00101\u001a\u00020\u00042\u0006\u00102\u001a\u00020)2\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a2\u00103\u001a\u00020\u00042\u0006\u00104\u001a\u00020\u00022\u0012\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00040\u001f2\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0003\u001a\u0012\u00107\u001a\u00020\u00022\b\u00108\u001a\u0004\u0018\u00010\u0002H\u0002\u001a\u0012\u00109\u001a\u00020\u00022\b\u0010:\u001a\u0004\u0018\u00010\u0002H\u0002\u001a\u000e\u0010;\u001a\b\u0012\u0004\u0012\u00020<0\u0001H\u0003\u001a\u0014\u0010=\u001a\u0004\u0018\u00010>2\b\u00108\u001a\u0004\u0018\u00010\u0002H\u0002\u001a\u0014\u0010?\u001a\u0004\u0018\u00010\r2\b\u0010@\u001a\u0004\u0018\u00010\u0002H\u0003\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006A"}, d2 = {"LABEL_COLORS", "", "", "ChatItemImproved", "", "chat", "Lcom/rendly/app/ui/components/ChatPreview;", "onClick", "Lkotlin/Function0;", "onLongClick", "ChatItemSkeleton", "ChatOptionItem", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "iconColor", "Landroidx/compose/ui/graphics/Color;", "subtitle", "ChatOptionItem-XO-JAsU", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;JLjava/lang/String;Lkotlin/jvm/functions/Function0;)V", "ChatOptionsModal", "onDismiss", "EmptyChatsState", "isSearching", "", "onNewMessage", "LabelFilterChips", "labels", "Lcom/rendly/app/data/repository/ChatLabel;", "selectedLabelId", "onLabelSelected", "Lkotlin/Function1;", "LabelPickerModal", "conversationId", "currentLabels", "allLabels", "LabelsManagerModal", "isVisible", "MessagesContent", "onClose", "onOpenChat", "Lcom/rendly/app/data/model/Usuario;", "MessagesHeader", "onManageLabels", "NewMessageModal", "onSelectUser", "OptimizedMessagesDrawer", "modifier", "Landroidx/compose/ui/Modifier;", "RealUserItem", "user", "SearchBarMessages", "query", "onQueryChange", "onClear", "formatLastMessagePreview", "message", "formatMessageTimeAgo", "timestamp", "getLabelIconOptions", "Lcom/rendly/app/ui/components/LabelIconOption;", "getMessagePreview", "Lcom/rendly/app/ui/components/MessagePreview;", "resolveIconByName", "name", "app_debug"})
public final class OptimizedMessagesDrawerKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<java.lang.String> LABEL_COLORS = null;
    
    @androidx.compose.runtime.Composable
    public static final void OptimizedMessagesDrawer(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Usuario, kotlin.Unit> onOpenChat, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void MessagesContent(kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Usuario, kotlin.Unit> onOpenChat) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void MessagesHeader(kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function0<kotlin.Unit> onNewMessage, kotlin.jvm.functions.Function0<kotlin.Unit> onManageLabels) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SearchBarMessages(java.lang.String query, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQueryChange, kotlin.jvm.functions.Function0<kotlin.Unit> onClear) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void ChatItemImproved(com.rendly.app.ui.components.ChatPreview chat, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, kotlin.jvm.functions.Function0<kotlin.Unit> onLongClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EmptyChatsState(boolean isSearching, kotlin.jvm.functions.Function0<kotlin.Unit> onNewMessage) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void NewMessageModal(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Usuario, kotlin.Unit> onSelectUser) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ChatItemSkeleton() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RealUserItem(com.rendly.app.data.model.Usuario user, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ChatOptionsModal(com.rendly.app.ui.components.ChatPreview chat, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    private static final com.rendly.app.ui.components.MessagePreview getMessagePreview(java.lang.String message) {
        return null;
    }
    
    private static final java.lang.String formatLastMessagePreview(java.lang.String message) {
        return null;
    }
    
    private static final java.lang.String formatMessageTimeAgo(java.lang.String timestamp) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void LabelFilterChips(java.util.List<com.rendly.app.data.repository.ChatLabel> labels, java.lang.String selectedLabelId, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onLabelSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void LabelPickerModal(java.lang.String conversationId, java.util.List<com.rendly.app.data.repository.ChatLabel> currentLabels, java.util.List<com.rendly.app.data.repository.ChatLabel> allLabels, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final androidx.compose.ui.graphics.vector.ImageVector resolveIconByName(java.lang.String name) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final java.util.List<com.rendly.app.ui.components.LabelIconOption> getLabelIconOptions() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void LabelsManagerModal(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}