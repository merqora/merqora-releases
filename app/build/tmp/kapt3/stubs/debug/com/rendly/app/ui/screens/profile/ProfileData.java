package com.rendly.app.ui.screens.profile;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Highlight;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.data.repository.HighlightRepository;
import com.rendly.app.ui.components.HighlightedStories;
import com.rendly.app.ui.components.HighlightedStory;
import com.rendly.app.ui.components.HighlightCategory;
import com.rendly.app.ui.components.HighlightFrameStyle;
import com.rendly.app.ui.components.HighlightFrameColor;
import com.rendly.app.ui.components.HighlightBackgroundColor;
import com.rendly.app.ui.components.ProfileStatsBadges;
import com.rendly.app.ui.components.SellerTrustIndicator;
import com.rendly.app.ui.components.AddHighlightModal;
import com.rendly.app.ui.components.StoriesViewer;
import com.rendly.app.ui.components.Story;
import com.rendly.app.ui.components.UserStories;
import com.rendly.app.ui.components.HighlightOptionsModal;
import com.rendly.app.ui.components.PublishOptionsModal;
import com.rendly.app.ui.components.EditPostModal;
import com.rendly.app.ui.screens.publish.PublishScreen;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.model.Rend;
import com.rendly.app.data.repository.PostRepository;
import com.rendly.app.data.repository.RendRepository;
import com.rendly.app.data.repository.StoryRepository;
import com.rendly.app.ui.components.ProfileScreenSkeleton;
import com.rendly.app.ui.components.ProfileGridSkeleton;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b-\b\u0086\b\u0018\u00002\u00020\u0001B\u00bf\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0013\u00a2\u0006\u0002\u0010\u0016J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010,\u001a\u00020\u000eH\u00c6\u0003J\t\u0010-\u001a\u00020\u000eH\u00c6\u0003J\t\u0010.\u001a\u00020\u000eH\u00c6\u0003J\t\u0010/\u001a\u00020\u000eH\u00c6\u0003J\t\u00100\u001a\u00020\u0013H\u00c6\u0003J\t\u00101\u001a\u00020\u0003H\u00c6\u0003J\t\u00102\u001a\u00020\u0013H\u00c6\u0003J\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\u000b\u00104\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00105\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00106\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00108\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00109\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u00c3\u0001\u0010;\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u000e2\b\b\u0002\u0010\u0011\u001a\u00020\u000e2\b\b\u0002\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u0013H\u00c6\u0001J\u0013\u0010<\u001a\u00020\u00132\b\u0010=\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010>\u001a\u00020\u000eH\u00d6\u0001J\t\u0010?\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0018R\u0011\u0010\u0010\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0018R\u0011\u0010\u0015\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u001dR\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0018R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0018R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0018R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001bR\u0011\u0010\u0011\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001bR\u0011\u0010\u000f\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001bR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u0018R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0018R\u0011\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001dR\u0013\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0018R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u0018R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u0018\u00a8\u0006@"}, d2 = {"Lcom/rendly/app/ui/screens/profile/ProfileData;", "", "userId", "", "username", "nombre", "nombreTienda", "descripcion", "avatarUrl", "bannerUrl", "ubicacion", "telefono", "sexo", "publicaciones", "", "seguidores", "clientes", "reputacion", "tieneTienda", "", "miembroDesde", "isVerified", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIIZLjava/lang/String;Z)V", "getAvatarUrl", "()Ljava/lang/String;", "getBannerUrl", "getClientes", "()I", "getDescripcion", "()Z", "getMiembroDesde", "getNombre", "getNombreTienda", "getPublicaciones", "getReputacion", "getSeguidores", "getSexo", "getTelefono", "getTieneTienda", "getUbicacion", "getUserId", "getUsername", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class ProfileData {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String username = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String nombre = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String nombreTienda = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String descripcion = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String avatarUrl = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String bannerUrl = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String ubicacion = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String telefono = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String sexo = null;
    private final int publicaciones = 0;
    private final int seguidores = 0;
    private final int clientes = 0;
    private final int reputacion = 0;
    private final boolean tieneTienda = false;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String miembroDesde = null;
    private final boolean isVerified = false;
    
    public ProfileData(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.Nullable
    java.lang.String nombre, @org.jetbrains.annotations.Nullable
    java.lang.String nombreTienda, @org.jetbrains.annotations.Nullable
    java.lang.String descripcion, @org.jetbrains.annotations.Nullable
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable
    java.lang.String bannerUrl, @org.jetbrains.annotations.Nullable
    java.lang.String ubicacion, @org.jetbrains.annotations.Nullable
    java.lang.String telefono, @org.jetbrains.annotations.Nullable
    java.lang.String sexo, int publicaciones, int seguidores, int clientes, int reputacion, boolean tieneTienda, @org.jetbrains.annotations.NotNull
    java.lang.String miembroDesde, boolean isVerified) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNombre() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNombreTienda() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDescripcion() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getAvatarUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getBannerUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUbicacion() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getTelefono() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getSexo() {
        return null;
    }
    
    public final int getPublicaciones() {
        return 0;
    }
    
    public final int getSeguidores() {
        return 0;
    }
    
    public final int getClientes() {
        return 0;
    }
    
    public final int getReputacion() {
        return 0;
    }
    
    public final boolean getTieneTienda() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getMiembroDesde() {
        return null;
    }
    
    public final boolean isVerified() {
        return false;
    }
    
    public ProfileData() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component10() {
        return null;
    }
    
    public final int component11() {
        return 0;
    }
    
    public final int component12() {
        return 0;
    }
    
    public final int component13() {
        return 0;
    }
    
    public final int component14() {
        return 0;
    }
    
    public final boolean component15() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component16() {
        return null;
    }
    
    public final boolean component17() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.screens.profile.ProfileData copy(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.Nullable
    java.lang.String nombre, @org.jetbrains.annotations.Nullable
    java.lang.String nombreTienda, @org.jetbrains.annotations.Nullable
    java.lang.String descripcion, @org.jetbrains.annotations.Nullable
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable
    java.lang.String bannerUrl, @org.jetbrains.annotations.Nullable
    java.lang.String ubicacion, @org.jetbrains.annotations.Nullable
    java.lang.String telefono, @org.jetbrains.annotations.Nullable
    java.lang.String sexo, int publicaciones, int seguidores, int clientes, int reputacion, boolean tieneTienda, @org.jetbrains.annotations.NotNull
    java.lang.String miembroDesde, boolean isVerified) {
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