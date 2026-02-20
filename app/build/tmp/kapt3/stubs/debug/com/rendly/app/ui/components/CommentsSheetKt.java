package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import com.rendly.app.ui.theme.*;
import androidx.compose.ui.text.SpanStyle;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\t\u001aB\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0003\u001a\b\u0010\t\u001a\u00020\u0001H\u0003\u001a\u00de\u0001\u0010\n\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u0018\u0010\u000e\u001a\u0014\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u000f2\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00010\u00132\u001a\b\u0002\u0010\u0014\u001a\u0014\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00010\u000f2\u0014\b\u0002\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00010\u00132\b\b\u0002\u0010\u0016\u001a\u00020\u00032\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00102\b\b\u0002\u0010\u0018\u001a\u00020\u00102\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u00102\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u001c\u001a\u00020\u001dH\u0007\u001aH\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\r2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u000e\b\u0002\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u000e\b\u0002\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u0003\u001aR\u0010#\u001a\u00020\u00012\u0006\u0010$\u001a\u00020\r2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u000e\b\u0002\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u000e\b\u0002\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010%\u001a\u00020\u0003H\u0003\u00a8\u0006&"}, d2 = {"CommentOptionsSheet", "", "isVisible", "", "isOwnComment", "onDismiss", "Lkotlin/Function0;", "onDelete", "onReport", "CommentSkeletonItem", "CommentsSheet", "comments", "", "Lcom/rendly/app/ui/components/Comment;", "onSendComment", "Lkotlin/Function2;", "", "", "onLikeComment", "Lkotlin/Function1;", "onReplyComment", "onDeleteComment", "isLoading", "currentUserAvatar", "currentUsername", "currentUserId", "errorMessage", "onDismissError", "modifier", "Landroidx/compose/ui/Modifier;", "OpinionItem", "comment", "onLike", "onReply", "onOptions", "ReplyItemConnected", "reply", "isLast", "app_debug"})
public final class CommentsSheetKt {
    
    @androidx.compose.runtime.Composable
    public static final void CommentsSheet(boolean isVisible, @org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.ui.components.Comment> comments, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Integer, kotlin.Unit> onSendComment, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onLikeComment, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onReplyComment, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onDeleteComment, boolean isLoading, @org.jetbrains.annotations.Nullable
    java.lang.String currentUserAvatar, @org.jetbrains.annotations.NotNull
    java.lang.String currentUsername, @org.jetbrains.annotations.Nullable
    java.lang.String currentUserId, @org.jetbrains.annotations.Nullable
    java.lang.String errorMessage, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismissError, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OpinionItem(com.rendly.app.ui.components.Comment comment, kotlin.jvm.functions.Function0<kotlin.Unit> onLike, kotlin.jvm.functions.Function0<kotlin.Unit> onReply, kotlin.jvm.functions.Function0<kotlin.Unit> onOptions, boolean isOwnComment) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ReplyItemConnected(com.rendly.app.ui.components.Comment reply, kotlin.jvm.functions.Function0<kotlin.Unit> onLike, kotlin.jvm.functions.Function0<kotlin.Unit> onReply, kotlin.jvm.functions.Function0<kotlin.Unit> onOptions, boolean isOwnComment, boolean isLast) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CommentSkeletonItem() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CommentOptionsSheet(boolean isVisible, boolean isOwnComment, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, kotlin.jvm.functions.Function0<kotlin.Unit> onReport) {
    }
}