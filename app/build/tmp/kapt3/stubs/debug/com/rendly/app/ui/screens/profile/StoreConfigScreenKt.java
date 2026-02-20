package com.rendly.app.ui.screens.profile;

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
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000T\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u001a\u001a\b\u0010\u0003\u001a\u00020\u0004H\u0003\u001aR\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0007H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0011\u0010\u0012\u001aN\u0010\u0013\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u00072\u0006\u0010\u0016\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\r2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0017\u0010\u0018\u001a\\\u0010\u0019\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u00072\u0012\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u0010\u001c\u001a\u00020\u00072\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u0010\u001e\u001a\u00020\u00072\u0012\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00040\u000fH\u0003\u001a6\u0010 \u001a\u00020\u00042\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"2\u0006\u0010$\u001a\u00020\r2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00040&2\u0006\u0010\'\u001a\u00020\rH\u0003\u001a4\u0010(\u001a\u00020\u00042\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00040&2\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00040&H\u0003\u001aE\u0010+\u001a\u00020\u00042\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00040&2#\b\u0002\u0010,\u001a\u001d\u0012\u0013\u0012\u00110\r\u00a2\u0006\f\b-\u0012\b\b\u0006\u0012\u0004\b\b(.\u0012\u0004\u0012\u00020\u00040\u000f2\b\b\u0002\u0010/\u001a\u000200H\u0007\u001a@\u00101\u001a\u00020\u00042\u0006\u00102\u001a\u00020\r2\u0012\u00103\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u00104\u001a\u00020\r2\u0012\u00105\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000fH\u0003\u001a\u00b0\u0001\u00106\u001a\u00020\u00042\u0006\u00107\u001a\u00020\r2\u0012\u00108\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u00109\u001a\u00020\r2\u0012\u0010:\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u0010;\u001a\u00020\u00072\u0012\u0010<\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u0010=\u001a\u00020\r2\u0012\u0010>\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u0010?\u001a\u00020\r2\u0012\u0010@\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000f2\u0006\u0010A\u001a\u00020\r2\u0012\u0010B\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00040\u000fH\u0003\u001a&\u0010C\u001a\u00020\u00042\u0006\u0010D\u001a\u00020\u00022\u0006\u0010E\u001a\u00020\r2\f\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00040&H\u0003\u001a&\u0010G\u001a\u00020\u00042\b\u0010H\u001a\u0004\u0018\u00010\u00072\u0012\u0010I\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00040\u000fH\u0003\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006J"}, d2 = {"STORE_TYPE_OPTIONS", "", "Lcom/rendly/app/ui/screens/profile/StoreTypeOption;", "CasualSellerInfo", "", "PaymentMethodToggle", "name", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "color", "Landroidx/compose/ui/graphics/Color;", "isEnabled", "", "onToggle", "Lkotlin/Function1;", "badge", "PaymentMethodToggle-FNF3uiM", "(Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JZLkotlin/jvm/functions/Function1;Ljava/lang/String;)V", "SettingToggleCard", "iconColor", "title", "description", "SettingToggleCard-3IgeMak", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function1;)V", "StoreBasicInfo", "storeName", "onStoreNameChange", "storeDescription", "onStoreDescriptionChange", "storeCategory", "onStoreCategoryChange", "StoreConfigBottomAction", "currentStep", "", "totalSteps", "canContinue", "onContinue", "Lkotlin/Function0;", "isLastStep", "StoreConfigHeader", "onBack", "onClose", "StoreConfigScreen", "onStoreConfigured", "Lkotlin/ParameterName;", "hasStore", "modifier", "Landroidx/compose/ui/Modifier;", "StoreLocationAndShipping", "hasPhysicalLocation", "onPhysicalLocationChange", "shipsNationwide", "onShipsNationwideChange", "StorePoliciesAndPayments", "acceptsReturns", "onAcceptsReturnsChange", "offersWarranty", "onOffersWarrantyChange", "warrantyDays", "onWarrantyDaysChange", "acceptsMercadoPago", "onAcceptsMercadoPagoChange", "acceptsCash", "onAcceptsCashChange", "acceptsTransfer", "onAcceptsTransferChange", "StoreTypeCard", "option", "isSelected", "onClick", "StoreTypeSelection", "selectedType", "onTypeSelected", "app_debug"})
public final class StoreConfigScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.profile.StoreTypeOption> STORE_TYPE_OPTIONS = null;
    
    @androidx.compose.runtime.Composable
    public static final void StoreConfigScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onStoreConfigured, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoreConfigHeader(int currentStep, int totalSteps, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onClose) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoreTypeSelection(java.lang.String selectedType, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTypeSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoreTypeCard(com.rendly.app.ui.screens.profile.StoreTypeOption option, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoreBasicInfo(java.lang.String storeName, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStoreNameChange, java.lang.String storeDescription, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStoreDescriptionChange, java.lang.String storeCategory, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStoreCategoryChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CasualSellerInfo() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoreLocationAndShipping(boolean hasPhysicalLocation, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onPhysicalLocationChange, boolean shipsNationwide, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onShipsNationwideChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StorePoliciesAndPayments(boolean acceptsReturns, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onAcceptsReturnsChange, boolean offersWarranty, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onOffersWarrantyChange, java.lang.String warrantyDays, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onWarrantyDaysChange, boolean acceptsMercadoPago, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onAcceptsMercadoPagoChange, boolean acceptsCash, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onAcceptsCashChange, boolean acceptsTransfer, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onAcceptsTransferChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoreConfigBottomAction(int currentStep, int totalSteps, boolean canContinue, kotlin.jvm.functions.Function0<kotlin.Unit> onContinue, boolean isLastStep) {
    }
}