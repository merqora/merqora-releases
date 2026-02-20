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

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001a\n\u0000\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\"\u0015\u0010\u0000\u001a\u00020\u0001*\u00020\u00028F\u00a2\u0006\u0006\u001a\u0004\b\u0003\u0010\u0004\"\u001b\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006*\u00020\u00028F\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\t\u00a8\u0006\n"}, d2 = {"hasData", "", "Lcom/rendly/app/ui/viewmodel/RendsUiState;", "getHasData", "(Lcom/rendly/app/ui/viewmodel/RendsUiState;)Z", "rendsList", "", "Lcom/rendly/app/data/model/Rend;", "getRendsList", "(Lcom/rendly/app/ui/viewmodel/RendsUiState;)Ljava/util/List;", "app_debug"})
public final class CachedRendViewModelKt {
    
    public static final boolean getHasData(@org.jetbrains.annotations.NotNull
    com.rendly.app.ui.viewmodel.RendsUiState $this$hasData) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.data.model.Rend> getRendsList(@org.jetbrains.annotations.NotNull
    com.rendly.app.ui.viewmodel.RendsUiState $this$rendsList) {
        return null;
    }
}