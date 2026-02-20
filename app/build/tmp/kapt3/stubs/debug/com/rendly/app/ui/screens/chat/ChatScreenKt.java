package com.rendly.app.ui.screens.chat;

import androidx.compose.animation.*;
import androidx.compose.ui.unit.Dp;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.ui.input.pointer.PointerEventPass;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.core.content.ContextCompat;
import java.io.File;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.repository.ChatRepository;
import com.rendly.app.data.repository.Message;
import com.rendly.app.data.repository.MessageStatus;
import com.rendly.app.data.repository.FollowersRepository;
import com.rendly.app.data.repository.HandshakeRepository;
import com.rendly.app.data.repository.ReputationRepository;
import com.rendly.app.data.repository.VerificationRepository;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.data.repository.PostRepository;
import com.rendly.app.data.model.*;
import com.rendly.app.ui.components.ClientRequestMessageBubble;
import com.rendly.app.ui.components.VerifiedBadge;
import com.rendly.app.ui.components.HandshakeProposalModal;
import com.rendly.app.ui.components.HandshakeActiveBanner;
import com.rendly.app.ui.components.HandshakeBannerState;
import com.rendly.app.ui.components.CancelConfirmationModal;
import com.rendly.app.R;
import androidx.compose.ui.window.DialogProperties;
import com.rendly.app.ui.theme.*;
import androidx.compose.animation.core.*;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.foundation.lazy.grid.GridCells;
import com.rendly.app.data.repository.CallRepository;
import com.rendly.app.data.model.CallStatus;
import androidx.compose.ui.focus.FocusRequester;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.ui.text.input.ImeAction;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u00a2\u0001\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0018\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010 \n\u0002\u0010\u0007\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0010\u001a:\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a^\u0010\u000b\u001a\u00020\u00012\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a8\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0019\u0010\u001a\u001aH\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u001d\u001a\u00020\u00172\u0006\u0010\u001e\u001a\u00020\u00172\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010 \u001a\u00020\u0003H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b!\u0010\"\u001an\u0010#\u001a\u00020\u00012\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\u00052\u0006\u0010\'\u001a\u00020\u00052\b\b\u0002\u0010(\u001a\u00020)2\b\b\u0002\u0010*\u001a\u00020\u00052\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u000e\b\u0002\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a\u0092\u0001\u0010/\u001a\u00020\u00012\u0006\u00100\u001a\u00020\u00032\u0012\u00101\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001022\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u00104\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0012\u00108\u001a\u000e\u0012\u0004\u0012\u000209\u0012\u0004\u0012\u00020\u0001022\b\b\u0002\u0010:\u001a\u00020\u00052\b\b\u0002\u0010;\u001a\u00020\u0005H\u0003\u001at\u0010<\u001a\u00020\u00012\u0006\u0010=\u001a\u00020%2\n\b\u0002\u0010>\u001a\u0004\u0018\u00010\u00032\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u000e\b\u0002\u0010?\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0016\b\u0002\u0010@\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u0001022\u0016\b\u0002\u0010A\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u0001022\b\b\u0002\u0010B\u001a\u00020CH\u0007\u001at\u0010D\u001a\u00020\u00012\u0006\u0010E\u001a\u00020\u00032\u0012\u0010F\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001022\u0006\u0010G\u001a\u00020H2\u0006\u0010I\u001a\u00020H2\u0006\u0010J\u001a\u00020\u00052\f\u0010K\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010L\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010M\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010N\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a<\u0010O\u001a\u00020\u00012\u0006\u0010>\u001a\u00020\u00032\u0006\u0010P\u001a\u00020\u00032\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0014\b\u0002\u0010Q\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u000102H\u0003\u001a\u0098\u0001\u0010R\u001a\u00020\u00012\u0006\u0010S\u001a\u00020\u00052\u0006\u0010=\u001a\u00020%2\u0006\u0010>\u001a\u00020\u00032\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0014\b\u0002\u0010T\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0001022\u000e\b\u0002\u0010U\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u000e\b\u0002\u0010V\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u000e\b\u0002\u0010W\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0014\b\u0002\u0010X\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u000102H\u0003\u001a@\u0010Y\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010Z\u001a\u00020\u00032\u0006\u0010[\u001a\u00020\u00032\u0006\u0010\\\u001a\u00020\u00172\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b]\u0010^\u001a\"\u0010_\u001a\u00020\u00012\u0006\u0010`\u001a\u00020a2\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0003H\u0003\u001a^\u0010b\u001a\u00020\u00012\u0006\u0010S\u001a\u00020\u00052\u0006\u0010c\u001a\u00020d2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n26\u0010e\u001a2\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\bg\u0012\b\bh\u0012\u0004\b\b(i\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\bg\u0012\b\bh\u0012\u0004\b\b(j\u0012\u0004\u0012\u00020\u00010fH\u0003\u001a^\u0010k\u001a\u00020\u00012\u0006\u0010S\u001a\u00020\u00052\u0006\u0010=\u001a\u00020%2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n26\u0010l\u001a2\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\bg\u0012\b\bh\u0012\u0004\b\b(m\u0012\u0013\u0012\u00110)\u00a2\u0006\f\bg\u0012\b\bh\u0012\u0004\b\b(n\u0012\u0004\u0012\u00020\u00010fH\u0003\u001a\u0084\u0001\u0010o\u001a\u00020\u00012\u0006\u0010p\u001a\u00020q2\b\u0010r\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010s\u001a\u0004\u0018\u00010\u00032\u0014\b\u0002\u0010t\u001a\u000e\u0012\u0004\u0012\u00020q\u0012\u0004\u0012\u00020\u0001022\u0016\b\u0002\u0010u\u001a\u0010\u0012\u0004\u0012\u00020d\u0012\u0004\u0012\u00020\u0001\u0018\u0001022\u0016\b\u0002\u0010v\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u0001022\u0016\b\u0002\u0010A\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u000102H\u0003\u001a0\u0010w\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00032\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\b\b\u0002\u0010x\u001a\u00020\u0005H\u0003\u001aj\u0010y\u001a\u00020\u00012\u0006\u0010p\u001a\u00020q2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0012\u0010z\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001022\f\u0010{\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010e\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010|\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010}\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a\'\u0010~\u001a\u00020\u00012\u0006\u0010\u007f\u001a\u00020\b2\t\b\u0002\u0010\u0080\u0001\u001a\u00020\u0017H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0006\b\u0081\u0001\u0010\u0082\u0001\u001aW\u0010\u0083\u0001\u001a\u00020\u00012\u0007\u0010\u0084\u0001\u001a\u00020H2\u0007\u0010\u0085\u0001\u001a\u00020\u00052\u000f\u0010\u0086\u0001\u001a\n\u0012\u0005\u0012\u00030\u0088\u00010\u0087\u00012\f\u0010}\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\r\u0010\u0089\u0001\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a\u0086\u0001\u0010\u008a\u0001\u001a\u00020\u00012\u000f\u0010\u008b\u0001\u001a\n\u0012\u0005\u0012\u00030\u0088\u00010\u0087\u00012\b\u0010\u008c\u0001\u001a\u00030\u0088\u00012\u0006\u0010\u001d\u001a\u00020\u00172\u0007\u0010\u008d\u0001\u001a\u00020\u00052\r\u0010\u008e\u0001\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0014\u0010\u008f\u0001\u001a\u000f\u0012\u0005\u0012\u00030\u0088\u0001\u0012\u0004\u0012\u00020\u0001022\u0014\u0010\u0090\u0001\u001a\u000f\u0012\u0005\u0012\u00030\u0088\u0001\u0012\u0004\u0012\u00020\u0001022\b\b\u0002\u0010B\u001a\u00020CH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0006\b\u0091\u0001\u0010\u0092\u0001\u001a!\u0010\u0093\u0001\u001a\u00020\u00012\b\u0010\u0094\u0001\u001a\u00030\u0095\u00012\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001aQ\u0010\u0096\u0001\u001a\u00020\u00012\u0006\u0010S\u001a\u00020\u00052\u0006\u0010s\u001a\u00020\u00032\t\b\u0002\u0010\u0097\u0001\u001a\u00020\u00032\b\b\u0002\u0010P\u001a\u00020\u00032\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0013\u0010\u0011\u001a\u000f\u0012\u0005\u0012\u00030\u0095\u0001\u0012\u0004\u0012\u00020\u000102H\u0003\u001a(\u0010\u0098\u0001\u001a\u00020\u00012\u0006\u0010$\u001a\u00020%2\u0007\u0010\u0099\u0001\u001a\u00020\u00052\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a<\u0010\u009a\u0001\u001a\u00020\u00012\u0006\u0010S\u001a\u00020\u00052\u0006\u0010s\u001a\u00020\u00032\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0013\u0010\u009b\u0001\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u000102H\u0003\u001a/\u0010\u009c\u0001\u001a\u00020\u00012\u0006\u0010`\u001a\u00020d2\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u0003H\u0003\u001a=\u0010\u009d\u0001\u001a\u00020\u00012\u0007\u0010`\u001a\u00030\u009e\u00012\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\u000f\b\u0002\u0010\u009f\u0001\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a\t\u0010\u00a0\u0001\u001a\u00020\u0001H\u0003\u001a\u0014\u0010\u00a1\u0001\u001a\u00020\u00012\t\u0010\u00a2\u0001\u001a\u0004\u0018\u00010\u0003H\u0003\u001aU\u0010\u00a3\u0001\u001a\u00020\u00012\u0007\u0010\u0084\u0001\u001a\u00020H2\u0007\u0010\u00a4\u0001\u001a\u00020\u00052\u0007\u0010\u00a5\u0001\u001a\u00020\u00052\b\u0010\u00a6\u0001\u001a\u00030\u0088\u00012\b\u0010\u00a7\u0001\u001a\u00030\u0088\u00012\r\u0010\u00a8\u0001\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a\u0011\u0010\u00a9\u0001\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0003H\u0002\u001a\u0012\u0010\u00aa\u0001\u001a\u00020\u00032\u0007\u0010\u00ab\u0001\u001a\u00020\u0003H\u0002\u001a\u0012\u0010\u00ac\u0001\u001a\u00020\u00032\u0007\u0010\u00ad\u0001\u001a\u00020HH\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u00ae\u0001"}, d2 = {"ArticleCardMessageContent", "", "postId", "", "isFromMe", "", "timestamp", "messageStatus", "Lcom/rendly/app/data/repository/MessageStatus;", "onOpenArticle", "Lkotlin/Function0;", "AttachmentMenu", "onDismiss", "onSelectImage", "onSelectFile", "onSelectLocation", "onSelectContact", "onSelectArticle", "AttachmentOption", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "label", "color", "Landroidx/compose/ui/graphics/Color;", "onClick", "AttachmentOption-9LQNqLg", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;JLkotlin/jvm/functions/Function0;)V", "AudioMessagePlayer", "audioUrl", "accentColor", "timeColor", "senderAvatar", "sentTime", "AudioMessagePlayer-9z6LAg8", "(Ljava/lang/String;ZJJLjava/lang/String;Ljava/lang/String;)V", "ChatHeader", "user", "Lcom/rendly/app/data/model/Usuario;", "isOnline", "isTyping", "otherUserReputation", "", "isVerified", "onBack", "onCall", "onMore", "onNavigateToProfile", "ChatInputV2", "text", "onTextChange", "Lkotlin/Function1;", "onSend", "onAttachmentClick", "onCameraClick", "onHandshakeClick", "onVoiceRecord", "onAudioRecorded", "Ljava/io/File;", "isUploading", "isRecording", "ChatScreen", "otherUser", "conversationId", "onOpenChatList", "onOpenProduct", "onNavigateToUserProfile", "modifier", "Landroidx/compose/ui/Modifier;", "ChatSearchHeader", "query", "onQueryChange", "resultCount", "", "currentIndex", "isSearching", "onClose", "onSearch", "onPrevious", "onNext", "ChatSearchScreen", "otherUsername", "onMessageSelected", "ChatSettingsModal", "isVisible", "onBlockStateChanged", "onReportSent", "onChatCleared", "onSearchInChat", "onScrollToMessage", "ChatSettingsOption", "title", "subtitle", "iconColor", "ChatSettingsOption-42QJj7c", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;JLkotlin/jvm/functions/Function0;)V", "ConsultPostMessageContent", "data", "Lcom/rendly/app/ui/screens/chat/ConsultPostData;", "ForwardSharedPostModal", "postData", "Lcom/rendly/app/ui/screens/chat/SharedPostData;", "onForward", "Lkotlin/Function2;", "Lkotlin/ParameterName;", "name", "targetUserId", "customMessage", "HandshakeConfirmationModal", "onConfirm", "productDescription", "agreedPrice", "MessageBubble", "message", "Lcom/rendly/app/data/repository/Message;", "otherUserAvatar", "currentUserId", "onLongPress", "onForwardSharedPost", "onSharedPostClick", "MessageOptionItem", "isDestructive", "MessageOptionsModal", "onReaction", "onEdit", "onCopy", "onDelete", "MessageStatusIcon", "status", "tintColor", "MessageStatusIcon-4WTKRHQ", "(Lcom/rendly/app/data/repository/MessageStatus;J)V", "ProfessionalRecordingFooter", "recordingTime", "isPaused", "waveformData", "", "", "onPauseResume", "SeekableWaveform", "waveformBars", "progress", "isPlaying", "onSeekStart", "onSeek", "onSeekEnd", "SeekableWaveform-T042LqI", "(Ljava/util/List;FJZLkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;)V", "ShareArticleGridItem", "post", "Lcom/rendly/app/data/model/Post;", "ShareArticleModal", "otherUserId", "ShareUserGridItem", "isSelected", "ShareUserModal", "onShareUser", "SharedPostMessageContent", "SharedUserMessageContent", "Lcom/rendly/app/ui/screens/chat/SharedUserData;", "onViewProfile", "TypingDotsAnimation", "TypingIndicatorBubble", "userAvatar", "VoiceRecordingFooter", "isLocked", "isCancelling", "cancelProgress", "alpha", "onCancel", "formatMessageTime", "formatMessageTimeWithAmPm", "isoTimestamp", "formatSharedUserStat", "count", "app_debug"})
public final class ChatScreenKt {
    
    @androidx.compose.runtime.Composable
    public static final void ChatScreen(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.Usuario otherUser, @org.jetbrains.annotations.Nullable
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onOpenChatList, @org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onOpenProduct, @org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToUserProfile, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ChatHeader(com.rendly.app.data.model.Usuario user, boolean isOnline, boolean isTyping, double otherUserReputation, boolean isVerified, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onCall, kotlin.jvm.functions.Function0<kotlin.Unit> onMore, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToProfile) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ChatSearchHeader(java.lang.String query, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQueryChange, int resultCount, int currentIndex, boolean isSearching, kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function0<kotlin.Unit> onSearch, kotlin.jvm.functions.Function0<kotlin.Unit> onPrevious, kotlin.jvm.functions.Function0<kotlin.Unit> onNext) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void MessageBubble(com.rendly.app.data.repository.Message message, java.lang.String otherUserAvatar, java.lang.String currentUserId, kotlin.jvm.functions.Function1<? super com.rendly.app.data.repository.Message, kotlin.Unit> onLongPress, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.screens.chat.SharedPostData, kotlin.Unit> onForwardSharedPost, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSharedPostClick, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToUserProfile) {
    }
    
    private static final java.lang.String formatMessageTimeWithAmPm(java.lang.String isoTimestamp) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void TypingIndicatorBubble(java.lang.String userAvatar) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TypingDotsAnimation() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ChatInputV2(java.lang.String text, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTextChange, kotlin.jvm.functions.Function0<kotlin.Unit> onSend, kotlin.jvm.functions.Function0<kotlin.Unit> onAttachmentClick, kotlin.jvm.functions.Function0<kotlin.Unit> onCameraClick, kotlin.jvm.functions.Function0<kotlin.Unit> onHandshakeClick, kotlin.jvm.functions.Function0<kotlin.Unit> onVoiceRecord, kotlin.jvm.functions.Function1<? super java.io.File, kotlin.Unit> onAudioRecorded, boolean isUploading, boolean isRecording) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfessionalRecordingFooter(int recordingTime, boolean isPaused, java.util.List<java.lang.Float> waveformData, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, kotlin.jvm.functions.Function0<kotlin.Unit> onPauseResume, kotlin.jvm.functions.Function0<kotlin.Unit> onSend) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void VoiceRecordingFooter(int recordingTime, boolean isLocked, boolean isCancelling, float cancelProgress, float alpha, kotlin.jvm.functions.Function0<kotlin.Unit> onCancel, kotlin.jvm.functions.Function0<kotlin.Unit> onSend) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AttachmentMenu(kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onSelectImage, kotlin.jvm.functions.Function0<kotlin.Unit> onSelectFile, kotlin.jvm.functions.Function0<kotlin.Unit> onSelectLocation, kotlin.jvm.functions.Function0<kotlin.Unit> onSelectContact, kotlin.jvm.functions.Function0<kotlin.Unit> onSelectArticle) {
    }
    
    private static final java.lang.String formatMessageTime(java.lang.String timestamp) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void MessageOptionsModal(com.rendly.app.data.repository.Message message, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onReaction, kotlin.jvm.functions.Function0<kotlin.Unit> onEdit, kotlin.jvm.functions.Function0<kotlin.Unit> onForward, kotlin.jvm.functions.Function0<kotlin.Unit> onCopy, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void MessageOptionItem(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, boolean isDestructive) {
    }
    
    /**
     * Componente visual para posts compartidos en el chat
     * Diseño: header (username izq + precio der), imagen, mensaje personalizado, hora
     */
    @androidx.compose.runtime.Composable
    private static final void SharedPostMessageContent(com.rendly.app.ui.screens.chat.SharedPostData data, boolean isFromMe, java.lang.String timestamp, java.lang.String senderAvatar) {
    }
    
    /**
     * Modal para reenviar un post compartido a otro usuario
     */
    @androidx.compose.runtime.Composable
    private static final void ForwardSharedPostModal(boolean isVisible, com.rendly.app.ui.screens.chat.SharedPostData postData, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onForward) {
    }
    
    /**
     * Modal de Handshake para confirmar compra/venta en persona
     * Shadow con fade independiente + Panel con slide-up/down fluido
     */
    @androidx.compose.runtime.Composable
    private static final void HandshakeConfirmationModal(boolean isVisible, com.rendly.app.data.model.Usuario otherUser, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Double, kotlin.Unit> onConfirm) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ChatSettingsModal(boolean isVisible, com.rendly.app.data.model.Usuario otherUser, java.lang.String conversationId, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onBlockStateChanged, kotlin.jvm.functions.Function0<kotlin.Unit> onReportSent, kotlin.jvm.functions.Function0<kotlin.Unit> onChatCleared, kotlin.jvm.functions.Function0<kotlin.Unit> onSearchInChat, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onScrollToMessage) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ChatSearchScreen(java.lang.String conversationId, java.lang.String otherUsername, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onMessageSelected) {
    }
    
    /**
     * Componente visual para consultas y ofertas de productos en el chat
     * Diseño profesional con imagen del producto, título, precio y mensaje
     */
    @androidx.compose.runtime.Composable
    private static final void ConsultPostMessageContent(com.rendly.app.ui.screens.chat.ConsultPostData data, boolean isFromMe, java.lang.String timestamp) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ShareUserModal(boolean isVisible, java.lang.String currentUserId, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Usuario, kotlin.Unit> onShareUser) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ShareUserGridItem(com.rendly.app.data.model.Usuario user, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SharedUserMessageContent(com.rendly.app.ui.screens.chat.SharedUserData data, boolean isFromMe, java.lang.String timestamp, com.rendly.app.data.repository.MessageStatus messageStatus, kotlin.jvm.functions.Function0<kotlin.Unit> onViewProfile) {
    }
    
    private static final java.lang.String formatSharedUserStat(int count) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void ArticleCardMessageContent(java.lang.String postId, boolean isFromMe, java.lang.String timestamp, com.rendly.app.data.repository.MessageStatus messageStatus, kotlin.jvm.functions.Function0<kotlin.Unit> onOpenArticle) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ShareArticleModal(boolean isVisible, java.lang.String currentUserId, java.lang.String otherUserId, java.lang.String otherUsername, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onSelectArticle) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ShareArticleGridItem(com.rendly.app.data.model.Post post, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}