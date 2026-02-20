package com.rendly.app.ui.components;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.rendly.app.data.repository.ChatRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rendly/app/ui/components/ClientRequestType;", "", "(Ljava/lang/String;I)V", "REQUEST", "ACCEPTED", "REJECTED", "PENDING", "app_debug"})
public enum ClientRequestType {
    /*public static final*/ REQUEST /* = new REQUEST() */,
    /*public static final*/ ACCEPTED /* = new ACCEPTED() */,
    /*public static final*/ REJECTED /* = new REJECTED() */,
    /*public static final*/ PENDING /* = new PENDING() */;
    
    ClientRequestType() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.components.ClientRequestType> getEntries() {
        return null;
    }
}