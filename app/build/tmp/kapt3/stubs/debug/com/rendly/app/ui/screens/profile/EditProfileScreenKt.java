package com.rendly.app.ui.screens.profile;

import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.VisualTransformation;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000v\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u001e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00032\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\b0\u000bH\u0003\u001a:\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\t\u001a\u00020\u00032\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\b0\u000b2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\b0\u0011H\u0003\u001a.\u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00032\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\b0\u000bH\u0003\u001am\u0010\u0017\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\u00032\u0006\u0010\u0019\u001a\u00020\u00032\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\b0\u00112\u0006\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\u00142\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u001d\u001a\u00020\u000e2\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u001fH\u0003\u00a2\u0006\u0002\u0010!\u001a4\u0010\"\u001a\u00020\b2\u0006\u0010#\u001a\u00020\u000e2\u0006\u0010$\u001a\u00020\u000e2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\b0\u000b2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\b0\u000bH\u0003\u001a\\\u0010\'\u001a\u00020\b2\b\b\u0002\u0010(\u001a\u00020)2\b\b\u0002\u0010$\u001a\u00020\u000e2$\b\u0002\u0010&\u001a\u001e\u0012\u0004\u0012\u00020)\u0012\u0006\u0012\u0004\u0018\u00010+\u0012\u0006\u0012\u0004\u0018\u00010+\u0012\u0004\u0012\u00020\b0*2\u000e\b\u0002\u0010%\u001a\b\u0012\u0004\u0012\u00020\b0\u000b2\b\b\u0002\u0010,\u001a\u00020-H\u0007\u001a$\u0010.\u001a\u00020\b2\u0006\u0010/\u001a\u00020\u00032\u0012\u00100\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\b0\u0011H\u0003\u001a~\u00101\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\b0\u000b2\b\b\u0002\u00102\u001a\u00020\u00032\u0014\b\u0002\u00103\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\b0\u00112\b\b\u0002\u00104\u001a\u00020\u00032\u0014\b\u0002\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\b0\u00112\b\b\u0002\u00106\u001a\u00020\u00032\u0014\b\u0002\u00107\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\b0\u0011H\u0003\u001aH\u00108\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00032\u0006\u00109\u001a\u00020:2\u001c\u0010;\u001a\u0018\u0012\u0004\u0012\u00020<\u0012\u0004\u0012\u00020\b0\u0011\u00a2\u0006\u0002\b=\u00a2\u0006\u0002\b>H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b?\u0010@\u001aH\u0010A\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\u00032\f\u0010B\u001a\b\u0012\u0004\u0012\u00020\u00030\u00012\u0006\u0010C\u001a\u00020\u00032\u0012\u0010D\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\b0\u00112\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\b0\u000bH\u0003\u001a\u0018\u0010E\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0003H\u0003\u001a\u0010\u0010F\u001a\u00020G2\u0006\u0010H\u001a\u00020\u0003H\u0002\" \u0010\u0000\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\" \u0010\u0005\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\" \u0010\u0006\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006I"}, d2 = {"ACCOUNT_TYPES", "", "Lkotlin/Pair;", "", "COUNTRIES", "GENDER_OPTIONS", "LANGUAGES", "AccountTypeCard", "", "currentType", "onClick", "Lkotlin/Function0;", "AccountTypeSelectionModal", "isVisible", "", "onDismiss", "onTypeSelected", "Lkotlin/Function1;", "ConfigButton", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "subtitle", "EditFieldPro", "label", "value", "onValueChange", "placeholder", "prefix", "multiline", "maxLines", "", "maxChars", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;ZILjava/lang/Integer;)V", "EditProfileHeader", "canSave", "isSaving", "onBack", "onSave", "EditProfileScreen", "initialData", "Lcom/rendly/app/ui/screens/profile/EditProfileData;", "Lkotlin/Function3;", "Landroid/net/Uri;", "modifier", "Landroidx/compose/ui/Modifier;", "GenderSelector", "selectedGender", "onGenderSelected", "PersonalInfoModal", "ubicacion", "onUbicacionChange", "telefono", "onTelefonoChange", "fechaNacimiento", "onFechaNacimientoChange", "PersonalInfoSection", "iconColor", "Landroidx/compose/ui/graphics/Color;", "content", "Landroidx/compose/foundation/layout/ColumnScope;", "Landroidx/compose/runtime/Composable;", "Lkotlin/ExtensionFunctionType;", "PersonalInfoSection-9LQNqLg", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;JLkotlin/jvm/functions/Function1;)V", "PickerModal", "options", "selectedOption", "onOptionSelected", "SectionHeader", "getAccountTypeInfo", "Lcom/rendly/app/ui/screens/profile/AccountTypeInfo;", "type", "app_debug"})
public final class EditProfileScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> ACCOUNT_TYPES = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> GENDER_OPTIONS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<java.lang.String> COUNTRIES = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> LANGUAGES = null;
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void EditProfileScreen(@org.jetbrains.annotations.NotNull
    com.rendly.app.ui.screens.profile.EditProfileData initialData, boolean isSaving, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function3<? super com.rendly.app.ui.screens.profile.EditProfileData, ? super android.net.Uri, ? super android.net.Uri, kotlin.Unit> onSave, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EditProfileHeader(boolean canSave, boolean isSaving, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onSave) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AccountTypeCard(java.lang.String currentType, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final com.rendly.app.ui.screens.profile.AccountTypeInfo getAccountTypeInfo(java.lang.String type) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void AccountTypeSelectionModal(boolean isVisible, java.lang.String currentType, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTypeSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SectionHeader(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EditFieldPro(java.lang.String label, java.lang.String value, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onValueChange, java.lang.String placeholder, androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String prefix, boolean multiline, int maxLines, java.lang.Integer maxChars) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void GenderSelector(java.lang.String selectedGender, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onGenderSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ConfigButton(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PersonalInfoModal(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, java.lang.String ubicacion, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onUbicacionChange, java.lang.String telefono, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTelefonoChange, java.lang.String fechaNacimiento, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onFechaNacimientoChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PickerModal(java.lang.String title, java.util.List<java.lang.String> options, java.lang.String selectedOption, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onOptionSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}