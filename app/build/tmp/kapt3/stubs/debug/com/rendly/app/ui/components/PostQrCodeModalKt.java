package com.rendly.app.ui.components;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rendly.app.data.model.Post;
import com.rendly.app.ui.theme.*;
import java.io.OutputStream;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a(\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a\u001c\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0004\u001a\u00020\u00052\b\u0010\n\u001a\u0004\u0018\u00010\tH\u0002\u001a\u001c\u0010\u000b\u001a\u0004\u0018\u00010\t2\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0002\u001a \u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\rH\u0002\u00a8\u0006\u0015"}, d2 = {"PostQrCodeModal", "", "isVisible", "", "post", "Lcom/rendly/app/data/model/Post;", "onDismiss", "Lkotlin/Function0;", "generateQrCardBitmap", "Landroid/graphics/Bitmap;", "qrBitmap", "generateQrCode", "content", "", "size", "", "saveQrToGallery", "context", "Landroid/content/Context;", "bitmap", "fileName", "app_debug"})
public final class PostQrCodeModalKt {
    
    @androidx.compose.runtime.Composable
    public static final void PostQrCodeModal(boolean isVisible, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.Post post, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    private static final android.graphics.Bitmap generateQrCode(java.lang.String content, int size) {
        return null;
    }
    
    private static final android.graphics.Bitmap generateQrCardBitmap(com.rendly.app.data.model.Post post, android.graphics.Bitmap qrBitmap) {
        return null;
    }
    
    private static final boolean saveQrToGallery(android.content.Context context, android.graphics.Bitmap bitmap, java.lang.String fileName) {
        return false;
    }
}