package com.rendly.app.data.model;

import androidx.compose.runtime.Stable;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\bY\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 \u0089\u00012\u00020\u0001:\u0004\u0088\u0001\u0089\u0001B\u00e3\u0002\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\r\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0019\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u001a\u001a\u00020\u001b\u0012\n\b\u0001\u0010\u001c\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u001d\u001a\u00020\u001b\u0012\b\b\u0001\u0010\u001e\u001a\u00020\u001b\u0012\b\b\u0001\u0010\u001f\u001a\u00020\u001b\u0012\u0006\u0010 \u001a\u00020\u001b\u0012\n\b\u0001\u0010!\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\"\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010#\u001a\u00020\u001b\u0012\n\b\u0001\u0010$\u001a\u0004\u0018\u00010%\u0012\b\u0010&\u001a\u0004\u0018\u00010\'\u00a2\u0006\u0002\u0010(B\u00e7\u0002\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u001b\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u001d\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001f\u001a\u00020\u001b\u0012\b\b\u0002\u0010 \u001a\u00020\u001b\u0012\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010#\u001a\u00020\u001b\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010%\u00a2\u0006\u0002\u0010)J\u000b\u0010[\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\\\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010]\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010^\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010_\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010`\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010a\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010b\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010c\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010d\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010e\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010f\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010g\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010h\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010i\u001a\u00020\u001bH\u00c6\u0003J\u000b\u0010j\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010k\u001a\u00020\u001bH\u00c6\u0003J\t\u0010l\u001a\u00020\u001bH\u00c6\u0003J\t\u0010m\u001a\u00020\u001bH\u00c6\u0003J\t\u0010n\u001a\u00020\u001bH\u00c6\u0003J\u000b\u0010o\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010p\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010q\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010r\u001a\u00020\u001bH\u00c6\u0003J\u0010\u0010s\u001a\u0004\u0018\u00010%H\u00c6\u0003\u00a2\u0006\u0002\u0010MJ\u000b\u0010t\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010u\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010v\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010w\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010x\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010y\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u00f2\u0002\u0010z\u001a\u00020\u00002\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\t\u001a\u00020\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u001d\u001a\u00020\u001b2\b\b\u0002\u0010\u001e\u001a\u00020\u001b2\b\b\u0002\u0010\u001f\u001a\u00020\u001b2\b\b\u0002\u0010 \u001a\u00020\u001b2\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010#\u001a\u00020\u001b2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010%H\u00c6\u0001\u00a2\u0006\u0002\u0010{J\u0013\u0010|\u001a\u00020\u001b2\b\u0010}\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010~\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u007f\u001a\u00020\u0005H\u00d6\u0001J.\u0010\u0080\u0001\u001a\u00030\u0081\u00012\u0007\u0010\u0082\u0001\u001a\u00020\u00002\b\u0010\u0083\u0001\u001a\u00030\u0084\u00012\b\u0010\u0085\u0001\u001a\u00030\u0086\u0001H\u00c1\u0001\u00a2\u0006\u0003\b\u0087\u0001R\u001e\u0010\u000f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b*\u0010+\u001a\u0004\b,\u0010-R\u0011\u0010 \u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010/R\u001e\u0010\u0010\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b0\u0010+\u001a\u0004\b1\u0010-R\u001e\u0010\u0007\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b2\u0010+\u001a\u0004\b3\u0010-R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010-R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010-R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010-R\u001e\u0010\r\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b7\u0010+\u001a\u0004\b8\u0010-R\u001e\u0010\u000e\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b9\u0010+\u001a\u0004\b:\u0010-R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010-R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010-R\u0013\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010-R\u001c\u0010\u001d\u001a\u00020\u001b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b>\u0010+\u001a\u0004\b\u001d\u0010/R\u001c\u0010\u001a\u001a\u00020\u001b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b?\u0010+\u001a\u0004\b\u001a\u0010/R\u001c\u0010#\u001a\u00020\u001b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b@\u0010+\u001a\u0004\b#\u0010/R\u001e\u0010\u001c\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bA\u0010+\u001a\u0004\bB\u0010-R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010-R\u001e\u0010!\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bD\u0010+\u001a\u0004\bE\u0010-R\u0013\u0010\"\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u0010-R\u001e\u0010\n\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bG\u0010+\u001a\u0004\bH\u0010-R\u001c\u0010\u001e\u001a\u00020\u001b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bI\u0010+\u001a\u0004\bJ\u0010/R \u0010$\u001a\u0004\u0018\u00010%8\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010N\u0012\u0004\bK\u0010+\u001a\u0004\bL\u0010MR\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u0010-R\u001c\u0010\u001f\u001a\u00020\u001b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bP\u0010+\u001a\u0004\bQ\u0010/R\u0013\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bR\u0010-R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bS\u0010-R\u0013\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bT\u0010-R\u001e\u0010\u0018\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bU\u0010+\u001a\u0004\bV\u0010-R\u001c\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bW\u0010+\u001a\u0004\bX\u0010-R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bY\u0010-R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bZ\u0010-\u00a8\u0006\u008a\u0001"}, d2 = {"Lcom/rendly/app/data/model/Usuario;", "", "seen1", "", "id", "", "userId", "createdAt", "email", "username", "nombreTienda", "descripcion", "genero", "fechaNacimiento", "fechaRegistro", "avatarUrl", "bannerUrl", "facebook", "whatsapp", "twitter", "instagram", "linkedin", "tiktok", "ubicacion", "ultimaActividad", "rol", "isOnline", "", "lastOnline", "isAnonymous", "recibirNovedades", "tieneTienda", "baneado", "motivoBaneo", "nombre", "isVerified", "reputationScore", "", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;ZZZZLjava/lang/String;Ljava/lang/String;ZLjava/lang/Double;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;ZZZZLjava/lang/String;Ljava/lang/String;ZLjava/lang/Double;)V", "getAvatarUrl$annotations", "()V", "getAvatarUrl", "()Ljava/lang/String;", "getBaneado", "()Z", "getBannerUrl$annotations", "getBannerUrl", "getCreatedAt$annotations", "getCreatedAt", "getDescripcion", "getEmail", "getFacebook", "getFechaNacimiento$annotations", "getFechaNacimiento", "getFechaRegistro$annotations", "getFechaRegistro", "getGenero", "getId", "getInstagram", "isAnonymous$annotations", "isOnline$annotations", "isVerified$annotations", "getLastOnline$annotations", "getLastOnline", "getLinkedin", "getMotivoBaneo$annotations", "getMotivoBaneo", "getNombre", "getNombreTienda$annotations", "getNombreTienda", "getRecibirNovedades$annotations", "getRecibirNovedades", "getReputationScore$annotations", "getReputationScore", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getRol", "getTieneTienda$annotations", "getTieneTienda", "getTiktok", "getTwitter", "getUbicacion", "getUltimaActividad$annotations", "getUltimaActividad", "getUserId$annotations", "getUserId", "getUsername", "getWhatsapp", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;ZZZZLjava/lang/String;Ljava/lang/String;ZLjava/lang/Double;)Lcom/rendly/app/data/model/Usuario;", "equals", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
@androidx.compose.runtime.Stable
public final class Usuario {
    @org.jetbrains.annotations.Nullable
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String email = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String username = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String nombreTienda = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String descripcion = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String genero = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String fechaNacimiento = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String fechaRegistro = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String avatarUrl = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String bannerUrl = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String facebook = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String whatsapp = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String twitter = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String instagram = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String linkedin = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String tiktok = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String ubicacion = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String ultimaActividad = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String rol = null;
    private final boolean isOnline = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String lastOnline = null;
    private final boolean isAnonymous = false;
    private final boolean recibirNovedades = false;
    private final boolean tieneTienda = false;
    private final boolean baneado = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String motivoBaneo = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String nombre = null;
    private final boolean isVerified = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double reputationScore = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.Usuario.Companion Companion = null;
    
    public Usuario(@org.jetbrains.annotations.Nullable
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.Nullable
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.Nullable
    java.lang.String nombreTienda, @org.jetbrains.annotations.Nullable
    java.lang.String descripcion, @org.jetbrains.annotations.Nullable
    java.lang.String genero, @org.jetbrains.annotations.Nullable
    java.lang.String fechaNacimiento, @org.jetbrains.annotations.Nullable
    java.lang.String fechaRegistro, @org.jetbrains.annotations.Nullable
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable
    java.lang.String bannerUrl, @org.jetbrains.annotations.Nullable
    java.lang.String facebook, @org.jetbrains.annotations.Nullable
    java.lang.String whatsapp, @org.jetbrains.annotations.Nullable
    java.lang.String twitter, @org.jetbrains.annotations.Nullable
    java.lang.String instagram, @org.jetbrains.annotations.Nullable
    java.lang.String linkedin, @org.jetbrains.annotations.Nullable
    java.lang.String tiktok, @org.jetbrains.annotations.Nullable
    java.lang.String ubicacion, @org.jetbrains.annotations.Nullable
    java.lang.String ultimaActividad, @org.jetbrains.annotations.Nullable
    java.lang.String rol, boolean isOnline, @org.jetbrains.annotations.Nullable
    java.lang.String lastOnline, boolean isAnonymous, boolean recibirNovedades, boolean tieneTienda, boolean baneado, @org.jetbrains.annotations.Nullable
    java.lang.String motivoBaneo, @org.jetbrains.annotations.Nullable
    java.lang.String nombre, boolean isVerified, @org.jetbrains.annotations.Nullable
    java.lang.Double reputationScore) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
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
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCreatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "created_at")
    @java.lang.Deprecated
    public static void getCreatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNombreTienda() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "nombre_tienda")
    @java.lang.Deprecated
    public static void getNombreTienda$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDescripcion() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getGenero() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getFechaNacimiento() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "fecha_nacimiento")
    @java.lang.Deprecated
    public static void getFechaNacimiento$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getFechaRegistro() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "fecha_registro")
    @java.lang.Deprecated
    public static void getFechaRegistro$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getAvatarUrl() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "avatar_url")
    @java.lang.Deprecated
    public static void getAvatarUrl$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getBannerUrl() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "banner_url")
    @java.lang.Deprecated
    public static void getBannerUrl$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getFacebook() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getWhatsapp() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getTwitter() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getInstagram() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLinkedin() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getTiktok() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUbicacion() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUltimaActividad() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "ultima_actividad")
    @java.lang.Deprecated
    public static void getUltimaActividad$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRol() {
        return null;
    }
    
    public final boolean isOnline() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "is_online")
    @java.lang.Deprecated
    public static void isOnline$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastOnline() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "last_online")
    @java.lang.Deprecated
    public static void getLastOnline$annotations() {
    }
    
    public final boolean isAnonymous() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "is_anonymous")
    @java.lang.Deprecated
    public static void isAnonymous$annotations() {
    }
    
    public final boolean getRecibirNovedades() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "recibir_novedades")
    @java.lang.Deprecated
    public static void getRecibirNovedades$annotations() {
    }
    
    public final boolean getTieneTienda() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "tiene_tienda")
    @java.lang.Deprecated
    public static void getTieneTienda$annotations() {
    }
    
    public final boolean getBaneado() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getMotivoBaneo() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "motivo_baneo")
    @java.lang.Deprecated
    public static void getMotivoBaneo$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNombre() {
        return null;
    }
    
    public final boolean isVerified() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "is_verified")
    @java.lang.Deprecated
    public static void isVerified$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getReputationScore() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "reputation_score")
    @java.lang.Deprecated
    public static void getReputationScore$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component13() {
        return null;
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
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component21() {
        return null;
    }
    
    public final boolean component22() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component23() {
        return null;
    }
    
    public final boolean component24() {
        return false;
    }
    
    public final boolean component25() {
        return false;
    }
    
    public final boolean component26() {
        return false;
    }
    
    public final boolean component27() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component28() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component29() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    public final boolean component30() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component31() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
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
    public final com.rendly.app.data.model.Usuario copy(@org.jetbrains.annotations.Nullable
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.Nullable
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.Nullable
    java.lang.String nombreTienda, @org.jetbrains.annotations.Nullable
    java.lang.String descripcion, @org.jetbrains.annotations.Nullable
    java.lang.String genero, @org.jetbrains.annotations.Nullable
    java.lang.String fechaNacimiento, @org.jetbrains.annotations.Nullable
    java.lang.String fechaRegistro, @org.jetbrains.annotations.Nullable
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable
    java.lang.String bannerUrl, @org.jetbrains.annotations.Nullable
    java.lang.String facebook, @org.jetbrains.annotations.Nullable
    java.lang.String whatsapp, @org.jetbrains.annotations.Nullable
    java.lang.String twitter, @org.jetbrains.annotations.Nullable
    java.lang.String instagram, @org.jetbrains.annotations.Nullable
    java.lang.String linkedin, @org.jetbrains.annotations.Nullable
    java.lang.String tiktok, @org.jetbrains.annotations.Nullable
    java.lang.String ubicacion, @org.jetbrains.annotations.Nullable
    java.lang.String ultimaActividad, @org.jetbrains.annotations.Nullable
    java.lang.String rol, boolean isOnline, @org.jetbrains.annotations.Nullable
    java.lang.String lastOnline, boolean isAnonymous, boolean recibirNovedades, boolean tieneTienda, boolean baneado, @org.jetbrains.annotations.Nullable
    java.lang.String motivoBaneo, @org.jetbrains.annotations.Nullable
    java.lang.String nombre, boolean isVerified, @org.jetbrains.annotations.Nullable
    java.lang.Double reputationScore) {
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
    com.rendly.app.data.model.Usuario self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/Usuario.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/Usuario;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.Usuario> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.Usuario.$serializer INSTANCE = null;
        
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
        public com.rendly.app.data.model.Usuario deserialize(@org.jetbrains.annotations.NotNull
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
        com.rendly.app.data.model.Usuario value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/Usuario$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/Usuario;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.Usuario> serializer() {
            return null;
        }
    }
}