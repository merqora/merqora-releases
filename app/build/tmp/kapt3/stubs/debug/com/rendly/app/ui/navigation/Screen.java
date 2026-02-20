package com.rendly.app.ui.navigation;

import androidx.compose.animation.*;
import androidx.compose.runtime.Composable;
import androidx.navigation.NavHostController;
import androidx.navigation.NavType;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\n\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010B\u000f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u0082\u0001\n\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u00a8\u0006\u001b"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen;", "", "route", "", "(Ljava/lang/String;)V", "getRoute", "()Ljava/lang/String;", "Checkout", "Home", "LiveStreams", "LiveViewer", "Login", "Messages", "Notifications", "ProductDetail", "Profile", "Register", "Lcom/rendly/app/ui/navigation/Screen$Checkout;", "Lcom/rendly/app/ui/navigation/Screen$Home;", "Lcom/rendly/app/ui/navigation/Screen$LiveStreams;", "Lcom/rendly/app/ui/navigation/Screen$LiveViewer;", "Lcom/rendly/app/ui/navigation/Screen$Login;", "Lcom/rendly/app/ui/navigation/Screen$Messages;", "Lcom/rendly/app/ui/navigation/Screen$Notifications;", "Lcom/rendly/app/ui/navigation/Screen$ProductDetail;", "Lcom/rendly/app/ui/navigation/Screen$Profile;", "Lcom/rendly/app/ui/navigation/Screen$Register;", "app_debug"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String route = null;
    
    private Screen(java.lang.String route) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getRoute() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$Checkout;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Checkout extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.Checkout INSTANCE = null;
        
        private Checkout() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$Home;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Home extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.Home INSTANCE = null;
        
        private Home() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$LiveStreams;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class LiveStreams extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.LiveStreams INSTANCE = null;
        
        private LiveStreams() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$LiveViewer;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "createRoute", "", "streamId", "app_debug"})
    public static final class LiveViewer extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.LiveViewer INSTANCE = null;
        
        private LiveViewer() {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull
        java.lang.String streamId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$Login;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Login extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.Login INSTANCE = null;
        
        private Login() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$Messages;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Messages extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.Messages INSTANCE = null;
        
        private Messages() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$Notifications;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Notifications extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.Notifications INSTANCE = null;
        
        private Notifications() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$ProductDetail;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "createRoute", "", "postId", "app_debug"})
    public static final class ProductDetail extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.ProductDetail INSTANCE = null;
        
        private ProductDetail() {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull
        java.lang.String postId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$Profile;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "createRoute", "", "userId", "app_debug"})
    public static final class Profile extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.Profile INSTANCE = null;
        
        private Profile() {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull
        java.lang.String userId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/navigation/Screen$Register;", "Lcom/rendly/app/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Register extends com.rendly.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.navigation.Screen.Register INSTANCE = null;
        
        private Register() {
        }
    }
}