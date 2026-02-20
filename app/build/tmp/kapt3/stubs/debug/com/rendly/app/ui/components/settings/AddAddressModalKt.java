package com.rendly.app.ui.components.settings;

import android.Manifest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.compose.ui.Modifier;
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.input.KeyboardCapitalization;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.*;
import com.rendly.app.engine.AddressEngine;
import com.rendly.app.ui.theme.*;
import com.rendly.app.ui.viewmodel.AddressEvent;
import com.rendly.app.ui.viewmodel.AddressUiState;
import com.rendly.app.ui.viewmodel.AddressViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000r\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a4\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006H\u0003\u001aD\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00010\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u0007\u001an\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u000b2\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000e2\u0006\u0010\u0016\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u00062\b\b\u0002\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010\u001e\u001a\u00020\u001fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b \u0010!\u001a\\\u0010\"\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u000b2\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020%0$2\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00010\u000e2\u0006\u0010\'\u001a\u00020\u00062\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a0\u0010)\u001a\u00020\u00012\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u00062\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\u0019\u001a\u00020\u001aH\u0003\u001a$\u0010.\u001a\u00020\u00012\u0006\u0010/\u001a\u00020+2\u0012\u00100\u001a\u000e\u0012\u0004\u0012\u00020+\u0012\u0004\u0012\u00020\u00010\u000eH\u0003\u001a6\u00101\u001a\u00020\u00012\u0006\u00102\u001a\u0002032\u0006\u00104\u001a\u0002032\u0006\u00105\u001a\u00020\u000b2\u0006\u00106\u001a\u00020\u000b2\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a\u001e\u00108\u001a\u00020\u00012\u0006\u00109\u001a\u00020%2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a\u0016\u0010:\u001a\u00020\u00012\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a$\u0010;\u001a\u00020\u00012\u0006\u0010<\u001a\u00020\u00062\u0012\u0010=\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u000eH\u0003\u001a\u001e\u0010>\u001a\u00020\u00012\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0006\u0010?\u001a\u00020\u0006H\u0003\u001a\u001a\u0010@\u001a\u00020\u00012\b\u0010A\u001a\u0004\u0018\u00010B2\u0006\u0010C\u001a\u00020\u0006H\u0003\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006D"}, d2 = {"AddAddressHeader", "", "onClose", "Lkotlin/Function0;", "onSave", "isSaving", "", "canSave", "AddAddressModal", "isVisible", "userId", "", "onDismiss", "onAddressAdded", "Lkotlin/Function1;", "Lcom/rendly/app/data/model/Address;", "viewModel", "Lcom/rendly/app/ui/viewmodel/AddressViewModel;", "AddressFormField", "label", "value", "onValueChange", "placeholder", "leadingIcon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "modifier", "Landroidx/compose/ui/Modifier;", "singleLine", "maxLines", "", "keyboardType", "Landroidx/compose/ui/text/input/KeyboardType;", "AddressFormField-bmlC4ec", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;Landroidx/compose/ui/Modifier;ZII)V", "AddressSearchField", "predictions", "", "Lcom/rendly/app/data/model/AddressPrediction;", "onPredictionSelected", "isSearching", "onSearch", "AddressTypeChip", "type", "Lcom/rendly/app/data/model/AddressType;", "isSelected", "onClick", "AddressTypeSelector", "selectedType", "onTypeSelected", "MapPreviewCard", "latitude", "", "longitude", "formattedAddress", "mapUrl", "onMapClick", "PredictionItem", "prediction", "SelectOnMapButton", "SetAsDefaultSwitch", "isDefault", "onToggle", "UseCurrentLocationButton", "isLoading", "ValidationStatusCard", "validationResult", "Lcom/rendly/app/engine/AddressEngine$ValidationResult;", "isValidating", "app_debug"})
public final class AddAddressModalKt {
    
    /**
     * ══════════════════════════════════════════════════════════════════════════════
     * ADD ADDRESS MODAL - Modal full-screen para agregar direcciones
     * ══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    public static final void AddAddressModal(boolean isVisible, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Address, kotlin.Unit> onAddressAdded, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.viewmodel.AddressViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AddAddressHeader(kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function0<kotlin.Unit> onSave, boolean isSaving, boolean canSave) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UseCurrentLocationButton(kotlin.jvm.functions.Function0<kotlin.Unit> onClick, boolean isLoading) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SelectOnMapButton(kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AddressSearchField(java.lang.String value, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onValueChange, java.util.List<com.rendly.app.data.model.AddressPrediction> predictions, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.AddressPrediction, kotlin.Unit> onPredictionSelected, boolean isSearching, kotlin.jvm.functions.Function0<kotlin.Unit> onSearch) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PredictionItem(com.rendly.app.data.model.AddressPrediction prediction, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ValidationStatusCard(com.rendly.app.engine.AddressEngine.ValidationResult validationResult, boolean isValidating) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void MapPreviewCard(double latitude, double longitude, java.lang.String formattedAddress, java.lang.String mapUrl, kotlin.jvm.functions.Function0<kotlin.Unit> onMapClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AddressTypeSelector(com.rendly.app.data.model.AddressType selectedType, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.AddressType, kotlin.Unit> onTypeSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AddressTypeChip(com.rendly.app.data.model.AddressType type, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SetAsDefaultSwitch(boolean isDefault, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onToggle) {
    }
}