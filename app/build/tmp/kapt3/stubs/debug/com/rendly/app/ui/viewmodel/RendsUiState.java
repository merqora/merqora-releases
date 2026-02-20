package com.rendly.app.ui.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import com.rendly.app.data.cache.CacheOrchestrator;
import com.rendly.app.data.cache.Screen;
import com.rendly.app.data.cache.core.CacheSource;
import com.rendly.app.data.cache.repository.CachedRendRepository;
import com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult;
import com.rendly.app.data.model.Rend;
import kotlinx.coroutines.flow.*;

/**
 * UI state for rends screen.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0003\u0004\u0005\u0006B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0004\u0007\b\t\n\u00a8\u0006\u000b"}, d2 = {"Lcom/rendly/app/ui/viewmodel/RendsUiState;", "", "()V", "Empty", "Error", "Loading", "Success", "Lcom/rendly/app/ui/viewmodel/RendsUiState$Empty;", "Lcom/rendly/app/ui/viewmodel/RendsUiState$Error;", "Lcom/rendly/app/ui/viewmodel/RendsUiState$Loading;", "Lcom/rendly/app/ui/viewmodel/RendsUiState$Success;", "app_debug"})
public abstract class RendsUiState {
    
    private RendsUiState() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0007\u001a\u00020\bH\u00d6\u0001J\t\u0010\t\u001a\u00020\nH\u00d6\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/rendly/app/ui/viewmodel/RendsUiState$Empty;", "Lcom/rendly/app/ui/viewmodel/RendsUiState;", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Empty extends com.rendly.app.ui.viewmodel.RendsUiState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.viewmodel.RendsUiState.Empty INSTANCE = null;
        
        private Empty() {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0010\b\u0002\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0007J\t\u0010\f\u001a\u00020\u0003H\u00c6\u0003J\u0011\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005H\u00c6\u0003J%\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u0010\b\u0002\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u00d6\u0003J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0003H\u00d6\u0001R\u0019\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/rendly/app/ui/viewmodel/RendsUiState$Error;", "Lcom/rendly/app/ui/viewmodel/RendsUiState;", "message", "", "cachedData", "", "Lcom/rendly/app/data/model/Rend;", "(Ljava/lang/String;Ljava/util/List;)V", "getCachedData", "()Ljava/util/List;", "getMessage", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class Error extends com.rendly.app.ui.viewmodel.RendsUiState {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String message = null;
        @org.jetbrains.annotations.Nullable
        private final java.util.List<com.rendly.app.data.model.Rend> cachedData = null;
        
        public Error(@org.jetbrains.annotations.NotNull
        java.lang.String message, @org.jetbrains.annotations.Nullable
        java.util.List<com.rendly.app.data.model.Rend> cachedData) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.util.List<com.rendly.app.data.model.Rend> getCachedData() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.util.List<com.rendly.app.data.model.Rend> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.ui.viewmodel.RendsUiState.Error copy(@org.jetbrains.annotations.NotNull
        java.lang.String message, @org.jetbrains.annotations.Nullable
        java.util.List<com.rendly.app.data.model.Rend> cachedData) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0007\u001a\u00020\bH\u00d6\u0001J\t\u0010\t\u001a\u00020\nH\u00d6\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/rendly/app/ui/viewmodel/RendsUiState$Loading;", "Lcom/rendly/app/ui/viewmodel/RendsUiState;", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Loading extends com.rendly.app.ui.viewmodel.RendsUiState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.viewmodel.RendsUiState.Loading INSTANCE = null;
        
        private Loading() {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0012\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BG\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\rJ\u000f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\tH\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\fH\u00c6\u0003JM\u0010\u001c\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u00062\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\u00062\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u00d6\u0003J\t\u0010 \u001a\u00020!H\u00d6\u0001J\t\u0010\"\u001a\u00020\fH\u00d6\u0001R\u0011\u0010\n\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u000fR\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u000fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006#"}, d2 = {"Lcom/rendly/app/ui/viewmodel/RendsUiState$Success;", "Lcom/rendly/app/ui/viewmodel/RendsUiState;", "rends", "", "Lcom/rendly/app/data/model/Rend;", "isRefreshing", "", "isStale", "source", "Lcom/rendly/app/data/cache/core/CacheSource;", "canLoadMore", "networkError", "", "(Ljava/util/List;ZZLcom/rendly/app/data/cache/core/CacheSource;ZLjava/lang/String;)V", "getCanLoadMore", "()Z", "getNetworkError", "()Ljava/lang/String;", "getRends", "()Ljava/util/List;", "getSource", "()Lcom/rendly/app/data/cache/core/CacheSource;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class Success extends com.rendly.app.ui.viewmodel.RendsUiState {
        @org.jetbrains.annotations.NotNull
        private final java.util.List<com.rendly.app.data.model.Rend> rends = null;
        private final boolean isRefreshing = false;
        private final boolean isStale = false;
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.cache.core.CacheSource source = null;
        private final boolean canLoadMore = false;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String networkError = null;
        
        public Success(@org.jetbrains.annotations.NotNull
        java.util.List<com.rendly.app.data.model.Rend> rends, boolean isRefreshing, boolean isStale, @org.jetbrains.annotations.NotNull
        com.rendly.app.data.cache.core.CacheSource source, boolean canLoadMore, @org.jetbrains.annotations.Nullable
        java.lang.String networkError) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.rendly.app.data.model.Rend> getRends() {
            return null;
        }
        
        public final boolean isRefreshing() {
            return false;
        }
        
        public final boolean isStale() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.cache.core.CacheSource getSource() {
            return null;
        }
        
        public final boolean getCanLoadMore() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getNetworkError() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.rendly.app.data.model.Rend> component1() {
            return null;
        }
        
        public final boolean component2() {
            return false;
        }
        
        public final boolean component3() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.cache.core.CacheSource component4() {
            return null;
        }
        
        public final boolean component5() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.ui.viewmodel.RendsUiState.Success copy(@org.jetbrains.annotations.NotNull
        java.util.List<com.rendly.app.data.model.Rend> rends, boolean isRefreshing, boolean isStale, @org.jetbrains.annotations.NotNull
        com.rendly.app.data.cache.core.CacheSource source, boolean canLoadMore, @org.jetbrains.annotations.Nullable
        java.lang.String networkError) {
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
}