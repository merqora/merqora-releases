package com.rendly.app.data.remote;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import com.rendly.app.BuildConfig;
import kotlinx.coroutines.Dispatchers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002JD\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00042\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u0013H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0016\u0010\u0017JD\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00040\u000e2\u0006\u0010\u0019\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00042\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u0013H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001a\u0010\u0017JL\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00040\u000e2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010\u0011\u001a\u00020\u00042\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u0013H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b \u0010!JD\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00040\u000e2\u0006\u0010#\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00042\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u0013H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b$\u0010\u0017R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006%"}, d2 = {"Lcom/rendly/app/data/remote/ImageKitService;", "", "()V", "PRIVATE_KEY", "", "PUBLIC_KEY", "TAG", "UPLOAD_URL", "URL_ENDPOINT", "getURL_ENDPOINT", "()Ljava/lang/String;", "client", "Lokhttp3/OkHttpClient;", "uploadAudioBytes", "Lkotlin/Result;", "audioBytes", "", "folder", "onProgress", "Lkotlin/Function1;", "", "", "uploadAudioBytes-BWLJW6A", "([BLjava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadImageBytes", "imageBytes", "uploadImageBytes-BWLJW6A", "uploadVideo", "context", "Landroid/content/Context;", "videoUri", "Landroid/net/Uri;", "uploadVideo-yxL6bBk", "(Landroid/content/Context;Landroid/net/Uri;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadVideoBytes", "videoBytes", "uploadVideoBytes-BWLJW6A", "app_debug"})
public final class ImageKitService {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ImageKitService";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PUBLIC_KEY = "public_kk09G2vc9jjsVDVRz6D35/YavwE=";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PRIVATE_KEY = "private_jB3PhowAiQtz/Uyq8OuQX7itPTs=";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String URL_ENDPOINT = "https://ik.imagekit.io/4z6ezuoeb";
    @org.jetbrains.annotations.NotNull
    private static final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.remote.ImageKitService INSTANCE = null;
    
    private ImageKitService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getURL_ENDPOINT() {
        return null;
    }
}