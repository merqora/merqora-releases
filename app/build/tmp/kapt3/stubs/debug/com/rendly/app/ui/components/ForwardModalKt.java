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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.repository.ChatRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010 \n\u0002\b\u0005\u001a\b\u0010\u0000\u001a\u00020\u0001H\u0003\u001aH\u0010\u0002\u001a\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\u001e\u0010\t\u001a\u001a\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\nH\u0007\u001aB\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\f2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\u00102\u0006\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0003\u001a\u0010\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0006H\u0003\u001a\u0018\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u0004H\u0003\u001aV\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\f2\u0012\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\u00102\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001d2\u0006\u0010\u001e\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00132\u0012\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u0010H\u0003\u001a\u001e\u0010 \u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u000b2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0003\u00a8\u0006\""}, d2 = {"EmptySearchState", "", "ForwardModal", "isVisible", "", "post", "Lcom/rendly/app/data/model/Post;", "onDismiss", "Lkotlin/Function0;", "onForwardToUser", "Lkotlin/Function3;", "Lcom/rendly/app/data/model/Usuario;", "", "MessageInputBar", "message", "onMessageChange", "Lkotlin/Function1;", "isSending", "focusRequester", "Landroidx/compose/ui/focus/FocusRequester;", "onSend", "PostPreviewCard", "SelectedUserContent", "user", "showSuccess", "UserSearchContent", "searchQuery", "onSearchChange", "searchResults", "", "isSearching", "onUserSelect", "UserSelectItem", "onClick", "app_debug"})
public final class ForwardModalKt {
    
    @androidx.compose.runtime.Composable
    public static final void ForwardModal(boolean isVisible, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.Post post, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function3<? super com.rendly.app.data.model.Usuario, ? super com.rendly.app.data.model.Post, ? super java.lang.String, kotlin.Unit> onForwardToUser) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PostPreviewCard(com.rendly.app.data.model.Post post) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserSearchContent(java.lang.String searchQuery, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSearchChange, java.util.List<com.rendly.app.data.model.Usuario> searchResults, boolean isSearching, androidx.compose.ui.focus.FocusRequester focusRequester, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Usuario, kotlin.Unit> onUserSelect) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserSelectItem(com.rendly.app.data.model.Usuario user, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SelectedUserContent(com.rendly.app.data.model.Usuario user, boolean showSuccess) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void MessageInputBar(java.lang.String message, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onMessageChange, boolean isSending, androidx.compose.ui.focus.FocusRequester focusRequester, kotlin.jvm.functions.Function0<kotlin.Unit> onSend) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EmptySearchState() {
    }
}