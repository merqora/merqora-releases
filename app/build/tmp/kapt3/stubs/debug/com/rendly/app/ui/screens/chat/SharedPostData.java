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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0017\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BC\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\u0006\u0010\n\u001a\u00020\u0003\u0012\u0006\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003JU\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\"\u001a\u00020#H\u00d6\u0001J\t\u0010$\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u000e\u00a8\u0006%"}, d2 = {"Lcom/rendly/app/ui/screens/chat/SharedPostData;", "", "postId", "", "images", "", "title", "price", "", "ownerUsername", "ownerAvatar", "customMessage", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;DLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getCustomMessage", "()Ljava/lang/String;", "getImages", "()Ljava/util/List;", "getOwnerAvatar", "getOwnerUsername", "getPostId", "getPrice", "()D", "getTitle", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class SharedPostData {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String postId = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> images = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String title = null;
    private final double price = 0.0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String ownerUsername = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String ownerAvatar = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String customMessage = null;
    
    public SharedPostData(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, @org.jetbrains.annotations.NotNull
    java.lang.String title, double price, @org.jetbrains.annotations.NotNull
    java.lang.String ownerUsername, @org.jetbrains.annotations.NotNull
    java.lang.String ownerAvatar, @org.jetbrains.annotations.NotNull
    java.lang.String customMessage) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPostId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getImages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTitle() {
        return null;
    }
    
    public final double getPrice() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getOwnerUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getOwnerAvatar() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCustomMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    public final double component4() {
        return 0.0;
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
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.screens.chat.SharedPostData copy(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, @org.jetbrains.annotations.NotNull
    java.lang.String title, double price, @org.jetbrains.annotations.NotNull
    java.lang.String ownerUsername, @org.jetbrains.annotations.NotNull
    java.lang.String ownerAvatar, @org.jetbrains.annotations.NotNull
    java.lang.String customMessage) {
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