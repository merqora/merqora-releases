package com.rendly.app.ui.components.settings;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.drawscope.DrawScope;
import androidx.compose.ui.text.font.FontWeight;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.UserPreferencesRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00004\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\u001a\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0003\u001a\u001e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00040\u000bH\u0007\u001a\u001c\u0010\f\u001a\u00020\u0004*\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000fH\u0002\u001a\u001c\u0010\u0011\u001a\u00020\u0004*\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000fH\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"AVAILABLE_LANGUAGES", "", "Lcom/rendly/app/ui/components/settings/LanguageOption;", "LanguageFlagIcon", "", "languageCode", "", "LanguageSettingsScreen", "isVisible", "", "onDismiss", "Lkotlin/Function0;", "drawSpainFlag", "Landroidx/compose/ui/graphics/drawscope/DrawScope;", "w", "", "h", "drawUSAFlag", "app_debug"})
public final class LanguageSettingsScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.settings.LanguageOption> AVAILABLE_LANGUAGES = null;
    
    @androidx.compose.runtime.Composable
    public static final void LanguageSettingsScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * Professional Canvas-drawn flag icons for language selection
     */
    @androidx.compose.runtime.Composable
    private static final void LanguageFlagIcon(java.lang.String languageCode) {
    }
    
    private static final void drawSpainFlag(androidx.compose.ui.graphics.drawscope.DrawScope $this$drawSpainFlag, float w, float h) {
    }
    
    private static final void drawUSAFlag(androidx.compose.ui.graphics.drawscope.DrawScope $this$drawUSAFlag, float w, float h) {
    }
}