package com.rendly.app.data.remote;

import android.graphics.Bitmap;
import android.util.Log;
import kotlinx.coroutines.Dispatchers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\n\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u000b\u001a\u00020\u0004JD\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\r2\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00042\u0014\b\u0002\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00140\u0012H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0017"}, d2 = {"Lcom/rendly/app/data/remote/CloudinaryService;", "", "()V", "CLOUD_NAME", "", "TAG", "UPLOAD_PRESET", "UPLOAD_URL", "client", "Lokhttp3/OkHttpClient;", "extractPublicIdFromUrl", "url", "uploadImage", "Lkotlin/Result;", "bitmap", "Landroid/graphics/Bitmap;", "folder", "onProgress", "Lkotlin/Function1;", "", "", "uploadImage-BWLJW6A", "(Landroid/graphics/Bitmap;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class CloudinaryService {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "CloudinaryService";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String CLOUD_NAME = "dz0clge3s";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String UPLOAD_PRESET = "Merqora_unsigned";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String UPLOAD_URL = "https://api.cloudinary.com/v1_1/dz0clge3s/image/upload";
    @org.jetbrains.annotations.NotNull
    private static final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.remote.CloudinaryService INSTANCE = null;
    
    private CloudinaryService() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String extractPublicIdFromUrl(@org.jetbrains.annotations.NotNull
    java.lang.String url) {
        return null;
    }
}