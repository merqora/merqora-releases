package com.rendly.app.ui.screens.chat;

import androidx.compose.animation.*;
import androidx.compose.ui.unit.Dp;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.ui.input.pointer.PointerEventPass;
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
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.core.content.ContextCompat;
import java.io.File;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.repository.ChatRepository;
import com.rendly.app.data.repository.Message;
import com.rendly.app.data.repository.MessageStatus;
import com.rendly.app.data.repository.FollowersRepository;
import com.rendly.app.data.repository.HandshakeRepository;
import com.rendly.app.data.repository.ReputationRepository;
import com.rendly.app.data.repository.VerificationRepository;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.data.repository.PostRepository;
import com.rendly.app.data.model.*;
import com.rendly.app.ui.components.ClientRequestMessageBubble;
import com.rendly.app.ui.components.VerifiedBadge;
import com.rendly.app.ui.components.HandshakeProposalModal;
import com.rendly.app.ui.components.HandshakeActiveBanner;
import com.rendly.app.ui.components.HandshakeBannerState;
import com.rendly.app.ui.components.CancelConfirmationModal;
import com.rendly.app.R;
import androidx.compose.ui.window.DialogProperties;
import com.rendly.app.ui.theme.*;
import androidx.compose.animation.core.*;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.foundation.lazy.grid.GridCells;
import com.rendly.app.data.repository.CallRepository;
import com.rendly.app.data.model.CallStatus;
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.ui.text.input.ImeAction;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003JE\u0010\u0019\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\fR\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\f\u00a8\u0006 "}, d2 = {"Lcom/rendly/app/ui/screens/chat/ConsultPostData;", "", "postId", "", "productTitle", "productPrice", "", "productImage", "message", "type", "(Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getMessage", "()Ljava/lang/String;", "getPostId", "getProductImage", "getProductPrice", "()D", "getProductTitle", "getType", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class ConsultPostData {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String postId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String productTitle = null;
    private final double productPrice = 0.0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String productImage = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String message = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String type = null;
    
    public ConsultPostData(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    java.lang.String productTitle, double productPrice, @org.jetbrains.annotations.NotNull
    java.lang.String productImage, @org.jetbrains.annotations.NotNull
    java.lang.String message, @org.jetbrains.annotations.NotNull
    java.lang.String type) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPostId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getProductTitle() {
        return null;
    }
    
    public final double getProductPrice() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getProductImage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    public final double component3() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.screens.chat.ConsultPostData copy(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    java.lang.String productTitle, double productPrice, @org.jetbrains.annotations.NotNull
    java.lang.String productImage, @org.jetbrains.annotations.NotNull
    java.lang.String message, @org.jetbrains.annotations.NotNull
    java.lang.String type) {
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