package com.rendly.app.ui.components.settings;

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
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.ui.theme.*;
import com.rendly.app.data.repository.AISupportRepository;
import com.rendly.app.data.remote.SupabaseClient;
import io.github.jan.supabase.realtime.PostgresAction;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000Z\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010 \n\u0002\b\u0002\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\b\u0010\n\u001a\u00020\u0001H\u0003\u001a.\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00032\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aB\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u00172\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0018\u0010\u0019\u001a\u001e\u0010\u001a\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a2\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\u001c\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u0015H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001a\u001e\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u0010 \u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u0010!\u001a\u00020\u00012\u0006\u0010\"\u001a\u00020\r2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aV\u0010#\u001a\u00020\u00012\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\r2\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\'\u0012\u0004\u0012\u00020\u00010*2\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00010*2\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u0010-\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u0010.\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u0010/\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001e\u00100\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a&\u00101\u001a\u00020\u00012\u0006\u0010$\u001a\u00020%2\u0014\b\u0002\u00102\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010*H\u0003\u001a\b\u00103\u001a\u00020\u0001H\u0003\u001a8\u00104\u001a\u00020\u00012\u0006\u00105\u001a\u00020\r2\f\u00106\u001a\b\u0012\u0004\u0012\u00020%072\u0018\u00108\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020%07\u0012\u0004\u0012\u00020\u00010*H\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00069"}, d2 = {"AccountHelpScreen", "", "isVisible", "", "onDismiss", "Lkotlin/Function0;", "ActionButtonChip", "button", "Lcom/rendly/app/data/repository/AISupportRepository$ActionButton;", "onClick", "AgentHandoffBanner", "FaqItem", "question", "", "answer", "isExpanded", "HelpCategoryCard", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "color", "Landroidx/compose/ui/graphics/Color;", "modifier", "Landroidx/compose/ui/Modifier;", "HelpCategoryCard-XO-JAsU", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;JLandroidx/compose/ui/Modifier;Lkotlin/jvm/functions/Function0;)V", "HelpCenterScreen", "HelpSection", "content", "HelpSection-g2O1Hgs", "(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;J)V", "PaymentsHelpScreen", "PurchasesHelpScreen", "QuickActionChip", "text", "RatingMessageBubble", "message", "Lcom/rendly/app/ui/components/settings/SupportMessage;", "selectedRating", "", "ratingComment", "onRatingChange", "Lkotlin/Function1;", "onCommentChange", "onSubmit", "SalesHelpScreen", "SecurityHelpScreen", "ShippingHelpScreen", "SupportChatScreen", "SupportMessageBubble", "onActionClick", "TypingIndicator", "simulateSupportResponse", "response", "currentMessages", "", "onComplete", "app_debug"})
public final class HelpCenterScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void HelpCenterScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void FaqItem(java.lang.String question, java.lang.String answer, boolean isExpanded, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PurchasesHelpScreen(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SalesHelpScreen(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AccountHelpScreen(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentsHelpScreen(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ShippingHelpScreen(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SecurityHelpScreen(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SupportChatScreen(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SupportMessageBubble(com.rendly.app.ui.components.settings.SupportMessage message, kotlin.jvm.functions.Function1<? super com.rendly.app.data.repository.AISupportRepository.ActionButton, kotlin.Unit> onActionClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AgentHandoffBanner() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RatingMessageBubble(com.rendly.app.ui.components.settings.SupportMessage message, int selectedRating, java.lang.String ratingComment, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onRatingChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onCommentChange, kotlin.jvm.functions.Function0<kotlin.Unit> onSubmit) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ActionButtonChip(com.rendly.app.data.repository.AISupportRepository.ActionButton button, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void QuickActionChip(java.lang.String text, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TypingIndicator() {
    }
    
    private static final void simulateSupportResponse(java.lang.String response, java.util.List<com.rendly.app.ui.components.settings.SupportMessage> currentMessages, kotlin.jvm.functions.Function1<? super java.util.List<com.rendly.app.ui.components.settings.SupportMessage>, kotlin.Unit> onComplete) {
    }
}