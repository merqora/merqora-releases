package com.rendly.app.ui.components.settings;

import android.widget.Toast;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.SecurityRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0003\u001a$\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00032\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u0010\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\nH\u0003\u001a \u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u0003H\u0003\u001a\u001e\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\n2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u0013H\u0007\u00a8\u0006\u0014"}, d2 = {"InstructionItem", "", "text", "", "OTPInput", "code", "onCodeChange", "Lkotlin/Function1;", "StepConnector", "isActive", "", "StepIndicator", "step", "", "currentStep", "label", "TwoFactorSetupScreen", "isVisible", "onDismiss", "Lkotlin/Function0;", "app_debug"})
public final class TwoFactorSetupScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void TwoFactorSetupScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StepIndicator(int step, int currentStep, java.lang.String label) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StepConnector(boolean isActive) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void InstructionItem(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OTPInput(java.lang.String code, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onCodeChange) {
    }
}