package com.rendly.app.ui.components.settings;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Address;
import com.rendly.app.data.model.AddressStatus;
import com.rendly.app.data.model.AddressType;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.ui.theme.*;
import com.rendly.app.ui.viewmodel.AddressViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a:\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a(\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u00a8\u0006\u000e"}, d2 = {"AddressCardNew", "", "address", "Lcom/rendly/app/data/model/Address;", "onSetDefault", "Lkotlin/Function0;", "onEdit", "onDelete", "AddressesScreen", "isVisible", "", "onDismiss", "viewModel", "Lcom/rendly/app/ui/viewmodel/AddressViewModel;", "app_debug"})
public final class AddressesScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void AddressesScreen(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.viewmodel.AddressViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AddressCardNew(com.rendly.app.data.model.Address address, kotlin.jvm.functions.Function0<kotlin.Unit> onSetDefault, kotlin.jvm.functions.Function0<kotlin.Unit> onEdit, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
}