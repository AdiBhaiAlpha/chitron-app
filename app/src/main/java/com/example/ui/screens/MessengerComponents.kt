package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatConversationEntity
import com.example.data.ChatMessageEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.ChatHeadBubbleState
import com.example.ui.theme.AppIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Animated 3-dot Messenger Typing Indicator Bubble.
 */
@Composable
fun MessengerTypingIndicatorBubble(
    senderInitial: String,
    senderName: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = senderInitial.take(1).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomEnd = 18.dp,
                bottomStart = 4.dp
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot1Scale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot2Scale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot3Scale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$senderName is typing…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Fullscreen Admin Chat Thread Screen (Messenger-style)
 * - Dedicated Back button & Android BackHandler
 * - Minimize to Floating Chat Head Bubble button
 * - Non-jumping LazyColumn anchored to bottom
 * - Animated Typing Indicator before new messages arrive
 */
@Composable
fun FullscreenAdminChatScreen(
    conversation: ChatConversationEntity,
    messages: List<ChatMessageEntity>,
    isVisitorTyping: Boolean,
    onBack: () -> Unit,
    onMinimizeToChatHead: () -> Unit,
    onSendReply: (String) -> Unit,
    onToggleArchiveStatus: () -> Unit,
    onDeleteThread: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var replyText by remember { mutableStateOf("") }
    var isLocalTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }

    // Keep scroll anchored at the latest message at the bottom without ever jumping to the top
    val totalBottomItems = messages.size + (if (isVisitorTyping) 1 else 0)
    var previousCount by remember { mutableStateOf(0) }

    LaunchedEffect(totalBottomItems, conversation.visitorId) {
        if (totalBottomItems > 0) {
            val targetIndex = (totalBottomItems - 1).coerceAtLeast(0)
            if (previousCount == 0) {
                listState.scrollToItem(targetIndex)
            } else if (totalBottomItems > previousCount) {
                listState.animateScrollToItem(targetIndex)
            }
            previousCount = totalBottomItems
        }
    }

    val initial = conversation.visitorName.trim().firstOrNull()?.uppercase() ?: "V"
    val isActive = conversation.status == "active"

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .testTag("fullscreen_admin_chat"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Messenger Top Bar with Back Navigation, Avatar, Live Status, Bubble Minimize & Actions
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to messages",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Avatar with active green dot
                        Box(modifier = Modifier.size(42.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(if (isActive) Color(0xFF2E7D32) else Color.Gray)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = conversation.visitorName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    isVisitorTyping -> "typing a message…"
                                    isActive -> "Active now • Live Chat"
                                    else -> conversation.status.replaceFirstChar { it.uppercase() }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isVisitorTyping || isActive) Color(0xFF2E7D32)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Pop out into Floating Chat Head Bubble button
                        IconButton(
                            onClick = onMinimizeToChatHead,
                            modifier = Modifier.testTag("minimize_to_bubble_button")
                        ) {
                            Icon(
                                imageVector = AppIcons.ChatBubble,
                                contentDescription = "Minimize to Chat Head Bubble",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        TextButton(onClick = onToggleArchiveStatus) {
                            Text(
                                text = if (conversation.status == "archived") "Unarchive" else "Archive",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        IconButton(
                            onClick = onDeleteThread,
                            modifier = Modifier.testTag("delete_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete conversation",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }

            // 2. Fixed, Bottom-Anchored Chat Messages Area (never scrolls up on new message!)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = messages,
                    key = { index, msg -> "${msg.timestamp}_${index}_${msg.sender}" }
                ) { _, msg ->
                    val isAdmin = msg.sender == "admin"
                    val formattedTime = remember(msg.timestamp) {
                        timeFormatter.format(Date(msg.timestamp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAdmin) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (!isAdmin) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column(
                            horizontalAlignment = if (isAdmin) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                color = if (isAdmin) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isAdmin) 18.dp else 4.dp,
                                    bottomEnd = if (isAdmin) 4.dp else 18.dp
                                ),
                                border = if (isAdmin) null else BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.widthIn(max = 285.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isAdmin) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                // Animated Typing Indicator at the bottom when visitor is sending a message
                if (isVisitorTyping) {
                    item(key = "visitor_typing_indicator") {
                        MessengerTypingIndicatorBubble(
                            senderInitial = initial,
                            senderName = conversation.visitorName
                        )
                    }
                }
            }

            // 3. Fixed Bottom Messenger Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = {
                                replyText = it
                                isLocalTyping = it.isNotBlank()
                            },
                            placeholder = { Text("Write a message…") },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_chat_input")
                        )

                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onSendReply(replyText)
                                    replyText = ""
                                    isLocalTyping = false
                                }
                            },
                            enabled = replyText.isNotBlank(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("admin_chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send reply"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Visitor Live Chat Screen (replaces cramped bottom sheet so visitors also get
 * Fullscreen Chat, Back navigation, Minimize to Chat Head, and Typing Indicator).
 */
@Composable
fun FullscreenVisitorChatScreen(
    visitorName: String,
    messages: List<ChatMessageEntity>,
    isAdminTyping: Boolean,
    lang: AppLanguage,
    onUpdateVisitorName: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onMinimizeToBubble: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler {
        onDismiss()
    }

    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }

    val totalItems = messages.size + (if (isAdminTyping) 1 else 0)
    var prevCount by remember { mutableStateOf(0) }

    LaunchedEffect(totalItems) {
        if (totalItems > 0) {
            val targetIdx = (totalItems - 1).coerceAtLeast(0)
            if (prevCount == 0) {
                listState.scrollToItem(targetIdx)
            } else if (totalItems > prevCount) {
                listState.animateScrollToItem(targetIdx)
            }
            prevCount = totalItems
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .testTag("fullscreen_visitor_chat"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with Back button, Chitron's avatar, online status, and Minimize to Bubble button
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("visitor_chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        Box(modifier = Modifier.size(40.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "C",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32))
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.t("chat.headerName", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isAdminTyping) "typing a reply…" else AppStrings.t("chat.statusText", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        IconButton(
                            onClick = onMinimizeToBubble,
                            modifier = Modifier.testTag("visitor_minimize_bubble_button")
                        ) {
                            Icon(
                                imageVector = AppIcons.ChatBubble,
                                contentDescription = "Minimize to Chat Head",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close chat")
                        }
                    }

                    // Visitor Name bar
                    OutlinedTextField(
                        value = visitorName,
                        onValueChange = onUpdateVisitorName,
                        label = { Text(AppStrings.t("chat.namePlaceholder", lang)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("visitor_name_input")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }

            // Messages list anchored to bottom
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty() && !isAdminTyping) {
                    item(key = "welcome_banner") {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = AppStrings.t("chat.welcome", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = messages,
                    key = { idx, msg -> "${msg.timestamp}_${idx}_${msg.sender}" }
                ) { _, msg ->
                    val isVisitor = msg.sender == "visitor"
                    val formattedTime = remember(msg.timestamp) {
                        timeFormatter.format(Date(msg.timestamp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isVisitor) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (!isVisitor) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "C",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column(
                            horizontalAlignment = if (isVisitor) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                color = if (isVisitor) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isVisitor) 18.dp else 4.dp,
                                    bottomEnd = if (isVisitor) 4.dp else 18.dp
                                ),
                                border = if (isVisitor) null else BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.widthIn(max = 285.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isVisitor) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                if (isAdminTyping) {
                    item(key = "admin_typing_indicator") {
                        MessengerTypingIndicatorBubble(
                            senderInitial = "C",
                            senderName = "Chitron"
                        )
                    }
                }
            }

            // Fixed Bottom Message Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = { Text(AppStrings.t("chat.inputPlaceholder", lang)) },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("visitor_message_input")
                        )
                        IconButton(
                            onClick = {
                                if (messageInput.isNotBlank()) {
                                    onSendMessage(messageInput)
                                    messageInput = ""
                                }
                            },
                            enabled = messageInput.isNotBlank(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("visitor_send_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = AppStrings.t("chat.send", lang)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Messenger-style Draggable Floating Chat Head Bubble in the corner of the screen!
 */
@Composable
fun FloatingChatHeadBubbleOverlay(
    bubbleState: ChatHeadBubbleState?,
    onOpenChat: (visitorId: String, isForAdmin: Boolean) -> Unit,
    onDismissBubble: () -> Unit,
    onDismissPreview: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = bubbleState != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        val state = bubbleState ?: return@AnimatedVisibility
        val initial = state.senderName.trim().firstOrNull()?.uppercase() ?: "C"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 88.dp, end = 12.dp, bottom = 140.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Speech Bubble Preview Card next to the Chat Head
                if (state.showPreviewBubble && state.lastMessage.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .widthIn(max = 220.dp)
                            .clickable {
                                onOpenChat(state.visitorId, state.isForAdmin)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = state.senderName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = state.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hide preview",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDismissPreview() }
                            )
                        }
                    }
                }

                // Circular Messenger Chat Head Bubble
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("floating_chat_head_bubble")
                ) {
                    // Main Avatar Circle
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 10.dp,
                        border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                            .clickable {
                                onOpenChat(state.visitorId, state.isForAdmin)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Unread Red Badge (top-left of bubble)
                    if (state.unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD32F2F),
                            border = BorderStroke(1.5.dp, Color.White),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.unreadCount.coerceAtMost(99).toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Close '×' mini badge (top-right of bubble)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clickable { onDismissBubble() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close chat head",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Online Green Dot (bottom-right of bubble)
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .padding(end = 2.dp, bottom = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32))
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }
        }
    }
}
