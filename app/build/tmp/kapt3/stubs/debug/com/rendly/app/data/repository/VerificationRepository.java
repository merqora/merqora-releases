package com.rendly.app.data.repository;

import android.util.Log;
import io.github.jan.supabase.postgrest.query.Order;
import io.github.jan.supabase.realtime.PostgresAction;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.Serializable;
import kotlinx.serialization.json.JsonObject;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.remote.SupabaseClient;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0003\'()B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0011\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0086@\u00a2\u0006\u0002\u0010\u0016J\b\u0010\u0017\u001a\u00020\u0004H\u0002J\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0018\u0010\u001a\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0018\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u001d\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0013J:\u0010\u001e\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u001f\u001a\u00020\u00042\u0006\u0010 \u001a\u00020\u00042\u0006\u0010!\u001a\u00020\u00042\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u0004H\u0086@\u00a2\u0006\u0002\u0010#J\u0016\u0010$\u001a\u00020%2\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0006\u0010&\u001a\u00020%R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/rendly/app/data/repository/VerificationRepository;", "", "()V", "TAG", "", "_otherUserVerified", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "otherUserVerified", "Lkotlinx/coroutines/flow/StateFlow;", "getOtherUserVerified", "()Lkotlinx/coroutines/flow/StateFlow;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "subscribedUserId", "verificationChannel", "Lio/github/jan/supabase/realtime/RealtimeChannel;", "cancelPendingRequest", "userId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAccountDataForVerification", "Lcom/rendly/app/data/repository/VerificationRepository$AccountDataForVerification;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCurrentTimestamp", "getLatestRequest", "Lcom/rendly/app/data/repository/VerificationRepository$VerificationRequest;", "getPendingRequest", "getVerificationStatus", "Lcom/rendly/app/data/repository/VerificationRepository$UserVerificationStatus;", "isUserVerified", "submitVerificationRequest", "verificationType", "fullLegalName", "reasonForVerification", "notablePresence", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "subscribeToVerification", "", "unsubscribeFromVerification", "AccountDataForVerification", "UserVerificationStatus", "VerificationRequest", "app_debug"})
public final class VerificationRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "VerificationRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _otherUserVerified = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> otherUserVerified = null;
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel verificationChannel;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String subscribedUserId;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.VerificationRepository INSTANCE = null;
    
    private VerificationRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getOtherUserVerified() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getVerificationStatus(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getPendingRequest(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.repository.VerificationRepository.VerificationRequest> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getLatestRequest(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.repository.VerificationRepository.VerificationRequest> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object submitVerificationRequest(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String verificationType, @org.jetbrains.annotations.NotNull
    java.lang.String fullLegalName, @org.jetbrains.annotations.NotNull
    java.lang.String reasonForVerification, @org.jetbrains.annotations.Nullable
    java.lang.String notablePresence, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object cancelPendingRequest(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getAccountDataForVerification(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.repository.VerificationRepository.AccountDataForVerification> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object isUserVerified(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.String getCurrentTimestamp() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object subscribeToVerification(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void unsubscribeFromVerification() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u001f\b\u0086\b\u0018\u00002\u00020\u0001B]\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u0012\u0006\u0010\t\u001a\u00020\u0007\u0012\u0006\u0010\n\u001a\u00020\u0007\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\u000fJ\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0007H\u00c6\u0003J\t\u0010#\u001a\u00020\u0007H\u00c6\u0003J\t\u0010$\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010%\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\rH\u00c6\u0003Ju\u0010\'\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\u00072\b\b\u0002\u0010\n\u001a\u00020\u00072\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010(\u001a\u00020\r2\b\u0010)\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010*\u001a\u00020\u0007H\u00d6\u0001J\t\u0010+\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0013R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0013R\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0011R\u0011\u0010\n\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0011R\u0011\u0010\u000e\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0011R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0013\u00a8\u0006,"}, d2 = {"Lcom/rendly/app/data/repository/VerificationRepository$AccountDataForVerification;", "", "username", "", "email", "phone", "followersCount", "", "clientsCount", "postsCount", "reputation", "memberSince", "hasStore", "", "salesCount", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;ZI)V", "getClientsCount", "()I", "getEmail", "()Ljava/lang/String;", "getFollowersCount", "getHasStore", "()Z", "getMemberSince", "getPhone", "getPostsCount", "getReputation", "getSalesCount", "getUsername", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
    public static final class AccountDataForVerification {
        @org.jetbrains.annotations.Nullable
        private final java.lang.String username = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String email = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String phone = null;
        private final int followersCount = 0;
        private final int clientsCount = 0;
        private final int postsCount = 0;
        private final int reputation = 0;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String memberSince = null;
        private final boolean hasStore = false;
        private final int salesCount = 0;
        
        public AccountDataForVerification(@org.jetbrains.annotations.Nullable
        java.lang.String username, @org.jetbrains.annotations.Nullable
        java.lang.String email, @org.jetbrains.annotations.Nullable
        java.lang.String phone, int followersCount, int clientsCount, int postsCount, int reputation, @org.jetbrains.annotations.Nullable
        java.lang.String memberSince, boolean hasStore, int salesCount) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getUsername() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getEmail() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getPhone() {
            return null;
        }
        
        public final int getFollowersCount() {
            return 0;
        }
        
        public final int getClientsCount() {
            return 0;
        }
        
        public final int getPostsCount() {
            return 0;
        }
        
        public final int getReputation() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getMemberSince() {
            return null;
        }
        
        public final boolean getHasStore() {
            return false;
        }
        
        public final int getSalesCount() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component1() {
            return null;
        }
        
        public final int component10() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component3() {
            return null;
        }
        
        public final int component4() {
            return 0;
        }
        
        public final int component5() {
            return 0;
        }
        
        public final int component6() {
            return 0;
        }
        
        public final int component7() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component8() {
            return null;
        }
        
        public final boolean component9() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.VerificationRepository.AccountDataForVerification copy(@org.jetbrains.annotations.Nullable
        java.lang.String username, @org.jetbrains.annotations.Nullable
        java.lang.String email, @org.jetbrains.annotations.Nullable
        java.lang.String phone, int followersCount, int clientsCount, int postsCount, int reputation, @org.jetbrains.annotations.Nullable
        java.lang.String memberSince, boolean hasStore, int salesCount) {
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
    
    @kotlinx.serialization.Serializable
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 \"2\u00020\u0001:\u0002!\"B5\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0007\u0012\b\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\u0002\u0010\u000bB\'\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010\u0013\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J+\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007H\u00c6\u0001J\u0013\u0010\u0015\u001a\u00020\u00052\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0018\u001a\u00020\u0007H\u00d6\u0001J&\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u00002\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u00c1\u0001\u00a2\u0006\u0002\b R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\rR\u0013\u0010\b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000f\u00a8\u0006#"}, d2 = {"Lcom/rendly/app/data/repository/VerificationRepository$UserVerificationStatus;", "", "seen1", "", "is_verified", "", "verified_at", "", "verification_type", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(IZLjava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(ZLjava/lang/String;Ljava/lang/String;)V", "()Z", "getVerification_type", "()Ljava/lang/String;", "getVerified_at", "component1", "component2", "component3", "copy", "equals", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
    public static final class UserVerificationStatus {
        private final boolean is_verified = false;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String verified_at = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String verification_type = null;
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus.Companion Companion = null;
        
        public UserVerificationStatus(boolean is_verified, @org.jetbrains.annotations.Nullable
        java.lang.String verified_at, @org.jetbrains.annotations.Nullable
        java.lang.String verification_type) {
            super();
        }
        
        public final boolean is_verified() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getVerified_at() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getVerification_type() {
            return null;
        }
        
        public UserVerificationStatus() {
            super();
        }
        
        public final boolean component1() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus copy(boolean is_verified, @org.jetbrains.annotations.Nullable
        java.lang.String verified_at, @org.jetbrains.annotations.Nullable
        java.lang.String verification_type) {
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
        
        @kotlin.jvm.JvmStatic
        public static final void write$Self$app_debug(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus self, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/repository/VerificationRepository.UserVerificationStatus.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/repository/VerificationRepository$UserVerificationStatus;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
        @java.lang.Deprecated
        public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus> {
            @org.jetbrains.annotations.NotNull
            public static final com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus.$serializer INSTANCE = null;
            
            private $serializer() {
                super();
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] childSerializers() {
                return null;
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus deserialize(@org.jetbrains.annotations.NotNull
            kotlinx.serialization.encoding.Decoder decoder) {
                return null;
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.descriptors.SerialDescriptor getDescriptor() {
                return null;
            }
            
            @java.lang.Override
            public void serialize(@org.jetbrains.annotations.NotNull
            kotlinx.serialization.encoding.Encoder encoder, @org.jetbrains.annotations.NotNull
            com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus value) {
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/repository/VerificationRepository$UserVerificationStatus$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/repository/VerificationRepository$UserVerificationStatus;", "app_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull
            public final kotlinx.serialization.KSerializer<com.rendly.app.data.repository.VerificationRepository.UserVerificationStatus> serializer() {
                return null;
            }
        }
    }
    
    @kotlinx.serialization.Serializable
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0013\n\u0002\u0018\u0002\n\u0002\b,\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 S2\u00020\u0001:\u0002RSB\u00d1\u0001\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\r\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u000f\u001a\u00020\u0003\u0012\u0006\u0010\u0010\u001a\u00020\u0003\u0012\u0006\u0010\u0011\u001a\u00020\u0003\u0012\b\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019\u00a2\u0006\u0002\u0010\u001aB\u00db\u0001\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\u0005\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u001bJ\u000b\u00101\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u00102\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\t\u00105\u001a\u00020\u0003H\u00c6\u0003J\u000b\u00106\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u00108\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u00109\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010<\u001a\u00020\u0005H\u00c6\u0003J\t\u0010=\u001a\u00020\u0005H\u00c6\u0003J\t\u0010>\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010?\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010@\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010A\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010B\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u00e1\u0001\u0010D\u001a\u00020\u00002\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u00032\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010E\u001a\u00020F2\b\u0010G\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010H\u001a\u00020\u0003H\u00d6\u0001J\t\u0010I\u001a\u00020\u0005H\u00d6\u0001J&\u0010J\u001a\u00020K2\u0006\u0010L\u001a\u00020\u00002\u0006\u0010M\u001a\u00020N2\u0006\u0010O\u001a\u00020PH\u00c1\u0001\u00a2\u0006\u0002\bQR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0013\u0010\r\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001dR\u0013\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001dR\u0013\u0010\n\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001dR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001dR\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010#R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001dR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001dR\u0013\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001dR\u0011\u0010\u0011\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010#R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001dR\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001dR\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001dR\u0013\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001dR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001dR\u0013\u0010\t\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001dR\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\u001dR\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\u001d\u00a8\u0006T"}, d2 = {"Lcom/rendly/app/data/repository/VerificationRepository$VerificationRequest;", "", "seen1", "", "id", "", "user_id", "verification_type", "status", "username", "display_name", "email", "avatar_url", "bio", "website_url", "followers_count", "following_count", "posts_count", "full_legal_name", "reason_for_verification", "notable_presence", "rejection_reason", "created_at", "updated_at", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAvatar_url", "()Ljava/lang/String;", "getBio", "getCreated_at", "getDisplay_name", "getEmail", "getFollowers_count", "()I", "getFollowing_count", "getFull_legal_name", "getId", "getNotable_presence", "getPosts_count", "getReason_for_verification", "getRejection_reason", "getStatus", "getUpdated_at", "getUser_id", "getUsername", "getVerification_type", "getWebsite_url", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
    public static final class VerificationRequest {
        @org.jetbrains.annotations.Nullable
        private final java.lang.String id = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String user_id = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String verification_type = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String status = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String username = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String display_name = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String email = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String avatar_url = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String bio = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String website_url = null;
        private final int followers_count = 0;
        private final int following_count = 0;
        private final int posts_count = 0;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String full_legal_name = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String reason_for_verification = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String notable_presence = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String rejection_reason = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String created_at = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String updated_at = null;
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.VerificationRepository.VerificationRequest.Companion Companion = null;
        
        public VerificationRequest(@org.jetbrains.annotations.Nullable
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String user_id, @org.jetbrains.annotations.NotNull
        java.lang.String verification_type, @org.jetbrains.annotations.NotNull
        java.lang.String status, @org.jetbrains.annotations.Nullable
        java.lang.String username, @org.jetbrains.annotations.Nullable
        java.lang.String display_name, @org.jetbrains.annotations.Nullable
        java.lang.String email, @org.jetbrains.annotations.Nullable
        java.lang.String avatar_url, @org.jetbrains.annotations.Nullable
        java.lang.String bio, @org.jetbrains.annotations.Nullable
        java.lang.String website_url, int followers_count, int following_count, int posts_count, @org.jetbrains.annotations.Nullable
        java.lang.String full_legal_name, @org.jetbrains.annotations.Nullable
        java.lang.String reason_for_verification, @org.jetbrains.annotations.Nullable
        java.lang.String notable_presence, @org.jetbrains.annotations.Nullable
        java.lang.String rejection_reason, @org.jetbrains.annotations.Nullable
        java.lang.String created_at, @org.jetbrains.annotations.Nullable
        java.lang.String updated_at) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getUser_id() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getVerification_type() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getStatus() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getUsername() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getDisplay_name() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getEmail() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getAvatar_url() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getBio() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getWebsite_url() {
            return null;
        }
        
        public final int getFollowers_count() {
            return 0;
        }
        
        public final int getFollowing_count() {
            return 0;
        }
        
        public final int getPosts_count() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getFull_legal_name() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getReason_for_verification() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getNotable_presence() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getRejection_reason() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getCreated_at() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getUpdated_at() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
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
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component14() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component15() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component16() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component17() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component18() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component19() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
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
        public final com.rendly.app.data.repository.VerificationRepository.VerificationRequest copy(@org.jetbrains.annotations.Nullable
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String user_id, @org.jetbrains.annotations.NotNull
        java.lang.String verification_type, @org.jetbrains.annotations.NotNull
        java.lang.String status, @org.jetbrains.annotations.Nullable
        java.lang.String username, @org.jetbrains.annotations.Nullable
        java.lang.String display_name, @org.jetbrains.annotations.Nullable
        java.lang.String email, @org.jetbrains.annotations.Nullable
        java.lang.String avatar_url, @org.jetbrains.annotations.Nullable
        java.lang.String bio, @org.jetbrains.annotations.Nullable
        java.lang.String website_url, int followers_count, int following_count, int posts_count, @org.jetbrains.annotations.Nullable
        java.lang.String full_legal_name, @org.jetbrains.annotations.Nullable
        java.lang.String reason_for_verification, @org.jetbrains.annotations.Nullable
        java.lang.String notable_presence, @org.jetbrains.annotations.Nullable
        java.lang.String rejection_reason, @org.jetbrains.annotations.Nullable
        java.lang.String created_at, @org.jetbrains.annotations.Nullable
        java.lang.String updated_at) {
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
        
        @kotlin.jvm.JvmStatic
        public static final void write$Self$app_debug(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.repository.VerificationRepository.VerificationRequest self, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/repository/VerificationRepository.VerificationRequest.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/repository/VerificationRepository$VerificationRequest;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
        @java.lang.Deprecated
        public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.repository.VerificationRepository.VerificationRequest> {
            @org.jetbrains.annotations.NotNull
            public static final com.rendly.app.data.repository.VerificationRepository.VerificationRequest.$serializer INSTANCE = null;
            
            private $serializer() {
                super();
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] childSerializers() {
                return null;
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public com.rendly.app.data.repository.VerificationRepository.VerificationRequest deserialize(@org.jetbrains.annotations.NotNull
            kotlinx.serialization.encoding.Decoder decoder) {
                return null;
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.descriptors.SerialDescriptor getDescriptor() {
                return null;
            }
            
            @java.lang.Override
            public void serialize(@org.jetbrains.annotations.NotNull
            kotlinx.serialization.encoding.Encoder encoder, @org.jetbrains.annotations.NotNull
            com.rendly.app.data.repository.VerificationRepository.VerificationRequest value) {
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/repository/VerificationRepository$VerificationRequest$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/repository/VerificationRepository$VerificationRequest;", "app_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull
            public final kotlinx.serialization.KSerializer<com.rendly.app.data.repository.VerificationRepository.VerificationRequest> serializer() {
                return null;
            }
        }
    }
}