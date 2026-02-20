package com.rendly.app.ui.components.settings;

import android.widget.Toast;
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
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.data.repository.VerificationRepository;
import com.rendly.app.data.repository.VerificationRepository.AccountDataForVerification;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000L\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\u001a2\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\t\u0010\n\u001a:\u0010\u000b\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0010\u0010\u0011\u001a2\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0013\u0010\n\u001a\u0018\u0010\u0014\u001a\u00020\u00012\u0006\u0010\u0015\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00020\u000fH\u0003\u001a2\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0015\u001a\u00020\u00052\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u0016\u001a\u00020\u000f2\b\b\u0002\u0010\u001b\u001a\u00020\u000fH\u0003\u001a*\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001a\u001c\u0010\u001f\u001a\u00020\u00012\b\u0010 \u001a\u0004\u0018\u00010!2\b\b\u0002\u0010\"\u001a\u00020#H\u0003\u001a\u00a4\u0001\u0010$\u001a\u00020\u00012\u0006\u0010%\u001a\u00020\u000f2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00010\'2\u0006\u0010(\u001a\u00020\u00052\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010*2\u0006\u0010+\u001a\u00020\u00052\u0012\u0010,\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010*2\u0006\u0010-\u001a\u00020\u00052\u0012\u0010.\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010*2\u0006\u0010/\u001a\u00020\u00052\u0012\u00100\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010*2\u0006\u00101\u001a\u00020\u000f2\f\u00102\u001a\b\u0012\u0004\u0012\u00020\u00010\'H\u0003\u001a\u001e\u00103\u001a\u00020\u00012\u0006\u0010%\u001a\u00020\u000f2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00010\'H\u0007\u001a2\u00104\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b5\u0010\n\u001a\b\u00106\u001a\u00020\u0001H\u0003\u001a\u0012\u00107\u001a\u00020\u00192\b\u0010 \u001a\u0004\u0018\u00010!H\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00068"}, d2 = {"AccountDataRow", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "label", "", "value", "iconColor", "Landroidx/compose/ui/graphics/Color;", "AccountDataRow-g2O1Hgs", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;J)V", "ActiveBenefitItem", "title", "description", "isActive", "", "ActiveBenefitItem-42QJj7c", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;JZ)V", "BenefitItem", "BenefitItem-g2O1Hgs", "RequirementItem", "text", "isMet", "RequirementItemWithProgress", "current", "", "target", "isPercentage", "StatColumn", "StatColumn-mxwnekA", "(Ljava/lang/String;Ljava/lang/String;J)V", "VerificationProbabilityBar", "accountData", "Lcom/rendly/app/data/repository/VerificationRepository$AccountDataForVerification;", "modifier", "Landroidx/compose/ui/Modifier;", "VerificationRequestModal", "isVisible", "onDismiss", "Lkotlin/Function0;", "selectedType", "onTypeChange", "Lkotlin/Function1;", "fullLegalName", "onFullLegalNameChange", "reasonForVerification", "onReasonChange", "notablePresence", "onNotablePresenceChange", "isSubmitting", "onSubmit", "VerificationScreen", "VerificationTypeItem", "VerificationTypeItem-g2O1Hgs", "VerifiedCelebrationContent", "calculateVerificationProbability", "app_debug"})
public final class VerificationScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void VerificationScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void VerifiedCelebrationContent() {
    }
    
    private static final int calculateVerificationProbability(com.rendly.app.data.repository.VerificationRepository.AccountDataForVerification accountData) {
        return 0;
    }
    
    @androidx.compose.runtime.Composable
    private static final void VerificationProbabilityBar(com.rendly.app.data.repository.VerificationRepository.AccountDataForVerification accountData, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void VerificationRequestModal(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, java.lang.String selectedType, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTypeChange, java.lang.String fullLegalName, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onFullLegalNameChange, java.lang.String reasonForVerification, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onReasonChange, java.lang.String notablePresence, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNotablePresenceChange, boolean isSubmitting, kotlin.jvm.functions.Function0<kotlin.Unit> onSubmit) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RequirementItem(java.lang.String text, boolean isMet) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RequirementItemWithProgress(java.lang.String text, int current, int target, boolean isMet, boolean isPercentage) {
    }
}