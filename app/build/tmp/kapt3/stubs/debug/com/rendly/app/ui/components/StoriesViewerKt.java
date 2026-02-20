package com.rendly.app.ui.components;

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
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.repository.StoryRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000N\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\u0007\u001aV\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a:\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u00b6\u0001\u0010\u0012\u001a\u00020\u00012\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\b\b\u0002\u0010\u0016\u001a\u00020\u000e2\b\b\u0002\u0010\u0017\u001a\u00020\u00032\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0014\b\u0002\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\u001a\b\u0002\u0010\u001a\u001a\u0014\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u001b2\u0014\b\u0002\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\u0014\b\u0002\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\u0014\b\u0002\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u001f\u001a\u00020 H\u0007\u001aH\u0010!\u001a\u00020\u00012\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u0010\u0010&\u001a\u00020\u00032\u0006\u0010\'\u001a\u00020(H\u0002\u001a\\\u0010)\u001a\u00020\u00012\u0006\u0010*\u001a\u00020\u000e2\u0006\u0010+\u001a\u00020\u000e2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\u0012\u0010,\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010-\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0002\u001aN\u0010.\u001a\u00020\u00012\u0006\u0010*\u001a\u00020\u000e2\u0006\u0010+\u001a\u00020\u000e2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\u0012\u0010,\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010-\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\u0005H\u0002\u00a8\u0006/"}, d2 = {"OtherStoryBottomBar", "", "replyText", "", "onReplyChange", "Lkotlin/Function1;", "onSendReply", "Lkotlin/Function0;", "isLiked", "", "onLikeClick", "onForwardClick", "OwnStoryBottomBar", "viewsCount", "", "onViewsClick", "onDeleteClick", "onShareClick", "StoriesViewer", "userStories", "", "Lcom/rendly/app/ui/components/UserStories;", "initialUserIndex", "currentUserId", "onClose", "onStoryViewed", "onReply", "Lkotlin/Function2;", "onLike", "onShare", "onDeleteStory", "modifier", "Landroidx/compose/ui/Modifier;", "StoryOptionsModal", "isOwnStory", "onDismiss", "onDelete", "onReport", "getTimeAgo", "timestamp", "", "goToNextStory", "currentUserIndex", "currentStoryIndex", "onUserIndexChange", "onStoryIndexChange", "goToPreviousStory", "app_debug"})
public final class StoriesViewerKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.layout.ExperimentalLayoutApi.class})
    @androidx.compose.runtime.Composable
    public static final void StoriesViewer(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.ui.components.UserStories> userStories, int initialUserIndex, @org.jetbrains.annotations.NotNull
    java.lang.String currentUserId, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStoryViewed, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onReply, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onLike, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onShare, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onDeleteStory, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OwnStoryBottomBar(int viewsCount, kotlin.jvm.functions.Function0<kotlin.Unit> onViewsClick, kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteClick, kotlin.jvm.functions.Function0<kotlin.Unit> onShareClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OtherStoryBottomBar(java.lang.String replyText, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onReplyChange, kotlin.jvm.functions.Function0<kotlin.Unit> onSendReply, boolean isLiked, kotlin.jvm.functions.Function0<kotlin.Unit> onLikeClick, kotlin.jvm.functions.Function0<kotlin.Unit> onForwardClick) {
    }
    
    private static final void goToNextStory(int currentUserIndex, int currentStoryIndex, java.util.List<com.rendly.app.ui.components.UserStories> userStories, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onUserIndexChange, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onStoryIndexChange, kotlin.jvm.functions.Function0<kotlin.Unit> onClose) {
    }
    
    private static final void goToPreviousStory(int currentUserIndex, int currentStoryIndex, java.util.List<com.rendly.app.ui.components.UserStories> userStories, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onUserIndexChange, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onStoryIndexChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoryOptionsModal(boolean isOwnStory, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, kotlin.jvm.functions.Function0<kotlin.Unit> onShare, kotlin.jvm.functions.Function0<kotlin.Unit> onReport) {
    }
    
    private static final java.lang.String getTimeAgo(long timestamp) {
        return null;
    }
}