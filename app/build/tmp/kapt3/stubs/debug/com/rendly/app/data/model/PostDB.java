package com.rendly.app.data.model;

import androidx.compose.runtime.Immutable;
import androidx.compose.runtime.Stable;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b_\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 \u008e\u00012\u00020\u0001:\u0004\u008d\u0001\u008e\u0001B\u00e1\u0002\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\t\u001a\u00020\n\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\u000e\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e\u0012\u000e\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e\u0012\b\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u0011\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0012\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0013\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0015\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u0018\u001a\u00020\u0019\u0012\n\b\u0001\u0010\u001a\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u001c\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u001d\u001a\u0004\u0018\u00010\u0019\u0012\n\b\u0001\u0010\u001e\u001a\u0004\u0018\u00010\u0019\u0012\n\b\u0001\u0010\u001f\u001a\u0004\u0018\u00010\n\u0012\b\u0010 \u001a\u0004\u0018\u00010\u0003\u0012\b\u0010!\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\"\u001a\u0004\u0018\u00010\u0019\u0012\u000e\u0010#\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e\u0012\n\b\u0001\u0010$\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010%\u001a\u0004\u0018\u00010&\u00a2\u0006\u0002\u0010\'B\u00d5\u0002\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0002\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e\u0012\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000e\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0019\u0012\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u0019\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u0019\u0012\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u0019\u0012\u0010\b\u0002\u0010#\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010(J\t\u0010b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010c\u001a\u00020\u0005H\u00c6\u0003J\t\u0010d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010g\u001a\u00020\u0003H\u00c6\u0003J\t\u0010h\u001a\u00020\u0003H\u00c6\u0003J\t\u0010i\u001a\u00020\u0005H\u00c6\u0003J\t\u0010j\u001a\u00020\u0005H\u00c6\u0003J\t\u0010k\u001a\u00020\u0019H\u00c6\u0003J\u000b\u0010l\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010m\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010n\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u00105J\u000b\u0010o\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010p\u001a\u0004\u0018\u00010\u0019H\u00c6\u0003\u00a2\u0006\u0002\u0010,J\u0010\u0010q\u001a\u0004\u0018\u00010\u0019H\u00c6\u0003\u00a2\u0006\u0002\u0010,J\u0010\u0010r\u001a\u0004\u0018\u00010\nH\u00c6\u0003\u00a2\u0006\u0002\u0010IJ\u0010\u0010s\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u00105J\u000b\u0010t\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010u\u001a\u0004\u0018\u00010\u0019H\u00c6\u0003\u00a2\u0006\u0002\u0010,J\u0011\u0010v\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000eH\u00c6\u0003J\u000b\u0010w\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010x\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010y\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010z\u001a\u00020\nH\u00c6\u0003J\u000b\u0010{\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010|\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0011\u0010}\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000eH\u00c6\u0003J\u000f\u0010~\u001a\b\u0012\u0004\u0012\u00020\u00050\u000eH\u00c6\u0003J\u00e1\u0002\u0010\u007f\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\t\u001a\u00020\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00052\u0010\b\u0002\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u00052\b\b\u0002\u0010\u0011\u001a\u00020\u00032\b\b\u0002\u0010\u0012\u001a\u00020\u00032\b\b\u0002\u0010\u0013\u001a\u00020\u00032\b\b\u0002\u0010\u0014\u001a\u00020\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u00052\b\b\u0002\u0010\u0017\u001a\u00020\u00052\b\b\u0002\u0010\u0018\u001a\u00020\u00192\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u00192\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u00192\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u00192\u0010\b\u0002\u0010#\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001\u00a2\u0006\u0003\u0010\u0080\u0001J\u0015\u0010\u0081\u0001\u001a\u00020\u00192\t\u0010\u0082\u0001\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\n\u0010\u0083\u0001\u001a\u00020\u0003H\u00d6\u0001J\n\u0010\u0084\u0001\u001a\u00020\u0005H\u00d6\u0001J.\u0010\u0085\u0001\u001a\u00030\u0086\u00012\u0007\u0010\u0087\u0001\u001a\u00020\u00002\b\u0010\u0088\u0001\u001a\u00030\u0089\u00012\b\u0010\u008a\u0001\u001a\u00030\u008b\u0001H\u00c1\u0001\u00a2\u0006\u0003\b\u008c\u0001R \u0010\u001d\u001a\u0004\u0018\u00010\u00198\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010-\u0012\u0004\b)\u0010*\u001a\u0004\b+\u0010,R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010/R\u0019\u0010#\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010/R \u0010\u001b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u00106\u0012\u0004\b3\u0010*\u001a\u0004\b4\u00105R\u001e\u0010\u001a\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b7\u0010*\u001a\u0004\b8\u0010/R\u001e\u0010\u001c\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b9\u0010*\u001a\u0004\b:\u0010/R\u001c\u0010\u0016\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b;\u0010*\u001a\u0004\b<\u0010/R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010/R \u0010\u001e\u001a\u0004\u0018\u00010\u00198\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010-\u0012\u0004\b>\u0010*\u001a\u0004\b?\u0010,R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010/R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u00101R\u001c\u0010\u0018\u001a\u00020\u00198\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bB\u0010*\u001a\u0004\b\u0018\u0010CR\u001c\u0010\u0011\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bD\u0010*\u001a\u0004\bE\u0010FR \u0010\u001f\u001a\u0004\u0018\u00010\n8\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010J\u0012\u0004\bG\u0010*\u001a\u0004\bH\u0010IR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010LR\u001e\u0010$\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bM\u0010*\u001a\u0004\bN\u0010/R \u0010\"\u001a\u0004\u0018\u00010\u00198\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010-\u0012\u0004\bO\u0010*\u001a\u0004\bP\u0010,R\u001c\u0010\u0012\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bQ\u0010*\u001a\u0004\bR\u0010FR\u001c\u0010\u0015\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bS\u0010*\u001a\u0004\bT\u0010FR\u001c\u0010\u0014\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bU\u0010*\u001a\u0004\bV\u0010FR\u0011\u0010\u0010\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bW\u0010/R\u0015\u0010 \u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\bX\u00105R\u0019\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\bY\u00101R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bZ\u0010/R\u001c\u0010\u0017\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b[\u0010*\u001a\u0004\b\\\u0010/R\u001c\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b]\u0010*\u001a\u0004\b^\u0010/R\u001c\u0010\u0013\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b_\u0010*\u001a\u0004\b`\u0010FR\u0013\u0010!\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\ba\u0010/\u00a8\u0006\u008f\u0001"}, d2 = {"Lcom/rendly/app/data/model/PostDB;", "", "seen1", "", "id", "", "userId", "title", "description", "price", "", "category", "condition", "tags", "", "images", "status", "likesCount", "reviewsCount", "viewsCount", "sharesCount", "savesCount", "createdAt", "updatedAt", "isCollection", "", "coverType", "coverImageIndex", "coverUrl", "allowOffers", "freeShipping", "previousPrice", "stock", "warranty", "returnsAccepted", "colors", "productId", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/lang/String;IIIIILjava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/util/List;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/lang/String;IIIIILjava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/util/List;Ljava/lang/String;)V", "getAllowOffers$annotations", "()V", "getAllowOffers", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getCategory", "()Ljava/lang/String;", "getColors", "()Ljava/util/List;", "getCondition", "getCoverImageIndex$annotations", "getCoverImageIndex", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getCoverType$annotations", "getCoverType", "getCoverUrl$annotations", "getCoverUrl", "getCreatedAt$annotations", "getCreatedAt", "getDescription", "getFreeShipping$annotations", "getFreeShipping", "getId", "getImages", "isCollection$annotations", "()Z", "getLikesCount$annotations", "getLikesCount", "()I", "getPreviousPrice$annotations", "getPreviousPrice", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getPrice", "()D", "getProductId$annotations", "getProductId", "getReturnsAccepted$annotations", "getReturnsAccepted", "getReviewsCount$annotations", "getReviewsCount", "getSavesCount$annotations", "getSavesCount", "getSharesCount$annotations", "getSharesCount", "getStatus", "getStock", "getTags", "getTitle", "getUpdatedAt$annotations", "getUpdatedAt", "getUserId$annotations", "getUserId", "getViewsCount$annotations", "getViewsCount", "getWarranty", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/lang/String;IIIIILjava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/util/List;Ljava/lang/String;)Lcom/rendly/app/data/model/PostDB;", "equals", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
public final class PostDB {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String title = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String description = null;
    private final double price = 0.0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String category = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String condition = null;
    @org.jetbrains.annotations.Nullable
    private final java.util.List<java.lang.String> tags = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> images = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String status = null;
    private final int likesCount = 0;
    private final int reviewsCount = 0;
    private final int viewsCount = 0;
    private final int sharesCount = 0;
    private final int savesCount = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String updatedAt = null;
    private final boolean isCollection = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String coverType = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer coverImageIndex = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String coverUrl = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Boolean allowOffers = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Boolean freeShipping = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double previousPrice = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer stock = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String warranty = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Boolean returnsAccepted = null;
    @org.jetbrains.annotations.Nullable
    private final java.util.List<java.lang.String> colors = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String productId = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.PostDB.Companion Companion = null;
    
    public PostDB(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.String description, double price, @org.jetbrains.annotations.Nullable
    java.lang.String category, @org.jetbrains.annotations.Nullable
    java.lang.String condition, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> tags, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, @org.jetbrains.annotations.NotNull
    java.lang.String status, int likesCount, int reviewsCount, int viewsCount, int sharesCount, int savesCount, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.NotNull
    java.lang.String updatedAt, boolean isCollection, @org.jetbrains.annotations.Nullable
    java.lang.String coverType, @org.jetbrains.annotations.Nullable
    java.lang.Integer coverImageIndex, @org.jetbrains.annotations.Nullable
    java.lang.String coverUrl, @org.jetbrains.annotations.Nullable
    java.lang.Boolean allowOffers, @org.jetbrains.annotations.Nullable
    java.lang.Boolean freeShipping, @org.jetbrains.annotations.Nullable
    java.lang.Double previousPrice, @org.jetbrains.annotations.Nullable
    java.lang.Integer stock, @org.jetbrains.annotations.Nullable
    java.lang.String warranty, @org.jetbrains.annotations.Nullable
    java.lang.Boolean returnsAccepted, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> colors, @org.jetbrains.annotations.Nullable
    java.lang.String productId) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "user_id")
    @java.lang.Deprecated
    public static void getUserId$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDescription() {
        return null;
    }
    
    public final double getPrice() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCategory() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCondition() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> getTags() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getImages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStatus() {
        return null;
    }
    
    public final int getLikesCount() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "likes_count")
    @java.lang.Deprecated
    public static void getLikesCount$annotations() {
    }
    
    public final int getReviewsCount() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "reviews_count")
    @java.lang.Deprecated
    public static void getReviewsCount$annotations() {
    }
    
    public final int getViewsCount() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "views_count")
    @java.lang.Deprecated
    public static void getViewsCount$annotations() {
    }
    
    public final int getSharesCount() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "shares_count")
    @java.lang.Deprecated
    public static void getSharesCount$annotations() {
    }
    
    public final int getSavesCount() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "saves_count")
    @java.lang.Deprecated
    public static void getSavesCount$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCreatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "created_at")
    @java.lang.Deprecated
    public static void getCreatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUpdatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "updated_at")
    @java.lang.Deprecated
    public static void getUpdatedAt$annotations() {
    }
    
    public final boolean isCollection() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "is_collection")
    @java.lang.Deprecated
    public static void isCollection$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCoverType() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "cover_type")
    @java.lang.Deprecated
    public static void getCoverType$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getCoverImageIndex() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "cover_image_index")
    @java.lang.Deprecated
    public static void getCoverImageIndex$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCoverUrl() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "cover_url")
    @java.lang.Deprecated
    public static void getCoverUrl$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean getAllowOffers() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "allow_offers")
    @java.lang.Deprecated
    public static void getAllowOffers$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean getFreeShipping() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "free_shipping")
    @java.lang.Deprecated
    public static void getFreeShipping$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getPreviousPrice() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "previous_price")
    @java.lang.Deprecated
    public static void getPreviousPrice$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getStock() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getWarranty() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean getReturnsAccepted() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "returns_accepted")
    @java.lang.Deprecated
    public static void getReturnsAccepted$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> getColors() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getProductId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "product_id")
    @java.lang.Deprecated
    public static void getProductId$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
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
    
    public final int component15() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component17() {
        return null;
    }
    
    public final boolean component18() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component21() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean component22() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean component23() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component24() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component25() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component26() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean component27() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> component28() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component29() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    public final double component5() {
        return 0.0;
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
    public final java.util.List<java.lang.String> component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.PostDB copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.String description, double price, @org.jetbrains.annotations.Nullable
    java.lang.String category, @org.jetbrains.annotations.Nullable
    java.lang.String condition, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> tags, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, @org.jetbrains.annotations.NotNull
    java.lang.String status, int likesCount, int reviewsCount, int viewsCount, int sharesCount, int savesCount, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.NotNull
    java.lang.String updatedAt, boolean isCollection, @org.jetbrains.annotations.Nullable
    java.lang.String coverType, @org.jetbrains.annotations.Nullable
    java.lang.Integer coverImageIndex, @org.jetbrains.annotations.Nullable
    java.lang.String coverUrl, @org.jetbrains.annotations.Nullable
    java.lang.Boolean allowOffers, @org.jetbrains.annotations.Nullable
    java.lang.Boolean freeShipping, @org.jetbrains.annotations.Nullable
    java.lang.Double previousPrice, @org.jetbrains.annotations.Nullable
    java.lang.Integer stock, @org.jetbrains.annotations.Nullable
    java.lang.String warranty, @org.jetbrains.annotations.Nullable
    java.lang.Boolean returnsAccepted, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> colors, @org.jetbrains.annotations.Nullable
    java.lang.String productId) {
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
    com.rendly.app.data.model.PostDB self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/PostDB.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/PostDB;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.PostDB> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.PostDB.$serializer INSTANCE = null;
        
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
        public com.rendly.app.data.model.PostDB deserialize(@org.jetbrains.annotations.NotNull
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
        com.rendly.app.data.model.PostDB value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/PostDB$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/PostDB;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.PostDB> serializer() {
            return null;
        }
    }
}