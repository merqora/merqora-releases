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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BU\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\u0002\u0010\u0012J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0005H\u00c6\u0003J\t\u0010&\u001a\u00020\u0007H\u00c6\u0003J\u0016\u0010\'\u001a\u00020\tH\u00c6\u0003\u00f8\u0001\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b(\u0010\u001aJ\t\u0010)\u001a\u00020\u000bH\u00c6\u0003J\t\u0010*\u001a\u00020\rH\u00c6\u0003J\t\u0010+\u001a\u00020\u000fH\u00c6\u0003J\t\u0010,\u001a\u00020\u0011H\u00c6\u0003Jc\u0010-\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u00c6\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b.\u0010/J\u0013\u00100\u001a\u0002012\b\u00102\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00103\u001a\u000204H\u00d6\u0001J\t\u00105\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0019\u0010\b\u001a\u00020\t\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u001b\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u00066"}, d2 = {"Lcom/rendly/app/ui/components/StoryTextState;", "", "text", "", "fontSize", "", "fontOption", "Lcom/rendly/app/ui/components/FontOption;", "color", "Landroidx/compose/ui/graphics/Color;", "animation", "Lcom/rendly/app/ui/components/TextAnimation;", "effect", "Lcom/rendly/app/ui/components/TextEffect;", "alignment", "Lcom/rendly/app/ui/components/TextAlignOption;", "backgroundState", "Lcom/rendly/app/ui/components/TextBackgroundState;", "(Ljava/lang/String;FLcom/rendly/app/ui/components/FontOption;JLcom/rendly/app/ui/components/TextAnimation;Lcom/rendly/app/ui/components/TextEffect;Lcom/rendly/app/ui/components/TextAlignOption;Lcom/rendly/app/ui/components/TextBackgroundState;Lkotlin/jvm/internal/DefaultConstructorMarker;)V", "getAlignment", "()Lcom/rendly/app/ui/components/TextAlignOption;", "getAnimation", "()Lcom/rendly/app/ui/components/TextAnimation;", "getBackgroundState", "()Lcom/rendly/app/ui/components/TextBackgroundState;", "getColor-0d7_KjU", "()J", "J", "getEffect", "()Lcom/rendly/app/ui/components/TextEffect;", "getFontOption", "()Lcom/rendly/app/ui/components/FontOption;", "getFontSize", "()F", "getText", "()Ljava/lang/String;", "component1", "component2", "component3", "component4", "component4-0d7_KjU", "component5", "component6", "component7", "component8", "copy", "copy-uDo3WH8", "(Ljava/lang/String;FLcom/rendly/app/ui/components/FontOption;JLcom/rendly/app/ui/components/TextAnimation;Lcom/rendly/app/ui/components/TextEffect;Lcom/rendly/app/ui/components/TextAlignOption;Lcom/rendly/app/ui/components/TextBackgroundState;)Lcom/rendly/app/ui/components/StoryTextState;", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class StoryTextState {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String text = null;
    private final float fontSize = 0.0F;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.ui.components.FontOption fontOption = null;
    private final long color = 0L;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.ui.components.TextAnimation animation = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.ui.components.TextEffect effect = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.ui.components.TextAlignOption alignment = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.ui.components.TextBackgroundState backgroundState = null;
    
    private StoryTextState(java.lang.String text, float fontSize, com.rendly.app.ui.components.FontOption fontOption, long color, com.rendly.app.ui.components.TextAnimation animation, com.rendly.app.ui.components.TextEffect effect, com.rendly.app.ui.components.TextAlignOption alignment, com.rendly.app.ui.components.TextBackgroundState backgroundState) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getText() {
        return null;
    }
    
    public final float getFontSize() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.FontOption getFontOption() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextAnimation getAnimation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextEffect getEffect() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextAlignOption getAlignment() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextBackgroundState getBackgroundState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final float component2() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.FontOption component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextAnimation component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextEffect component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextAlignOption component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.TextBackgroundState component8() {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}