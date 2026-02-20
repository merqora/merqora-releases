package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.ExperimentalComposeUiApi;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.platform.LocalSoftwareKeyboardController;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.unit.Dp;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0082\u0001\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a.\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00062\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00120\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0003\u001a8\u0010\u0018\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\t2\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00120\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0019\u0010\u001a\u001a.\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\f2\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00120\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0003\u001a.\u0010\u001c\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00022\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00120\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0003\u001aX\u0010\u001d\u001a\u00020\u00122\u0006\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020!2\u0012\u0010#\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u00120\u00152\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00120&2\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\'\u0010(\u001a@\u0010)\u001a\u00020\u00122\b\u0010*\u001a\u0004\u0018\u00010+2\u0006\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020/2\u0012\u00100\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00120\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0003\u001a<\u00101\u001a\u00020\u00122\u0006\u00102\u001a\u0002032\u0012\u00104\u001a\u000e\u0012\u0004\u0012\u000203\u0012\u0004\u0012\u00020\u00120\u00152\f\u00105\u001a\b\u0012\u0004\u0012\u000203062\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0003\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\"\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u0004\"\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u0004\"\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u0004\"\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0004\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00067"}, d2 = {"FONT_OPTIONS", "", "Lcom/rendly/app/ui/components/FontOption;", "getFONT_OPTIONS", "()Ljava/util/List;", "TEXT_ANIMATIONS", "Lcom/rendly/app/ui/components/TextAnimation;", "getTEXT_ANIMATIONS", "TEXT_COLORS", "Landroidx/compose/ui/graphics/Color;", "getTEXT_COLORS", "TEXT_EFFECTS", "Lcom/rendly/app/ui/components/TextEffect;", "getTEXT_EFFECTS", "TEXT_TOOLS", "Lcom/rendly/app/ui/components/TextTool;", "getTEXT_TOOLS", "AnimationCarousel", "", "selected", "onSelect", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "ColorCarousel", "ColorCarousel-ek8zF_U", "(JLkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;)V", "EffectsCarousel", "FontCarousel", "StoryTextEditor", "visible", "", "keyboardHeight", "Landroidx/compose/ui/unit/Dp;", "previewHeight", "onTextStateChanged", "Lcom/rendly/app/ui/components/StoryTextState;", "onDismiss", "Lkotlin/Function0;", "StoryTextEditor-WMci_g0", "(ZFFLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;)V", "TextToolbar", "selectedTool", "Lcom/rendly/app/ui/components/TextToolType;", "alignment", "Lcom/rendly/app/ui/components/TextAlignOption;", "backgroundState", "Lcom/rendly/app/ui/components/TextBackgroundState;", "onToolSelected", "VerticalFontSizeSlider", "value", "", "onValueChange", "valueRange", "Lkotlin/ranges/ClosedFloatingPointRange;", "app_debug"})
public final class StoryTextEditorKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.TextTool> TEXT_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.FontOption> FONT_OPTIONS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<androidx.compose.ui.graphics.Color> TEXT_COLORS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.TextAnimation> TEXT_ANIMATIONS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.TextEffect> TEXT_EFFECTS = null;
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.TextTool> getTEXT_TOOLS() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.FontOption> getFONT_OPTIONS() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<androidx.compose.ui.graphics.Color> getTEXT_COLORS() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.TextAnimation> getTEXT_ANIMATIONS() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.TextEffect> getTEXT_EFFECTS() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void VerticalFontSizeSlider(float value, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onValueChange, kotlin.ranges.ClosedFloatingPointRange<java.lang.Float> valueRange, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TextToolbar(com.rendly.app.ui.components.TextToolType selectedTool, com.rendly.app.ui.components.TextAlignOption alignment, com.rendly.app.ui.components.TextBackgroundState backgroundState, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.TextTool, kotlin.Unit> onToolSelected, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void FontCarousel(com.rendly.app.ui.components.FontOption selected, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.FontOption, kotlin.Unit> onSelect, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AnimationCarousel(com.rendly.app.ui.components.TextAnimation selected, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.TextAnimation, kotlin.Unit> onSelect, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EffectsCarousel(com.rendly.app.ui.components.TextEffect selected, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.TextEffect, kotlin.Unit> onSelect, androidx.compose.ui.Modifier modifier) {
    }
}