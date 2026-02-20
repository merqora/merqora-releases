package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rendly/app/data/repository/FollowType;", "", "(Ljava/lang/String;I)V", "NONE", "FOLLOWER", "CLIENT_PENDING", "CLIENT", "app_debug"})
public enum FollowType {
    /*public static final*/ NONE /* = new NONE() */,
    /*public static final*/ FOLLOWER /* = new FOLLOWER() */,
    /*public static final*/ CLIENT_PENDING /* = new CLIENT_PENDING() */,
    /*public static final*/ CLIENT /* = new CLIENT() */;
    
    FollowType() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.repository.FollowType> getEntries() {
        return null;
    }
}