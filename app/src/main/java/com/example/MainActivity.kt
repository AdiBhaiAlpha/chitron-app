package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ArchiveDatabase
import com.example.data.ArchiveRepository
import com.example.data.ChatConversationEntity
import com.example.data.SiteConfigEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.AdminSubTab
import com.example.ui.ArchiveViewModel
import com.example.ui.ChatNotificationHelper
import com.example.ui.MainDestination
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminMainScreen
import com.example.ui.screens.FloatingChatHeadBubbleOverlay
import com.example.ui.screens.FullscreenAdminChatScreen
import com.example.ui.screens.FullscreenVisitorChatScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.WritingScreen
import com.example.ui.theme.AppIcons
import com.example.ui.theme.ChitronsArchiveTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val pendingNotificationIntent = MutableStateFlow<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ChatNotificationHelper.ensureNotificationChannel(applicationContext)
        pendingNotificationIntent.value = intent

        val database = ArchiveDatabase.getDatabase(applicationContext)
        val repository = ArchiveRepository(database.archiveDao())
        val prefs = getSharedPreferences("chitrons_archive_prefs", Context.MODE_PRIVATE)
        val factory = ArchiveViewModel.Factory(repository, prefs)

        setContent {
            val viewModel: ArchiveViewModel = viewModel(factory = factory)
            val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val latestIntent by pendingNotificationIntent.collectAsStateWithLifecycle()

            LaunchedEffect(latestIntent) {
                val navIntent = latestIntent ?: return@LaunchedEffect
                val visitorId = navIntent.getStringExtra(ChatNotificationHelper.EXTRA_VISITOR_ID)
                val isForAdmin = navIntent.getBooleanExtra(ChatNotificationHelper.EXTRA_IS_ADMIN, false)
                if (!visitorId.isNullOrBlank()) {
                    viewModel.openChatFromBubbleOrNotification(visitorId, isForAdmin)
                    pendingNotificationIntent.value = null
                }
            }

            ChitronsArchiveTheme(darkTheme = isDark) {
                ChitronsArchiveApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationIntent.value = intent
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChitronsArchiveApp(viewModel: ArchiveViewModel) {
    val context = LocalContext.current
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val lang by viewModel.language.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val allPosts by viewModel.allPosts.collectAsStateWithLifecycle()
    val allGallery by viewModel.allGallery.collectAsStateWithLifecycle()
    val allConversations by viewModel.allConversations.collectAsStateWithLifecycle()
    val siteConfigState by viewModel.siteConfig.collectAsStateWithLifecycle()
    val siteConfig = siteConfigState ?: SiteConfigEntity()

    val searchQuery by viewModel.writingSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.writingSelectedCategory.collectAsStateWithLifecycle()
    val selectedTag by viewModel.writingSelectedTag.collectAsStateWithLifecycle()
    val sortNewest by viewModel.writingSortNewest.collectAsStateWithLifecycle()
    val showAdminShortcutDialog by viewModel.showAdminShortcutDialog.collectAsStateWithLifecycle()

    val selectedPostId by viewModel.selectedPostId.collectAsStateWithLifecycle()
    val galleryCategory by viewModel.gallerySelectedCategory.collectAsStateWithLifecycle()
    val lightboxIndex by viewModel.lightboxPhotoIndex.collectAsStateWithLifecycle()

    val isVisitorChatOpen by viewModel.isVisitorChatOpen.collectAsStateWithLifecycle()
    val visitorName by viewModel.visitorName.collectAsStateWithLifecycle()
    val visitorMessages by viewModel.visitorMessages.collectAsStateWithLifecycle()
    val isAdminTypingForVisitor by viewModel.isAdminTypingForVisitor.collectAsStateWithLifecycle()

    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val adminLoginError by viewModel.adminLoginError.collectAsStateWithLifecycle()
    val adminSubTab by viewModel.adminSubTab.collectAsStateWithLifecycle()
    val adminPostFilter by viewModel.adminPostFilter.collectAsStateWithLifecycle()
    val adminChatFilter by viewModel.adminChatFilter.collectAsStateWithLifecycle()
    val adminSelectedVisitorId by viewModel.adminSelectedVisitorId.collectAsStateWithLifecycle()
    val adminActiveMessages by viewModel.adminActiveMessages.collectAsStateWithLifecycle()
    val typingVisitorId by viewModel.typingVisitorId.collectAsStateWithLifecycle()
    val chatHeadBubble by viewModel.chatHeadBubble.collectAsStateWithLifecycle()
    val editingRevisions by viewModel.editingPostRevisions.collectAsStateWithLifecycle()

    // Request POST_NOTIFICATIONS runtime permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Play Messenger sound & post Android Notification when new message arrives
    LaunchedEffect(viewModel) {
        viewModel.incomingMessageEvents.collect { event ->
            ChatNotificationHelper.playMessageSound(context)
            ChatNotificationHelper.showIncomingMessageNotification(
                context = context,
                visitorId = event.visitorId,
                senderName = event.senderName,
                messageText = event.messageText,
                isForAdmin = event.isForAdmin
            )
        }
    }

    // Check if Fullscreen Visitor Chat or Fullscreen Admin Chat is currently open
    val isFullscreenAdminChatOpen = isAdminAuthenticated &&
        currentDestination == MainDestination.ADMIN &&
        adminSubTab == AdminSubTab.MESSAGES &&
        !adminSelectedVisitorId.isNullOrBlank()

    if (isVisitorChatOpen) {
        FullscreenVisitorChatScreen(
            visitorName = visitorName,
            messages = visitorMessages,
            isAdminTyping = isAdminTypingForVisitor,
            lang = lang,
            onUpdateVisitorName = viewModel::updateVisitorName,
            onSendMessage = viewModel::sendVisitorMessage,
            onMinimizeToBubble = viewModel::minimizeVisitorChatToBubble,
            onDismiss = { viewModel.setVisitorChatOpen(false) }
        )
        return
    }

    if (isFullscreenAdminChatOpen) {
        val activeVid = adminSelectedVisitorId.orEmpty()
        val activeConv = allConversations.find { it.visitorId == activeVid }
            ?: ChatConversationEntity(
                visitorId = activeVid,
                visitorName = "Visitor (${activeVid.takeLast(6)})",
                lastMessage = "",
                lastMessageAt = System.currentTimeMillis(),
                lastSender = "visitor",
                unreadForAdmin = 0,
                status = "active"
            )
        FullscreenAdminChatScreen(
            conversation = activeConv,
            messages = adminActiveMessages,
            isVisitorTyping = typingVisitorId == activeVid,
            onBack = viewModel::closeAdminConversation,
            onMinimizeToChatHead = { viewModel.minimizeAdminConversationToChatHead(activeVid) },
            onSendReply = { text -> viewModel.sendAdminReply(activeVid, text) },
            onToggleArchiveStatus = {
                val nextStatus = if (activeConv.status == "archived") "active" else "archived"
                viewModel.updateConversationStatus(activeVid, nextStatus)
            },
            onDeleteThread = { viewModel.deleteConversation(activeVid) }
        )
        return
    }

    if (currentDestination != MainDestination.HOME && currentDestination != MainDestination.POST_DETAIL) {
        BackHandler {
            viewModel.navigateTo(MainDestination.HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = siteConfig.siteName.ifBlank { "Chitron's Archive" },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(MainDestination.HOME) }
                                .testTag("top_bar_logo")
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.syncNow() },
                            modifier = Modifier.testTag("sync_refresh_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = AppIcons.Sync,
                                    contentDescription = "Sync with live server"
                                )
                            }
                        }

                        // Bilingual EN / বাং Toggle Pill
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.toggleLanguage() }
                                .testTag("lang_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "EN",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (lang == AppLanguage.EN) FontWeight.Bold else FontWeight.Normal,
                                    color = if (lang == AppLanguage.EN) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "/",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "বাং",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (lang == AppLanguage.BN) FontWeight.Bold else FontWeight.Normal,
                                    color = if (lang == AppLanguage.BN) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { viewModel.toggleDarkTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDark) AppIcons.LightMode else AppIcons.DarkMode,
                                contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                if (currentDestination != MainDestination.ADMIN) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.setVisitorChatOpen(true) },
                        icon = {
                            Icon(AppIcons.ChatBubble, contentDescription = null)
                        },
                        text = {
                            Text(AppStrings.t("chat.fabLabel", lang))
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("message_me_fab")
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.HOME,
                        onClick = { viewModel.navigateTo(MainDestination.HOME) },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = AppStrings.t("nav.home", lang)
                            )
                        },
                        label = { Text(AppStrings.t("nav.home", lang)) },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.WRITING || currentDestination == MainDestination.POST_DETAIL,
                        onClick = { viewModel.navigateTo(MainDestination.WRITING) },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.WRITING || currentDestination == MainDestination.POST_DETAIL) AppIcons.ArticleFilled else AppIcons.ArticleOutlined,
                                contentDescription = AppStrings.t("nav.writing", lang)
                            )
                        },
                        label = { Text(AppStrings.t("nav.writing", lang)) },
                        modifier = Modifier.testTag("nav_writing")
                    )
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.GALLERY,
                        onClick = { viewModel.navigateTo(MainDestination.GALLERY) },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.GALLERY) AppIcons.PhotoLibraryFilled else AppIcons.PhotoLibraryOutlined,
                                contentDescription = AppStrings.t("nav.gallery", lang)
                            )
                        },
                        label = { Text(AppStrings.t("nav.gallery", lang)) },
                        modifier = Modifier.testTag("nav_gallery")
                    )
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.ABOUT,
                        onClick = { viewModel.navigateTo(MainDestination.ABOUT) },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.ABOUT) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = AppStrings.t("nav.about", lang)
                            )
                        },
                        label = { Text(AppStrings.t("nav.about", lang)) },
                        modifier = Modifier.testTag("nav_about")
                    )
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.ADMIN,
                        onClick = { viewModel.navigateTo(MainDestination.ADMIN) },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.ADMIN) AppIcons.AdminFilled else AppIcons.AdminOutlined,
                                contentDescription = AppStrings.t("nav.admin", lang)
                            )
                        },
                        label = { Text(AppStrings.t("nav.admin", lang)) },
                        modifier = Modifier.testTag("nav_admin")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    MainDestination.HOME -> {
                        HomeScreen(
                            posts = allPosts,
                            siteConfig = siteConfig,
                            lang = lang,
                            onReadWritingClick = { viewModel.navigateTo(MainDestination.WRITING) },
                            onAboutClick = { viewModel.navigateTo(MainDestination.ABOUT) },
                            onPostClick = { postId -> viewModel.openPostDetail(postId) },
                            onCategoryClick = { category -> viewModel.openWritingWithCategory(category) }
                        )
                    }

                    MainDestination.WRITING -> {
                        WritingScreen(
                            posts = allPosts,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            selectedTag = selectedTag,
                            sortNewest = sortNewest,
                            showAdminShortcutModal = showAdminShortcutDialog,
                            lang = lang,
                            onSearchQueryChange = viewModel::setWritingSearchQuery,
                            onCategorySelect = viewModel::setWritingCategory,
                            onTagSelect = viewModel::setWritingTag,
                            onSortToggle = viewModel::setWritingSortNewest,
                            onPostClick = viewModel::openPostDetail,
                            onDismissAdminShortcut = viewModel::dismissAdminShortcutDialog,
                            onConfirmAdminShortcut = viewModel::confirmAdminShortcut
                        )
                    }

                    MainDestination.POST_DETAIL -> {
                        val activePost = allPosts.find { it.id == selectedPostId }
                        PostDetailScreen(
                            post = activePost,
                            allPublishedPosts = allPosts,
                            lang = lang,
                            onBack = viewModel::navigateBackFromPostDetail,
                            onSelectPost = viewModel::openPostDetail,
                            onTagClick = viewModel::openWritingWithTag
                        )
                    }

                    MainDestination.GALLERY -> {
                        GalleryScreen(
                            galleryItems = allGallery,
                            selectedCategory = galleryCategory,
                            lightboxIndex = lightboxIndex,
                            lang = lang,
                            onSelectCategory = viewModel::setGalleryCategory,
                            onOpenLightbox = viewModel::openLightbox,
                            onCloseLightbox = viewModel::closeLightbox
                        )
                    }

                    MainDestination.ABOUT -> {
                        AboutScreen(
                            siteConfig = siteConfig,
                            lang = lang,
                            onReadWritingClick = { viewModel.navigateTo(MainDestination.WRITING) }
                        )
                    }

                    MainDestination.ADMIN -> {
                        AdminMainScreen(
                            isAuthenticated = isAdminAuthenticated,
                            loginError = adminLoginError,
                            activeSubTab = adminSubTab,
                            postFilter = adminPostFilter,
                            chatFilter = adminChatFilter,
                            selectedVisitorId = adminSelectedVisitorId,
                            allPosts = allPosts,
                            allGallery = allGallery,
                            allConversations = allConversations,
                            activeMessages = adminActiveMessages,
                            editingRevisions = editingRevisions,
                            siteConfig = siteConfig,
                            lang = lang,
                            onVerifyPin = viewModel::verifyAdminPin,
                            onLogout = viewModel::logoutAdmin,
                            onSelectSubTab = viewModel::setAdminSubTab,
                            onSelectPostFilter = viewModel::setAdminPostFilter,
                            onSavePost = viewModel::saveOrUpdatePost,
                            onDuplicatePost = viewModel::duplicatePost,
                            onUpdatePostStatus = viewModel::updatePostStatus,
                            onDeletePostPermanently = viewModel::deletePostPermanently,
                            onLoadRevisionsForPost = viewModel::setEditingPostForRevisions,
                            onRestoreRevision = viewModel::restoreRevision,
                            onSelectChatFilter = viewModel::setAdminChatFilter,
                            onSelectConversation = viewModel::selectAdminConversation,
                            onSendAdminReply = viewModel::sendAdminReply,
                            onUpdateConversationStatus = viewModel::updateConversationStatus,
                            onDeleteConversation = viewModel::deleteConversation,
                            onAddGalleryPhoto = viewModel::addGalleryPhoto,
                            onDeleteGalleryPhoto = viewModel::deleteGalleryPhoto,
                            onSaveSiteConfig = viewModel::saveSiteConfig
                        )
                    }
                }
            }
        }

        // Messenger-style Floating Chat Head Bubble in the corner of the screen
        FloatingChatHeadBubbleOverlay(
            bubbleState = chatHeadBubble,
            onOpenChat = viewModel::openChatFromBubbleOrNotification,
            onDismissBubble = viewModel::dismissChatHeadBubble,
            onDismissPreview = viewModel::dismissChatHeadPreviewToast
        )
    }
}
