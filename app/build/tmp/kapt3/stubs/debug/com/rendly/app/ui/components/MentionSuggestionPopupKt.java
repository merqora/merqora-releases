package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.repository.UserPreferencesRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\u001aS\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052!\u0010\u0006\u001a\u001d\u0012\u0013\u0012\u00110\u0005\u00a2\u0006\f\b\b\u0012\b\b\t\u0012\u0004\b\b(\n\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\f2\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0007\u001a\u001e\u0010\u000f\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\fH\u0003\u001a\u001a\u0010\u0013\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0014\u001a\u00020\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u001a \u0010\u0017\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u00a8\u0006\u0018"}, d2 = {"MentionSuggestionPopup", "", "isVisible", "", "query", "", "onUserSelected", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "username", "onDismiss", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "MentionUserGridItem", "user", "Lcom/rendly/app/data/repository/UserPreferencesRepository$MentionUserDB;", "onClick", "extractMentionQuery", "text", "cursorPosition", "", "insertMention", "app_debug"})
public final class MentionSuggestionPopupKt {
    
    /**
     * Professional mention popup with 4-column grid layout.
     * Each item shows avatar on top + @username below.
     * Appears above the text input when user types '@'.
     */
    @androidx.compose.runtime.Composable
    public static final void MentionSuggestionPopup(boolean isVisible, @org.jetbrains.annotations.NotNull
    java.lang.String query, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onUserSelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void MentionUserGridItem(com.rendly.app.data.repository.UserPreferencesRepository.MentionUserDB user, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    /**
     * Helper to detect '@' mention trigger in text.
     * Returns the query after '@' if user is currently typing a mention, null otherwise.
     */
    @org.jetbrains.annotations.Nullable
    public static final java.lang.String extractMentionQuery(@org.jetbrains.annotations.NotNull
    java.lang.String text, int cursorPosition) {
        return null;
    }
    
    /**
     * Replaces the current @query with the selected username in the text.
     */
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String insertMention(@org.jetbrains.annotations.NotNull
    java.lang.String text, @org.jetbrains.annotations.NotNull
    java.lang.String username, int cursorPosition) {
        return null;
    }
}