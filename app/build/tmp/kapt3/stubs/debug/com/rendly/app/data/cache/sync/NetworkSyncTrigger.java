package com.rendly.app.data.cache.sync;

import android.content.Context;
import android.util.Log;
import androidx.work.*;
import com.rendly.app.data.cache.db.PendingOperationEntity;
import com.rendly.app.data.cache.db.MerqoraDatabase;
import com.rendly.app.data.cache.repository.CachedRendRepository;
import kotlinx.coroutines.Dispatchers;
import java.util.concurrent.TimeUnit;

/**
 * Network connectivity observer that triggers sync on reconnection.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/rendly/app/data/cache/sync/NetworkSyncTrigger;", "", "()V", "TAG", "", "onNetworkAvailable", "", "context", "Landroid/content/Context;", "app_debug"})
public final class NetworkSyncTrigger {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "NetworkSyncTrigger";
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.cache.sync.NetworkSyncTrigger INSTANCE = null;
    
    private NetworkSyncTrigger() {
        super();
    }
    
    public final void onNetworkAvailable(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
}