package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import coil.request.ImageRequest;
import coil.request.CachePolicy;
import com.rendly.app.data.repository.StoryRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000:\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\u001aJ\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0018\u0010\b\u001a\u0014\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\t2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0007\u001a:\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u00072\b\u0010\u000f\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0010\u001a\u00020\n2\b\b\u0002\u0010\u0011\u001a\u00020\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u0014H\u0003\u00a8\u0006\u0015"}, d2 = {"StoriesCarousel", "", "stories", "", "Lcom/rendly/app/data/repository/StoryRepository$StoryWithUser;", "viewedStoryIds", "", "", "onStoryClick", "Lkotlin/Function2;", "", "modifier", "Landroidx/compose/ui/Modifier;", "StoryAvatarItem", "username", "avatarUrl", "storiesCount", "isViewed", "", "onClick", "Lkotlin/Function0;", "app_debug"})
public final class StoriesCarouselKt {
    
    /**
     * Carrusel horizontal de stories de otros usuarios
     * Se muestra debajo de MyStoryBanner cuando hay stories disponibles
     * Si no hay stories, el componente NO se muestra
     */
    @androidx.compose.runtime.Composable
    public static final void StoriesCarousel(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.repository.StoryRepository.StoryWithUser> stories, @org.jetbrains.annotations.NotNull
    java.util.Set<java.lang.String> viewedStoryIds, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Integer, kotlin.Unit> onStoryClick, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoryAvatarItem(java.lang.String username, java.lang.String avatarUrl, int storiesCount, boolean isViewed, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}