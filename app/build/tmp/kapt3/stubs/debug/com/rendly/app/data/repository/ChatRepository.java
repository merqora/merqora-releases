package com.rendly.app.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.remote.ImageKitService;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.service.ChatNotificationService;
import io.github.jan.supabase.postgrest.query.Order;
import io.github.jan.supabase.realtime.PostgresAction;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;
import kotlinx.serialization.json.JsonNull;
import com.rendly.app.data.cache.BadgeCountCache;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u009e\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010%\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u001c\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u001a\n\u0002\u0010$\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b \n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J,\u0010H\u001a\b\u0012\u0004\u0012\u00020J0I2\u0006\u0010K\u001a\u00020\u00042\u0006\u0010L\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bM\u0010NJ\u001e\u0010O\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u00042\u0006\u0010Q\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ\"\u0010R\u001a\u00020\u00182\u0006\u0010S\u001a\u00020\u00042\n\b\u0002\u0010T\u001a\u0004\u0018\u00010\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ\u001e\u0010U\u001a\u00020J2\u0006\u0010P\u001a\u00020\u00042\f\u0010V\u001a\b\u0012\u0004\u0012\u00020\u00160\u0013H\u0002J\u0010\u0010W\u001a\u00020\u00042\u0006\u0010X\u001a\u00020\u0004H\u0002J\u0016\u0010Y\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0006\u0010[\u001a\u00020JJ\u0006\u0010\\\u001a\u00020JJ,\u0010]\u001a\u0004\u0018\u00010!2\u0006\u0010^\u001a\u00020\u00042\u0006\u0010_\u001a\u00020\u00042\n\b\u0002\u0010`\u001a\u0004\u0018\u00010\u0004H\u0086@\u00a2\u0006\u0002\u0010aJ\u0016\u0010b\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0016\u0010c\u001a\u00020\u00182\u0006\u0010Q\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0016\u0010d\u001a\u00020\u00182\u0006\u0010e\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ(\u0010f\u001a\u0004\u0018\u00010g2\u0006\u0010h\u001a\u00020#2\u0006\u0010P\u001a\u00020\u00042\u0006\u0010i\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010jJ$\u0010k\u001a\b\u0012\u0004\u0012\u00020\u00160\u00132\f\u0010V\u001a\b\u0012\u0004\u0012\u00020\u00160\u00132\u0006\u0010P\u001a\u00020\u0004H\u0002J\u0014\u0010l\u001a\b\u0012\u0004\u0012\u00020m0\u0013H\u0086@\u00a2\u0006\u0002\u0010nJ\u0016\u0010o\u001a\n\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u00132\u0006\u0010P\u001a\u00020\u0004J\u000e\u0010p\u001a\u00020\u00042\u0006\u0010X\u001a\u00020\u0004J\u0012\u0010q\u001a\u0004\u0018\u00010\u00042\u0006\u0010P\u001a\u00020\u0004H\u0002J\u001c\u0010r\u001a\b\u0012\u0004\u0012\u00020!0\u00132\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0018\u0010s\u001a\u0004\u0018\u00010\u00042\u0006\u0010t\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u000e\u0010u\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004J\u000e\u0010v\u001a\u00020J2\u0006\u0010h\u001a\u00020#J\u0016\u0010w\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0016\u0010x\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u000e\u0010y\u001a\u00020\u00182\u0006\u0010X\u001a\u00020\u0004J\u000e\u0010z\u001a\u00020\u00182\u0006\u0010X\u001a\u00020\u0004J\u000e\u0010{\u001a\u00020\u00182\u0006\u0010X\u001a\u00020\u0004J\u000e\u0010|\u001a\u00020\u00182\u0006\u0010X\u001a\u00020\u0004J\u0016\u0010}\u001a\u00020\u00182\u0006\u0010t\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u000e\u0010~\u001a\u00020JH\u0086@\u00a2\u0006\u0002\u0010nJ\u0016\u0010\u007f\u001a\u00020J2\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0017\u0010\u0080\u0001\u001a\u00020\t2\u0006\u0010e\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u000f\u0010\u0081\u0001\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010nJ\u0015\u0010\u0082\u0001\u001a\b\u0012\u0004\u0012\u00020!0\u0013H\u0086@\u00a2\u0006\u0002\u0010nJ\u0017\u0010\u0083\u0001\u001a\u00020J2\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0017\u0010\u0084\u0001\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ$\u0010\u0085\u0001\u001a\u00020J2\u0006\u0010P\u001a\u00020\u00042\u000b\b\u0002\u0010\u0086\u0001\u001a\u0004\u0018\u00010\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ\'\u0010\u0087\u0001\u001a\u0015\u0012\u0004\u0012\u00020\u0004\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00130\u0088\u00012\t\u0010\u0089\u0001\u001a\u0004\u0018\u00010\u0004H\u0002J\u0017\u0010\u008a\u0001\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ8\u0010\u008b\u0001\u001a\b\u0012\u0004\u0012\u00020J0I2\u0006\u0010K\u001a\u00020\u00042\u0006\u0010L\u001a\u00020\u00042\b\b\u0002\u0010T\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0005\b\u008c\u0001\u0010aJ\u001f\u0010\u008d\u0001\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u00042\u0006\u0010Q\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ-\u0010\u008e\u0001\u001a\u00020\u00182\u0007\u0010\u008f\u0001\u001a\u00020\u00042\u0006\u0010T\u001a\u00020\u00042\u000b\b\u0002\u0010\u0090\u0001\u001a\u0004\u0018\u00010\u0004H\u0086@\u00a2\u0006\u0002\u0010aJ&\u0010\u0091\u0001\u001a\b\u0012\u0004\u0012\u00020\u00160\u00132\u0006\u0010P\u001a\u00020\u00042\u0007\u0010\u0092\u0001\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ\u001f\u0010\u0093\u0001\u001a\t\u0012\u0005\u0012\u00030\u0094\u00010\u00132\u0007\u0010\u0092\u0001\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ0\u0010\u0095\u0001\u001a\b\u0012\u0004\u0012\u00020J0I2\u0007\u0010\u0096\u0001\u001a\u00020\u00042\u0007\u0010\u0097\u0001\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0005\b\u0098\u0001\u0010NJ=\u0010\u0099\u0001\u001a\u00020\u00182\u0007\u0010\u0096\u0001\u001a\u00020\u00042\u0007\u0010\u0097\u0001\u001a\u00020\u00042\u0007\u0010\u009a\u0001\u001a\u00020\u00042\u0007\u0010\u009b\u0001\u001a\u00020\u00042\u0007\u0010\u009c\u0001\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0003\u0010\u009d\u0001J\u001f\u0010\u009e\u0001\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u00042\u0006\u0010X\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ\u001a\u0010\u009f\u0001\u001a\u00020J2\u0006\u0010P\u001a\u00020\u00042\u0007\u0010\u00a0\u0001\u001a\u00020\u0004H\u0002J\u0010\u0010\u00a1\u0001\u001a\u00020J2\u0007\u0010\u00a2\u0001\u001a\u00020\u0018J\u0010\u0010\u00a3\u0001\u001a\u00020J2\u0007\u0010\u00a4\u0001\u001a\u00020\u0018J\u0018\u0010\u00a5\u0001\u001a\u00020J2\u0006\u0010P\u001a\u00020\u00042\u0007\u0010\u00a4\u0001\u001a\u00020\u0018J\u000f\u0010\u00a6\u0001\u001a\u00020JH\u0086@\u00a2\u0006\u0002\u0010nJ\u0011\u0010\u00a7\u0001\u001a\u00020J2\u0006\u0010P\u001a\u00020\u0004H\u0002J \u0010\u00a8\u0001\u001a\u00020\u00182\u0006\u0010e\u001a\u00020\u00042\u0007\u0010\u00a9\u0001\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ\u0017\u0010\u00aa\u0001\u001a\u00020\u00182\u0006\u0010S\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0017\u0010\u00ab\u0001\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u0017\u0010\u00ac\u0001\u001a\u00020\u00182\u0006\u0010P\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010ZJ\u000f\u0010\u00ad\u0001\u001a\u00020JH\u0086@\u00a2\u0006\u0002\u0010nJ\u001a\u0010\u00ae\u0001\u001a\u00020J2\u0006\u0010P\u001a\u00020\u00042\u0007\u0010\u009c\u0001\u001a\u00020\u0016H\u0002J4\u0010\u00af\u0001\u001a\u00020\u00182\u0006\u0010Q\u001a\u00020\u00042\u0006\u0010^\u001a\u00020\u00042\u0006\u0010_\u001a\u00020\u00042\n\b\u0002\u0010`\u001a\u0004\u0018\u00010\u0004H\u0086@\u00a2\u0006\u0003\u0010\u00b0\u0001J \u0010\u00b1\u0001\u001a\u00020\u00182\u0006\u0010e\u001a\u00020\u00042\u0007\u0010\u00b2\u0001\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010NJ,\u0010\u00b3\u0001\u001a\u0004\u0018\u00010\u00042\u0006\u0010h\u001a\u00020#2\u0006\u0010P\u001a\u00020\u00042\b\u0010\u00b4\u0001\u001a\u00030\u00b5\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00b6\u0001J,\u0010\u00b7\u0001\u001a\u0004\u0018\u00010\u00042\u0006\u0010h\u001a\u00020#2\u0006\u0010P\u001a\u00020\u00042\b\u0010\u00b8\u0001\u001a\u00030\u00b5\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00b6\u0001R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00180\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00180\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00180\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00180\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u001d\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\t0\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\"\u001a\u0004\u0018\u00010#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00130%\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0010\u0010(\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00130%\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\'R\u0010\u0010+\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010,\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010-\u001a\u0004\u0018\u00010.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010/\u001a\u0004\u0018\u00010.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00100\u001a\u00020\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u00101\u001a\b\u0012\u0004\u0012\u00020\u00180%\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\'R\u000e\u00103\u001a\u00020\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u00104\u001a\b\u0012\u0004\u0012\u00020\u00180%\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010\'R\u0017\u00105\u001a\b\u0012\u0004\u0012\u00020\u00180%\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010\'R\u0017\u00106\u001a\b\u0012\u0004\u0012\u00020\u00180%\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010\'R\u0017\u00107\u001a\b\u0012\u0004\u0012\u00020\u00180%\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010\'R\u0019\u00108\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040%\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010\'R\u000e\u0010:\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R \u0010;\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00130<X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010=\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010>\u001a\u0004\u0018\u00010.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010?\u001a\u0004\u0018\u00010.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010@\u001a\b\u0012\u0004\u0012\u00020\u00040%\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010\'R\u000e\u0010B\u001a\u00020CX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010D\u001a\b\u0012\u0004\u0012\u00020\t0%\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010\'R\u001d\u0010F\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0\u00130%\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010\'\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u00b9\u0001"}, d2 = {"Lcom/rendly/app/data/repository/ChatRepository;", "", "()V", "CLIENT_REQUEST_ACCEPTED", "", "CLIENT_REQUEST_PENDING", "CLIENT_REQUEST_PREFIX", "CLIENT_REQUEST_REJECTED", "INITIAL_MESSAGE_LIMIT", "", "KEY_PREFIX_CLEARED", "LOAD_MORE_LIMIT", "MAX_CACHED_CONVERSATIONS", "PREFS_NAME", "TAG", "TYPING_THROTTLE_MS", "", "_conversations", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/repository/Conversation;", "_currentMessages", "Lcom/rendly/app/data/repository/Message;", "_hasMoreMessages", "", "_isLoading", "_isLoadingMore", "_isOtherUserOnline", "_isOtherUserTyping", "_lastError", "_realtimeStatus", "_totalUnreadCount", "_userLabels", "Lcom/rendly/app/data/repository/ChatLabel;", "appContext", "Landroid/content/Context;", "conversations", "Lkotlinx/coroutines/flow/StateFlow;", "getConversations", "()Lkotlinx/coroutines/flow/StateFlow;", "currentConversationId", "currentMessages", "getCurrentMessages", "currentOtherUserId", "currentOtherUserName", "globalMessagesChannel", "Lio/github/jan/supabase/realtime/RealtimeChannel;", "globalParticipantsChannel", "hasMoreMessages", "hasMoreMessagesFlow", "getHasMoreMessagesFlow", "isGlobalSubscribed", "isLoading", "isLoadingMore", "isOtherUserOnline", "isOtherUserTyping", "lastError", "getLastError", "lastTypingBroadcast", "messagesCache", "", "oldestLoadedMessageDate", "presenceChannel", "realtimeChannel", "realtimeStatus", "getRealtimeStatus", "scope", "Lkotlinx/coroutines/CoroutineScope;", "totalUnreadCount", "getTotalUnreadCount", "userLabels", "getUserLabels", "acceptClientFromChat", "Lkotlin/Result;", "", "requesterId", "requesterUsername", "acceptClientFromChat-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "assignLabel", "conversationId", "labelId", "blockUser", "blockedId", "reason", "cacheMessages", "messages", "cleanMessageContent", "content", "clearChat", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearCurrentChat", "clearError", "createLabel", "name", "color", "icon", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteConversation", "deleteLabel", "deleteMessage", "messageId", "exportChatAsPdf", "Ljava/io/File;", "context", "otherUsername", "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "filterByClearedAt", "getBlockedUsers", "Lcom/rendly/app/data/repository/BlockedUserInfo;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCachedMessages", "getCleanMessageContent", "getClearedAt", "getLabelsForConversation", "getOrCreateConversation", "otherUserId", "hasCachedMessages", "init", "isChatMuted", "isChatPinned", "isClientAcceptedMessage", "isClientPendingMessage", "isClientRejectedMessage", "isClientRequestMessage", "isUserBlocked", "loadConversations", "loadMessages", "loadMessagesUntilFound", "loadMoreMessages", "loadUserLabels", "markMessagesAsRead", "muteChat", "openChat", "otherUserName", "parseReactionsJson", "", "reactionsRaw", "pinChat", "rejectClientFromChat", "rejectClientFromChat-BWLJW6A", "removeLabel", "reportUser", "reportedUserId", "description", "searchMessages", "query", "searchUsers", "Lcom/rendly/app/data/model/Usuario;", "sendClientRequest", "sellerId", "sellerUsername", "sendClientRequest-0E7RQCE", "sendConsultMessage", "postId", "postTitle", "message", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendMessage", "setClearedAt", "timestamp", "setOtherUserOnline", "isOnline", "setOtherUserTyping", "isTyping", "setTyping", "subscribeToGlobalMessages", "subscribeToMessages", "toggleReaction", "emoji", "unblockUser", "unmuteChat", "unpinChat", "unsubscribeFromGlobalMessages", "updateCacheWithNewMessage", "updateLabel", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateMessage", "newContent", "uploadAndSendAudio", "audioUri", "Landroid/net/Uri;", "(Landroid/content/Context;Ljava/lang/String;Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadAndSendMedia", "mediaUri", "app_debug"})
public final class ChatRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ChatRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable
    private static android.content.Context appContext;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String currentOtherUserName;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.Conversation>> _conversations = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.Conversation>> conversations = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.Message>> _currentMessages = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.Message>> currentMessages = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _totalUnreadCount = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> totalUnreadCount = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PREFS_NAME = "rendly_chat_prefs";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_PREFIX_CLEARED = "cleared_at_";
    @org.jetbrains.annotations.NotNull
    private static final java.util.Map<java.lang.String, java.util.List<com.rendly.app.data.repository.Message>> messagesCache = null;
    private static final int MAX_CACHED_CONVERSATIONS = 20;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isOtherUserTyping = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOtherUserTyping = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isOtherUserOnline = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOtherUserOnline = null;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String currentConversationId;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String currentOtherUserId;
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel realtimeChannel;
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel presenceChannel;
    private static long lastTypingBroadcast = 0L;
    private static final long TYPING_THROTTLE_MS = 500L;
    private static boolean hasMoreMessages = true;
    private static final int INITIAL_MESSAGE_LIMIT = 15;
    private static final int LOAD_MORE_LIMIT = 12;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String oldestLoadedMessageDate;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _hasMoreMessages = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> hasMoreMessagesFlow = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoadingMore = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoadingMore = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _lastError = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.String> lastError = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _realtimeStatus = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.String> realtimeStatus = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CLIENT_REQUEST_PREFIX = "[[CLIENT_REQUEST]]";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CLIENT_REQUEST_ACCEPTED = "[[CLIENT_ACCEPTED]]";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CLIENT_REQUEST_REJECTED = "[[CLIENT_REJECTED]]";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CLIENT_REQUEST_PENDING = "[[CLIENT_PENDING]]";
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel globalMessagesChannel;
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel globalParticipantsChannel;
    private static boolean isGlobalSubscribed = false;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.ChatLabel>> _userLabels = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.ChatLabel>> userLabels = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.ChatRepository INSTANCE = null;
    
    private ChatRepository() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.Conversation>> getConversations() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.Message>> getCurrentMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getTotalUnreadCount() {
        return null;
    }
    
    private final java.lang.String getClearedAt(java.lang.String conversationId) {
        return null;
    }
    
    private final void setClearedAt(java.lang.String conversationId, java.lang.String timestamp) {
    }
    
    private final java.util.List<com.rendly.app.data.repository.Message> filterByClearedAt(java.util.List<com.rendly.app.data.repository.Message> messages, java.lang.String conversationId) {
        return null;
    }
    
    /**
     * Obtiene mensajes del cache si existen
     */
    @org.jetbrains.annotations.Nullable
    public final java.util.List<com.rendly.app.data.repository.Message> getCachedMessages(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId) {
        return null;
    }
    
    /**
     * Guarda mensajes en cache
     */
    private final void cacheMessages(java.lang.String conversationId, java.util.List<com.rendly.app.data.repository.Message> messages) {
    }
    
    /**
     * Actualiza cache con nuevo mensaje
     */
    private final void updateCacheWithNewMessage(java.lang.String conversationId, com.rendly.app.data.repository.Message message) {
    }
    
    /**
     * Indica si la conversación tiene cache disponible
     */
    public final boolean hasCachedMessages(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOtherUserTyping() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOtherUserOnline() {
        return null;
    }
    
    public final void setTyping(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, boolean isTyping) {
    }
    
    public final void setOtherUserOnline(boolean isOnline) {
    }
    
    public final void setOtherUserTyping(boolean isTyping) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object searchUsers(@org.jetbrains.annotations.NotNull
    java.lang.String query, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.model.Usuario>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadConversations(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getOrCreateConversation(@org.jetbrains.annotations.NotNull
    java.lang.String otherUserId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHasMoreMessagesFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoadingMore() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadMessages(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadMoreMessages(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Cargar mensajes hasta encontrar un mensaje específico por ID (para scroll desde búsqueda)
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadMessagesUntilFound(@org.jetbrains.annotations.NotNull
    java.lang.String messageId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getLastError() {
        return null;
    }
    
    public final void clearError() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendMessage(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String content, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object uploadAndSendMedia(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    android.net.Uri mediaUri, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object uploadAndSendAudio(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    android.net.Uri audioUri, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object toggleReaction(@org.jetbrains.annotations.NotNull
    java.lang.String messageId, @org.jetbrains.annotations.NotNull
    java.lang.String emoji, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateMessage(@org.jetbrains.annotations.NotNull
    java.lang.String messageId, @org.jetbrains.annotations.NotNull
    java.lang.String newContent, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteMessage(@org.jetbrains.annotations.NotNull
    java.lang.String messageId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object markMessagesAsRead(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object openChat(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.Nullable
    java.lang.String otherUserName, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getRealtimeStatus() {
        return null;
    }
    
    private final void subscribeToMessages(java.lang.String conversationId) {
    }
    
    public final void clearCurrentChat() {
    }
    
    /**
     * Verificar si un mensaje es una solicitud de cliente
     */
    public final boolean isClientRequestMessage(@org.jetbrains.annotations.NotNull
    java.lang.String content) {
        return false;
    }
    
    public final boolean isClientAcceptedMessage(@org.jetbrains.annotations.NotNull
    java.lang.String content) {
        return false;
    }
    
    public final boolean isClientRejectedMessage(@org.jetbrains.annotations.NotNull
    java.lang.String content) {
        return false;
    }
    
    public final boolean isClientPendingMessage(@org.jetbrains.annotations.NotNull
    java.lang.String content) {
        return false;
    }
    
    /**
     * Obtener el contenido limpio del mensaje (sin prefijo)
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCleanMessageContent(@org.jetbrains.annotations.NotNull
    java.lang.String content) {
        return null;
    }
    
    /**
     * Parsear JSON de reacciones a Map<String, List<String>>
     */
    private final java.util.Map<java.lang.String, java.util.List<java.lang.String>> parseReactionsJson(java.lang.String reactionsRaw) {
        return null;
    }
    
    /**
     * Suscribirse a nuevos mensajes para actualizar el badge en HomeHeader
     * Debe llamarse al iniciar la app (después del login)
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object subscribeToGlobalMessages(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Desuscribirse de mensajes globales
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object unsubscribeFromGlobalMessages(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Silenciar notificaciones de una conversación
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object muteChat(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Reactivar notificaciones de una conversación
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object unmuteChat(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Verificar si una conversación está silenciada
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object isChatMuted(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Vaciar chat - solo local, persiste cleared_at en SharedPreferences
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object clearChat(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Eliminar conversación del listado (ocultar localmente usando cleared_at + remover de lista)
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteConversation(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Buscar mensajes en una conversación
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object searchMessages(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String query, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.repository.Message>> $completion) {
        return null;
    }
    
    /**
     * Bloquear usuario
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object blockUser(@org.jetbrains.annotations.NotNull
    java.lang.String blockedId, @org.jetbrains.annotations.Nullable
    java.lang.String reason, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Desbloquear usuario
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object unblockUser(@org.jetbrains.annotations.NotNull
    java.lang.String blockedId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Verificar si un usuario está bloqueado
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object isUserBlocked(@org.jetbrains.annotations.NotNull
    java.lang.String otherUserId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Obtener lista de usuarios bloqueados con info de perfil
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getBlockedUsers(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.repository.BlockedUserInfo>> $completion) {
        return null;
    }
    
    /**
     * Reportar usuario
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object reportUser(@org.jetbrains.annotations.NotNull
    java.lang.String reportedUserId, @org.jetbrains.annotations.NotNull
    java.lang.String reason, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Limpiar contenido de mensaje para exportación legible
     */
    private final java.lang.String cleanMessageContent(java.lang.String content) {
        return null;
    }
    
    /**
     * Exportar chat como PDF profesional
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object exportChatAsPdf(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String otherUsername, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.io.File> $completion) {
        return null;
    }
    
    /**
     * Fijar un chat
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object pinChat(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Desfijar un chat
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object unpinChat(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Verificar si un chat está fijado
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object isChatPinned(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.ChatLabel>> getUserLabels() {
        return null;
    }
    
    /**
     * Cargar etiquetas del usuario (crea las por defecto si no existen)
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadUserLabels(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.repository.ChatLabel>> $completion) {
        return null;
    }
    
    /**
     * Crear nueva etiqueta
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object createLabel(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String color, @org.jetbrains.annotations.Nullable
    java.lang.String icon, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.repository.ChatLabel> $completion) {
        return null;
    }
    
    /**
     * Actualizar etiqueta existente
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateLabel(@org.jetbrains.annotations.NotNull
    java.lang.String labelId, @org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String color, @org.jetbrains.annotations.Nullable
    java.lang.String icon, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Eliminar etiqueta
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteLabel(@org.jetbrains.annotations.NotNull
    java.lang.String labelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Asignar etiqueta a una conversación
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object assignLabel(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String labelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Remover etiqueta de una conversación
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object removeLabel(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String labelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Obtener etiquetas asignadas a una conversación
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getLabelsForConversation(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.repository.ChatLabel>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendConsultMessage(@org.jetbrains.annotations.NotNull
    java.lang.String sellerId, @org.jetbrains.annotations.NotNull
    java.lang.String sellerUsername, @org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    java.lang.String postTitle, @org.jetbrains.annotations.NotNull
    java.lang.String message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}