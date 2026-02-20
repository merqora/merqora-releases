package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\by\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 \u00b2\u00012\u00020\u0001:\u0004\u00b1\u0001\u00b2\u0001B\u00a3\u0003\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\t\u001a\u0004\u0018\u00010\n\u0012\n\b\u0001\u0010\u000b\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\f\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\r\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010\u000e\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010\u000f\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010\u0010\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010\u0011\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\u0012\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\u0013\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010\u0014\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\u0015\u001a\u0004\u0018\u00010\u0006\u0012\u0006\u0010\u0016\u001a\u00020\u0017\u0012\u0006\u0010\u0018\u001a\u00020\u0017\u0012\b\b\u0001\u0010\u0019\u001a\u00020\u0003\u0012\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b\u0012\b\b\u0001\u0010\u001c\u001a\u00020\u001d\u0012\n\b\u0001\u0010\u001e\u001a\u0004\u0018\u00010\u001f\u0012\n\b\u0001\u0010 \u001a\u0004\u0018\u00010\u001f\u0012\n\b\u0001\u0010!\u001a\u0004\u0018\u00010\u001f\u0012\b\u0010\"\u001a\u0004\u0018\u00010#\u0012\n\b\u0001\u0010$\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010%\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010&\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0001\u0010\'\u001a\u00020\u001d\u0012\b\b\u0001\u0010(\u001a\u00020\u001d\u0012\n\b\u0001\u0010)\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010*\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010+\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010,\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010-\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010.\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010/\u001a\u0004\u0018\u000100\u00a2\u0006\u0002\u00101B\u0089\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\u0006\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0006\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0017\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0017\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u001d\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u001f\u0012\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u001f\u0012\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u001f\u0012\b\b\u0002\u0010\"\u001a\u00020#\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\'\u001a\u00020\u001d\u0012\b\b\u0002\u0010(\u001a\u00020\u001d\u0012\n\b\u0002\u0010)\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010*\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010+\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010,\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010-\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u00102J\t\u0010~\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010\u007f\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\n\u0010\u0080\u0001\u001a\u00020\u0006H\u00c6\u0003J\f\u0010\u0081\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u0082\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\n\u0010\u0083\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0084\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0085\u0001\u001a\u00020\u0017H\u00c6\u0003J\n\u0010\u0086\u0001\u001a\u00020\u0017H\u00c6\u0003J\n\u0010\u0087\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0088\u0001\u001a\u00020\u001bH\u00c6\u0003J\n\u0010\u0089\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u008a\u0001\u001a\u00020\u001dH\u00c6\u0003J\u0011\u0010\u008b\u0001\u001a\u0004\u0018\u00010\u001fH\u00c6\u0003\u00a2\u0006\u0002\u00106J\u0011\u0010\u008c\u0001\u001a\u0004\u0018\u00010\u001fH\u00c6\u0003\u00a2\u0006\u0002\u00106J\u0011\u0010\u008d\u0001\u001a\u0004\u0018\u00010\u001fH\u00c6\u0003\u00a2\u0006\u0002\u00106J\n\u0010\u008e\u0001\u001a\u00020#H\u00c6\u0003J\f\u0010\u008f\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u0090\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u0091\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\n\u0010\u0092\u0001\u001a\u00020\u001dH\u00c6\u0003J\n\u0010\u0093\u0001\u001a\u00020\u001dH\u00c6\u0003J\n\u0010\u0094\u0001\u001a\u00020\u0006H\u00c6\u0003J\f\u0010\u0095\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u0096\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u0097\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u0098\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u0099\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u009a\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\n\u0010\u009b\u0001\u001a\u00020\nH\u00c6\u0003J\n\u0010\u009c\u0001\u001a\u00020\u0006H\u00c6\u0003J\f\u0010\u009d\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u009e\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u009f\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\f\u0010\u00a0\u0001\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u0094\u0003\u0010\u00a1\u0001\u001a\u00020\u00002\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u00062\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0011\u001a\u00020\u00062\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0014\u001a\u00020\u00062\b\b\u0002\u0010\u0015\u001a\u00020\u00062\b\b\u0002\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u0018\u001a\u00020\u00172\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\b\b\u0002\u0010\u001c\u001a\u00020\u001d2\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u001f2\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u001f2\b\b\u0002\u0010\"\u001a\u00020#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\'\u001a\u00020\u001d2\b\b\u0002\u0010(\u001a\u00020\u001d2\n\b\u0002\u0010)\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010*\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010+\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010,\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010-\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u0006H\u00c6\u0001\u00a2\u0006\u0003\u0010\u00a2\u0001J\u0015\u0010\u00a3\u0001\u001a\u00020\u001d2\t\u0010\u00a4\u0001\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0007\u0010\u00a5\u0001\u001a\u00020\u0006J\u0007\u0010\u00a6\u0001\u001a\u00020\u0006J\n\u0010\u00a7\u0001\u001a\u00020\u0003H\u00d6\u0001J\n\u0010\u00a8\u0001\u001a\u00020\u0006H\u00d6\u0001J.\u0010\u00a9\u0001\u001a\u00030\u00aa\u00012\u0007\u0010\u00ab\u0001\u001a\u00020\u00002\b\u0010\u00ac\u0001\u001a\u00030\u00ad\u00012\b\u0010\u00ae\u0001\u001a\u00030\u00af\u0001H\u00c1\u0001\u00a2\u0006\u0003\b\u00b0\u0001R \u0010!\u001a\u0004\u0018\u00010\u001f8\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u00107\u0012\u0004\b3\u00104\u001a\u0004\b5\u00106R\u001c\u0010\t\u001a\u00020\n8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b8\u00104\u001a\u0004\b9\u0010:R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010<R\u0011\u0010\u0011\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010<R\u0011\u0010>\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b?\u0010<R\u001c\u0010\u0019\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b@\u00104\u001a\u0004\bA\u0010BR\u0011\u0010\u0014\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010<R\u001c\u0010\u0015\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bD\u00104\u001a\u0004\bE\u0010<R\u001e\u0010+\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bF\u00104\u001a\u0004\bG\u0010<R\u001e\u0010)\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bH\u00104\u001a\u0004\bI\u0010<R\u0011\u0010J\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\bK\u0010<R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\bL\u0010<R\u001c\u0010\u000b\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bM\u00104\u001a\u0004\bN\u0010<R\u001e\u0010&\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bO\u00104\u001a\u0004\bP\u0010<R\u001e\u0010%\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bQ\u00104\u001a\u0004\bR\u0010<R \u0010 \u001a\u0004\u0018\u00010\u001f8\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u00107\u0012\u0004\bS\u00104\u001a\u0004\bT\u00106R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\bU\u0010<R\u001c\u0010(\u001a\u00020\u001d8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bV\u00104\u001a\u0004\b(\u0010WR\u001c\u0010\'\u001a\u00020\u001d8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bX\u00104\u001a\u0004\b\'\u0010WR\u0011\u0010Y\u001a\u00020\u001d8F\u00a2\u0006\u0006\u001a\u0004\bY\u0010WR\u0011\u0010Z\u001a\u00020\u001d8F\u00a2\u0006\u0006\u001a\u0004\bZ\u0010WR\u0011\u0010[\u001a\u00020\u001d8F\u00a2\u0006\u0006\u001a\u0004\b[\u0010WR\u001c\u0010\u001c\u001a\u00020\u001d8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\\\u00104\u001a\u0004\b\u001c\u0010WR\u0011\u0010\b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b]\u0010<R\u001e\u0010.\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b^\u00104\u001a\u0004\b_\u0010<R\u0011\u0010\u0016\u001a\u00020\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b`\u0010aR\u0011\u0010\u0018\u001a\u00020\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\bb\u0010aR\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\bc\u0010<R\u001e\u0010\u0013\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bd\u00104\u001a\u0004\be\u0010<R\u001e\u0010$\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bf\u00104\u001a\u0004\bg\u0010<R\u001e\u0010*\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bh\u00104\u001a\u0004\bi\u0010<R\u0011\u0010j\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\bk\u0010<R\u0011\u0010\"\u001a\u00020#\u00a2\u0006\b\n\u0000\u001a\u0004\bl\u0010mR\u001e\u0010\u0012\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bn\u00104\u001a\u0004\bo\u0010<R\u0011\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\bp\u0010qR\u001e\u0010\f\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\br\u00104\u001a\u0004\bs\u0010<R\u001e\u0010\r\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bt\u00104\u001a\u0004\bu\u0010<R \u0010\u001e\u001a\u0004\u0018\u00010\u001f8\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u00107\u0012\u0004\bv\u00104\u001a\u0004\bw\u00106R\u001e\u0010,\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bx\u00104\u001a\u0004\by\u0010<R\u001c\u0010\u0007\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bz\u00104\u001a\u0004\b{\u0010<R\u001e\u0010-\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b|\u00104\u001a\u0004\b}\u0010<\u00a8\u0006\u00b3\u0001"}, d2 = {"Lcom/rendly/app/data/model/Address;", "", "seen1", "", "seen2", "id", "", "userId", "label", "addressType", "Lcom/rendly/app/data/model/AddressType;", "formattedAddress", "streetAddress", "streetNumber", "apartment", "floor", "neighborhood", "city", "stateProvince", "postalCode", "country", "countryCode", "latitude", "", "longitude", "confidenceScore", "status", "Lcom/rendly/app/data/model/AddressStatus;", "isVerified", "", "textCoordConsistency", "", "gpsGeocodeDistance", "addressCompleteness", "source", "Lcom/rendly/app/data/model/AddressSource;", "rawInput", "geocodeProvider", "geocodePlaceId", "isDefault", "isActive", "deliveryInstructions", "referencePoint", "createdAt", "updatedAt", "verifiedAt", "lastUsedAt", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/rendly/app/data/model/AddressType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DDILcom/rendly/app/data/model/AddressStatus;ZLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Float;Lcom/rendly/app/data/model/AddressSource;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/rendly/app/data/model/AddressType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DDILcom/rendly/app/data/model/AddressStatus;ZLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Float;Lcom/rendly/app/data/model/AddressSource;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAddressCompleteness$annotations", "()V", "getAddressCompleteness", "()Ljava/lang/Float;", "Ljava/lang/Float;", "getAddressType$annotations", "getAddressType", "()Lcom/rendly/app/data/model/AddressType;", "getApartment", "()Ljava/lang/String;", "getCity", "cityWithPostalCode", "getCityWithPostalCode", "getConfidenceScore$annotations", "getConfidenceScore", "()I", "getCountry", "getCountryCode$annotations", "getCountryCode", "getCreatedAt$annotations", "getCreatedAt", "getDeliveryInstructions$annotations", "getDeliveryInstructions", "displayAddress", "getDisplayAddress", "getFloor", "getFormattedAddress$annotations", "getFormattedAddress", "getGeocodePlaceId$annotations", "getGeocodePlaceId", "getGeocodeProvider$annotations", "getGeocodeProvider", "getGpsGeocodeDistance$annotations", "getGpsGeocodeDistance", "getId", "isActive$annotations", "()Z", "isDefault$annotations", "isInvalid", "isSuspicious", "isValid", "isVerified$annotations", "getLabel", "getLastUsedAt$annotations", "getLastUsedAt", "getLatitude", "()D", "getLongitude", "getNeighborhood", "getPostalCode$annotations", "getPostalCode", "getRawInput$annotations", "getRawInput", "getReferencePoint$annotations", "getReferencePoint", "shortAddress", "getShortAddress", "getSource", "()Lcom/rendly/app/data/model/AddressSource;", "getStateProvince$annotations", "getStateProvince", "getStatus", "()Lcom/rendly/app/data/model/AddressStatus;", "getStreetAddress$annotations", "getStreetAddress", "getStreetNumber$annotations", "getStreetNumber", "getTextCoordConsistency$annotations", "getTextCoordConsistency", "getUpdatedAt$annotations", "getUpdatedAt", "getUserId$annotations", "getUserId", "getVerifiedAt$annotations", "getVerifiedAt", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component32", "component33", "component34", "component35", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/rendly/app/data/model/AddressType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DDILcom/rendly/app/data/model/AddressStatus;ZLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Float;Lcom/rendly/app/data/model/AddressSource;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/rendly/app/data/model/Address;", "equals", "other", "getStatusText", "getTypeIcon", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
public final class Address {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String label = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.AddressType addressType = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String formattedAddress = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String streetAddress = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String streetNumber = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String apartment = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String floor = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String neighborhood = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String city = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String stateProvince = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String postalCode = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String country = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String countryCode = null;
    private final double latitude = 0.0;
    private final double longitude = 0.0;
    private final int confidenceScore = 0;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.AddressStatus status = null;
    private final boolean isVerified = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Float textCoordConsistency = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Float gpsGeocodeDistance = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Float addressCompleteness = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.AddressSource source = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String rawInput = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String geocodeProvider = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String geocodePlaceId = null;
    private final boolean isDefault = false;
    private final boolean isActive = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String deliveryInstructions = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String referencePoint = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String updatedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String verifiedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String lastUsedAt = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.Address.Companion Companion = null;
    
    public Address(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String label, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressType addressType, @org.jetbrains.annotations.NotNull
    java.lang.String formattedAddress, @org.jetbrains.annotations.Nullable
    java.lang.String streetAddress, @org.jetbrains.annotations.Nullable
    java.lang.String streetNumber, @org.jetbrains.annotations.Nullable
    java.lang.String apartment, @org.jetbrains.annotations.Nullable
    java.lang.String floor, @org.jetbrains.annotations.Nullable
    java.lang.String neighborhood, @org.jetbrains.annotations.NotNull
    java.lang.String city, @org.jetbrains.annotations.Nullable
    java.lang.String stateProvince, @org.jetbrains.annotations.Nullable
    java.lang.String postalCode, @org.jetbrains.annotations.NotNull
    java.lang.String country, @org.jetbrains.annotations.NotNull
    java.lang.String countryCode, double latitude, double longitude, int confidenceScore, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressStatus status, boolean isVerified, @org.jetbrains.annotations.Nullable
    java.lang.Float textCoordConsistency, @org.jetbrains.annotations.Nullable
    java.lang.Float gpsGeocodeDistance, @org.jetbrains.annotations.Nullable
    java.lang.Float addressCompleteness, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressSource source, @org.jetbrains.annotations.Nullable
    java.lang.String rawInput, @org.jetbrains.annotations.Nullable
    java.lang.String geocodeProvider, @org.jetbrains.annotations.Nullable
    java.lang.String geocodePlaceId, boolean isDefault, boolean isActive, @org.jetbrains.annotations.Nullable
    java.lang.String deliveryInstructions, @org.jetbrains.annotations.Nullable
    java.lang.String referencePoint, @org.jetbrains.annotations.Nullable
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String updatedAt, @org.jetbrains.annotations.Nullable
    java.lang.String verifiedAt, @org.jetbrains.annotations.Nullable
    java.lang.String lastUsedAt) {
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
    public final java.lang.String getLabel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressType getAddressType() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "address_type")
    @java.lang.Deprecated
    public static void getAddressType$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedAddress() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "formatted_address")
    @java.lang.Deprecated
    public static void getFormattedAddress$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getStreetAddress() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "street_address")
    @java.lang.Deprecated
    public static void getStreetAddress$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getStreetNumber() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "street_number")
    @java.lang.Deprecated
    public static void getStreetNumber$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getApartment() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getFloor() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNeighborhood() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCity() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getStateProvince() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "state_province")
    @java.lang.Deprecated
    public static void getStateProvince$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPostalCode() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "postal_code")
    @java.lang.Deprecated
    public static void getPostalCode$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCountry() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCountryCode() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "country_code")
    @java.lang.Deprecated
    public static void getCountryCode$annotations() {
    }
    
    public final double getLatitude() {
        return 0.0;
    }
    
    public final double getLongitude() {
        return 0.0;
    }
    
    public final int getConfidenceScore() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "confidence_score")
    @java.lang.Deprecated
    public static void getConfidenceScore$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressStatus getStatus() {
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
    public final java.lang.Float getTextCoordConsistency() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "text_coord_consistency")
    @java.lang.Deprecated
    public static void getTextCoordConsistency$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Float getGpsGeocodeDistance() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "gps_geocode_distance")
    @java.lang.Deprecated
    public static void getGpsGeocodeDistance$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Float getAddressCompleteness() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "address_completeness")
    @java.lang.Deprecated
    public static void getAddressCompleteness$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressSource getSource() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRawInput() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "raw_input")
    @java.lang.Deprecated
    public static void getRawInput$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getGeocodeProvider() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "geocode_provider")
    @java.lang.Deprecated
    public static void getGeocodeProvider$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getGeocodePlaceId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "geocode_place_id")
    @java.lang.Deprecated
    public static void getGeocodePlaceId$annotations() {
    }
    
    public final boolean isDefault() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "is_default")
    @java.lang.Deprecated
    public static void isDefault$annotations() {
    }
    
    public final boolean isActive() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "is_active")
    @java.lang.Deprecated
    public static void isActive$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDeliveryInstructions() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "delivery_instructions")
    @java.lang.Deprecated
    public static void getDeliveryInstructions$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getReferencePoint() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "reference_point")
    @java.lang.Deprecated
    public static void getReferencePoint$annotations() {
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
    public final java.lang.String getUpdatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "updated_at")
    @java.lang.Deprecated
    public static void getUpdatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getVerifiedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "verified_at")
    @java.lang.Deprecated
    public static void getVerifiedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastUsedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "last_used_at")
    @java.lang.Deprecated
    public static void getLastUsedAt$annotations() {
    }
    
    public final boolean isValid() {
        return false;
    }
    
    public final boolean isSuspicious() {
        return false;
    }
    
    public final boolean isInvalid() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDisplayAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getShortAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCityWithPostalCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTypeIcon() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStatusText() {
        return null;
    }
    
    public Address() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
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
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component15() {
        return null;
    }
    
    public final double component16() {
        return 0.0;
    }
    
    public final double component17() {
        return 0.0;
    }
    
    public final int component18() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressStatus component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    public final boolean component20() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Float component21() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Float component22() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Float component23() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressSource component24() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component25() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component26() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component27() {
        return null;
    }
    
    public final boolean component28() {
        return false;
    }
    
    public final boolean component29() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component30() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component31() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component32() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component33() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component34() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component35() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressType component4() {
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
    public final com.rendly.app.data.model.Address copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String label, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressType addressType, @org.jetbrains.annotations.NotNull
    java.lang.String formattedAddress, @org.jetbrains.annotations.Nullable
    java.lang.String streetAddress, @org.jetbrains.annotations.Nullable
    java.lang.String streetNumber, @org.jetbrains.annotations.Nullable
    java.lang.String apartment, @org.jetbrains.annotations.Nullable
    java.lang.String floor, @org.jetbrains.annotations.Nullable
    java.lang.String neighborhood, @org.jetbrains.annotations.NotNull
    java.lang.String city, @org.jetbrains.annotations.Nullable
    java.lang.String stateProvince, @org.jetbrains.annotations.Nullable
    java.lang.String postalCode, @org.jetbrains.annotations.NotNull
    java.lang.String country, @org.jetbrains.annotations.NotNull
    java.lang.String countryCode, double latitude, double longitude, int confidenceScore, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressStatus status, boolean isVerified, @org.jetbrains.annotations.Nullable
    java.lang.Float textCoordConsistency, @org.jetbrains.annotations.Nullable
    java.lang.Float gpsGeocodeDistance, @org.jetbrains.annotations.Nullable
    java.lang.Float addressCompleteness, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressSource source, @org.jetbrains.annotations.Nullable
    java.lang.String rawInput, @org.jetbrains.annotations.Nullable
    java.lang.String geocodeProvider, @org.jetbrains.annotations.Nullable
    java.lang.String geocodePlaceId, boolean isDefault, boolean isActive, @org.jetbrains.annotations.Nullable
    java.lang.String deliveryInstructions, @org.jetbrains.annotations.Nullable
    java.lang.String referencePoint, @org.jetbrains.annotations.Nullable
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String updatedAt, @org.jetbrains.annotations.Nullable
    java.lang.String verifiedAt, @org.jetbrains.annotations.Nullable
    java.lang.String lastUsedAt) {
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
    com.rendly.app.data.model.Address self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/Address.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/Address;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.Address> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.Address.$serializer INSTANCE = null;
        
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
        public com.rendly.app.data.model.Address deserialize(@org.jetbrains.annotations.NotNull
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
        com.rendly.app.data.model.Address value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/Address$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/Address;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.Address> serializer() {
            return null;
        }
    }
}