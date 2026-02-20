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
import androidx.compose.runtime.snapshots.SnapshotStateList;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextDecoration;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.activity.result.contract.ActivityResultContracts;
import com.rendly.app.data.model.Post;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0088\u0001\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a$\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001aT\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00032\u0018\u0010\r\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u000e0\u00020\u00012\u0014\b\u0002\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a0\u0010\u0012\u001a\u00020\u00062\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00030\u00012\u0018\u0010\u0014\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u0001\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a6\u0010\u0015\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u00032\b\u0010\u0017\u001a\u0004\u0018\u00010\u00032\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00060\u0019H\u0003\u001aa\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u001d\u001a\u00020\u00032\u0012\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00060\n2\u0006\u0010\u001f\u001a\u00020\u00032\u0006\u0010 \u001a\u00020\u000e2\b\b\u0002\u0010!\u001a\u00020\b2\b\b\u0002\u0010\"\u001a\u00020#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010#H\u0003\u00a2\u0006\u0002\u0010%\u001a>\u0010&\u001a\u00020\u00062\u0006\u0010\'\u001a\u00020(2\u000e\b\u0002\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00030\u00012\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\u000e\b\u0002\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00060\u0019H\u0003\u001aj\u0010,\u001a\u00020\u00062\u0006\u0010-\u001a\u00020\b2\b\u0010\'\u001a\u0004\u0018\u00010(2\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\u0012\u0010/\u001a\u000e\u0012\u0004\u0012\u000200\u0012\u0004\u0012\u00020\u00060\n2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\u000e\b\u0002\u00102\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\u000e\b\u0002\u00103\u001a\b\u0012\u0004\u0012\u00020\u00060\u0019H\u0007\u001a*\u00104\u001a\u00020\u00062\u0006\u0010 \u001a\u00020\u000e2\u0006\u00105\u001a\u00020\u00032\u0006\u00106\u001a\u00020\u0004H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b7\u00108\u001a`\u00109\u001a\u00020\u00062\u0006\u0010\'\u001a\u00020(2\u0014\b\u0002\u0010:\u001a\u000e\u0012\u0004\u0012\u00020#\u0012\u0004\u0012\u00020\u00030\u00102\f\u0010;\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192*\u0010<\u001a&\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u0001\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020#\u0012\u0004\u0012\u00020\u00030\u0010\u0012\u0004\u0012\u00020\u00060=H\u0003\u001as\u0010>\u001a\u00020\u00062\u0006\u0010?\u001a\u00020@2\b\u0010A\u001a\u0004\u0018\u00010@2\u0006\u0010B\u001a\u00020\b2\u0012\u0010C\u001a\u000e\u0012\u0004\u0012\u00020@\u0012\u0004\u0012\u00020\u00060\n2\u0014\u0010D\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010@\u0012\u0004\u0012\u00020\u00060\n2\u0012\u0010E\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\n2\f\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00060\u0019H\u0003\u00a2\u0006\u0002\u0010G\u001a\u00e1\u0001\u0010H\u001a\u00020\u00062\u0006\u0010I\u001a\u00020@2\b\u0010A\u001a\u0004\u0018\u00010@2\u0006\u0010B\u001a\u00020\b2\u000e\b\u0002\u0010J\u001a\b\u0012\u0004\u0012\u00020\u00030\u00012\u0014\b\u0002\u0010K\u001a\u000e\u0012\u0004\u0012\u00020#\u0012\u0004\u0012\u00020L0\u00102\u0014\b\u0002\u0010M\u001a\u000e\u0012\u0004\u0012\u00020#\u0012\u0004\u0012\u00020\u00030\u00102\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192n\u0010/\u001aj\u0012\u0013\u0012\u00110@\u00a2\u0006\f\bO\u0012\b\bP\u0012\u0004\b\b(Q\u0012\u0015\u0012\u0013\u0018\u00010@\u00a2\u0006\f\bO\u0012\b\bP\u0012\u0004\b\b(A\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\bO\u0012\b\bP\u0012\u0004\b\b(R\u0012\u001f\u0012\u001d\u0012\u0004\u0012\u00020#\u0012\u0004\u0012\u00020L0\u0010\u00a2\u0006\f\bO\u0012\b\bP\u0012\u0004\b\b(K\u0012\u0004\u0012\u00020\u00060NH\u0003\u00a2\u0006\u0002\u0010S\u001aB\u0010T\u001a\u00020\u00062\u0006\u0010 \u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u00032\u0006\u0010U\u001a\u00020\u00042\b\b\u0002\u0010V\u001a\u00020W2\f\u0010X\u001a\b\u0012\u0004\u0012\u00020\u00060\u0019H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\bY\u0010Z\u001a2\u0010[\u001a\u00020\u00062\f\u00102\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\f\u0010\\\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\f\u0010]\u001a\b\u0012\u0004\u0012\u00020\u00060\u0019H\u0003\u001aN\u0010^\u001a\u00020\u00062\u0006\u0010 \u001a\u00020\u000e2\u0006\u00105\u001a\u00020\u00032\u0006\u0010_\u001a\u00020\u00032\u0006\u0010`\u001a\u00020\b2\u0012\u0010a\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00060\n2\u0006\u00106\u001a\u00020\u0004H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\bb\u0010c\u001a$\u0010d\u001a\u00020\u00062\u0006\u0010e\u001a\u00020\u00032\u0012\u0010f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a0\u0010g\u001a\u000e\u0012\u0004\u0012\u0002Hi\u0012\u0004\u0012\u0002Hj0h\"\u0004\b\u0000\u0010i\"\u0004\b\u0001\u0010j*\u000e\u0012\u0004\u0012\u0002Hi\u0012\u0004\u0012\u0002Hj0\u0010H\u0002\" \u0010\u0000\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006k"}, d2 = {"availableProductColors", "", "Lkotlin/Pair;", "", "Landroidx/compose/ui/graphics/Color;", "AvailabilityCard", "", "isAvailable", "", "onAvailabilityChange", "Lkotlin/Function1;", "CategorySelector", "selectedCategory", "categories", "Landroidx/compose/ui/graphics/vector/ImageVector;", "categoryDisplayNames", "", "onCategorySelected", "ColorSelector", "selectedColors", "onColorsChanged", "DeleteConfirmationContent", "postTitle", "postImage", "onCancel", "Lkotlin/Function0;", "onConfirm", "EditPostField", "label", "value", "onValueChange", "placeholder", "icon", "multiline", "maxLines", "", "maxChars", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;ZILjava/lang/Integer;)V", "EditPostHeader", "post", "Lcom/rendly/app/data/model/Post;", "currentImages", "onClose", "onEditImages", "EditPostModal", "isVisible", "onDismiss", "onSave", "Lcom/rendly/app/ui/components/EditPostData;", "onDelete", "onPromote", "onViewStats", "EditSectionHeader", "title", "iconColor", "EditSectionHeader-mxwnekA", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;J)V", "ImageEditorContent", "initialImageColors", "onBack", "onSaveImages", "Lkotlin/Function2;", "PriceEditCard", "price", "", "originalPrice", "showOriginalPrice", "onPriceChange", "onOriginalPriceChange", "onShowOriginalPriceChange", "onOpenSettings", "(DLjava/lang/Double;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;)V", "PriceSettingsModal", "currentPrice", "images", "variantPrices", "Lcom/rendly/app/ui/components/VariantPriceData;", "imageColors", "Lkotlin/Function4;", "Lkotlin/ParameterName;", "name", "newPrice", "showOriginal", "(DLjava/lang/Double;ZLjava/util/List;Ljava/util/Map;Ljava/util/Map;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function4;)V", "QuickActionButton", "color", "modifier", "Landroidx/compose/ui/Modifier;", "onClick", "QuickActionButton-XO-JAsU", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;JLandroidx/compose/ui/Modifier;Lkotlin/jvm/functions/Function0;)V", "QuickActionsRow", "onStats", "onShare", "ToggleOption", "subtitle", "isEnabled", "onToggle", "ToggleOption-kKL39v8", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function1;J)V", "WarrantySelector", "selectedWarranty", "onWarrantySelected", "toMutableStateMap", "Landroidx/compose/runtime/snapshots/SnapshotStateMap;", "K", "V", "app_debug"})
public final class EditPostModalKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<kotlin.Pair<java.lang.String, androidx.compose.ui.graphics.Color>> availableProductColors = null;
    
    @androidx.compose.runtime.Composable
    public static final void EditPostModal(boolean isVisible, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.Post post, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.EditPostData, kotlin.Unit> onSave, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onPromote, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onViewStats) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EditPostHeader(com.rendly.app.data.model.Post post, java.util.List<java.lang.String> currentImages, kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function0<kotlin.Unit> onEditImages) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void QuickActionsRow(kotlin.jvm.functions.Function0<kotlin.Unit> onPromote, kotlin.jvm.functions.Function0<kotlin.Unit> onStats, kotlin.jvm.functions.Function0<kotlin.Unit> onShare) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EditPostField(java.lang.String label, java.lang.String value, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onValueChange, java.lang.String placeholder, androidx.compose.ui.graphics.vector.ImageVector icon, boolean multiline, int maxLines, java.lang.Integer maxChars) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PriceEditCard(double price, java.lang.Double originalPrice, boolean showOriginalPrice, kotlin.jvm.functions.Function1<? super java.lang.Double, kotlin.Unit> onPriceChange, kotlin.jvm.functions.Function1<? super java.lang.Double, kotlin.Unit> onOriginalPriceChange, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onShowOriginalPriceChange, kotlin.jvm.functions.Function0<kotlin.Unit> onOpenSettings) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AvailabilityCard(boolean isAvailable, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onAvailabilityChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void DeleteConfirmationContent(java.lang.String postTitle, java.lang.String postImage, kotlin.jvm.functions.Function0<kotlin.Unit> onCancel, kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PriceSettingsModal(double currentPrice, java.lang.Double originalPrice, boolean showOriginalPrice, java.util.List<java.lang.String> images, java.util.Map<java.lang.Integer, com.rendly.app.ui.components.VariantPriceData> variantPrices, java.util.Map<java.lang.Integer, java.lang.String> imageColors, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function4<? super java.lang.Double, ? super java.lang.Double, ? super java.lang.Boolean, ? super java.util.Map<java.lang.Integer, com.rendly.app.ui.components.VariantPriceData>, kotlin.Unit> onSave) {
    }
    
    private static final <K extends java.lang.Object, V extends java.lang.Object>androidx.compose.runtime.snapshots.SnapshotStateMap<K, V> toMutableStateMap(java.util.Map<K, ? extends V> $this$toMutableStateMap) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void CategorySelector(java.lang.String selectedCategory, java.util.List<kotlin.Pair<java.lang.String, androidx.compose.ui.graphics.vector.ImageVector>> categories, java.util.Map<java.lang.String, java.lang.String> categoryDisplayNames, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onCategorySelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void WarrantySelector(java.lang.String selectedWarranty, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onWarrantySelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ColorSelector(java.util.List<java.lang.String> selectedColors, kotlin.jvm.functions.Function1<? super java.util.List<java.lang.String>, kotlin.Unit> onColorsChanged) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ImageEditorContent(com.rendly.app.data.model.Post post, java.util.Map<java.lang.Integer, java.lang.String> initialImageColors, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function2<? super java.util.List<java.lang.String>, ? super java.util.Map<java.lang.Integer, java.lang.String>, kotlin.Unit> onSaveImages) {
    }
}