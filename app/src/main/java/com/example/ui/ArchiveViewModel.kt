package com.example.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.ArchiveRemoteClient
import com.example.data.ArchiveRepository
import com.example.data.BlogPostEntity
import com.example.data.ChatConversationEntity
import com.example.data.ChatMessageEntity
import com.example.data.GalleryItemEntity
import com.example.data.PostRevisionEntity
import com.example.data.SiteConfigEntity
import com.example.i18n.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class MainDestination {
    HOME,
    WRITING,
    GALLERY,
    ABOUT,
    ADMIN,
    POST_DETAIL
}

enum class AdminSubTab {
    DASHBOARD,
    POSTS,
    MESSAGES,
    GALLERY,
    SITE_CONTENT
}

data class ChatHeadBubbleState(
    val visitorId: String,
    val senderName: String,
    val lastMessage: String,
    val unreadCount: Int,
    val isForAdmin: Boolean,
    val showPreviewBubble: Boolean = true
)

data class IncomingMessageEvent(
    val visitorId: String,
    val senderName: String,
    val messageText: String,
    val isForAdmin: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModel(
    private val repository: ArchiveRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _currentDestination = MutableStateFlow(MainDestination.HOME)
    val currentDestination: StateFlow<MainDestination> = _currentDestination.asStateFlow()

    private val _previousDestination = MutableStateFlow(MainDestination.HOME)

    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Writing filters
    private val _writingSearchQuery = MutableStateFlow("")
    val writingSearchQuery: StateFlow<String> = _writingSearchQuery.asStateFlow()

    private val _writingSelectedCategory = MutableStateFlow("")
    val writingSelectedCategory: StateFlow<String> = _writingSelectedCategory.asStateFlow()

    private val _writingSelectedTag = MutableStateFlow("")
    val writingSelectedTag: StateFlow<String> = _writingSelectedTag.asStateFlow()

    private val _writingSortNewest = MutableStateFlow(true)
    val writingSortNewest: StateFlow<Boolean> = _writingSortNewest.asStateFlow()

    private val _showAdminShortcutDialog = MutableStateFlow(false)
    val showAdminShortcutDialog: StateFlow<Boolean> = _showAdminShortcutDialog.asStateFlow()

    // Active post detail
    private val _selectedPostId = MutableStateFlow<Long?>(null)
    val selectedPostId: StateFlow<Long?> = _selectedPostId.asStateFlow()

    // Gallery filter & Lightbox
    private val _gallerySelectedCategory = MutableStateFlow("all")
    val gallerySelectedCategory: StateFlow<String> = _gallerySelectedCategory.asStateFlow()

    private val _lightboxPhotoIndex = MutableStateFlow<Int?>(null)
    val lightboxPhotoIndex: StateFlow<Int?> = _lightboxPhotoIndex.asStateFlow()

    // Visitor Direct Message Sheet (persistent visitorId matching website format)
    private val _isVisitorChatOpen = MutableStateFlow(false)
    val isVisitorChatOpen: StateFlow<Boolean> = _isVisitorChatOpen.asStateFlow()

    private val initialVisitorId: String = run {
        val saved = prefs.getString("visitor_id", null)
        if (!saved.isNullOrBlank()) {
            saved
        } else {
            val generated = "visitor_" + UUID.randomUUID().toString().replace("-", "").take(20)
            prefs.edit().putString("visitor_id", generated).apply()
            generated
        }
    }

    private val _visitorSessionId = MutableStateFlow(initialVisitorId)
    val visitorSessionId: StateFlow<String> = _visitorSessionId.asStateFlow()

    private val _visitorName = MutableStateFlow(prefs.getString("visitor_name", "") ?: "")
    val visitorName: StateFlow<String> = _visitorName.asStateFlow()

    // Admin CMS State
    private val _adminToken = MutableStateFlow<String?>(prefs.getString("admin_token", null))
    private val _isAdminAuthenticated = MutableStateFlow(!_adminToken.value.isNullOrBlank())
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _adminLoginError = MutableStateFlow<String?>(null)
    val adminLoginError: StateFlow<String?> = _adminLoginError.asStateFlow()

    private val _adminSubTab = MutableStateFlow(AdminSubTab.DASHBOARD)
    val adminSubTab: StateFlow<AdminSubTab> = _adminSubTab.asStateFlow()

    private val _adminPostFilter = MutableStateFlow("")
    val adminPostFilter: StateFlow<String> = _adminPostFilter.asStateFlow()

    private val _adminChatFilter = MutableStateFlow("all")
    val adminChatFilter: StateFlow<String> = _adminChatFilter.asStateFlow()

    // Null means Admin is on the Messages Inbox List; non-null means Fullscreen Chat is open for that visitorId
    private val _adminSelectedVisitorId = MutableStateFlow<String?>(null)
    val adminSelectedVisitorId: StateFlow<String?> = _adminSelectedVisitorId.asStateFlow()

    // Typing Indicators
    private val _typingVisitorId = MutableStateFlow<String?>(null)
    val typingVisitorId: StateFlow<String?> = _typingVisitorId.asStateFlow()

    private val _isAdminTypingForVisitor = MutableStateFlow(false)
    val isAdminTypingForVisitor: StateFlow<Boolean> = _isAdminTypingForVisitor.asStateFlow()

    // Floating Messenger Chat Head Bubble State
    private val _chatHeadBubble = MutableStateFlow<ChatHeadBubbleState?>(null)
    val chatHeadBubble: StateFlow<ChatHeadBubbleState?> = _chatHeadBubble.asStateFlow()

    // One-shot Incoming Message Events (for sound + Android system notification)
    private val _incomingMessageEvents = MutableSharedFlow<IncomingMessageEvent>(extraBufferCapacity = 8)
    val incomingMessageEvents: SharedFlow<IncomingMessageEvent> = _incomingMessageEvents.asSharedFlow()

    private val _editingPostId = MutableStateFlow<Long?>(null)

    // Database Flows
    val allPosts: StateFlow<List<BlogPostEntity>> = repository.allPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGallery: StateFlow<List<GalleryItemEntity>> = repository.allGalleryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConversations: StateFlow<List<ChatConversationEntity>> = repository.allConversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val siteConfig: StateFlow<SiteConfigEntity?> = repository.siteConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SiteConfigEntity())

    val visitorMessages: StateFlow<List<ChatMessageEntity>> = _visitorSessionId
        .flatMapLatest { id -> repository.getMessagesForVisitorFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminActiveMessages: StateFlow<List<ChatMessageEntity>> = _adminSelectedVisitorId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.getMessagesForVisitorFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val editingPostRevisions: StateFlow<List<PostRevisionEntity>> = _editingPostId
        .flatMapLatest { postId ->
            if (postId == null || postId <= 0L) flowOf(emptyList())
            else repository.getRevisionsForPostFlow(postId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncNow()
        // Real-time background polling for incoming visitor/admin messages across the entire app
        viewModelScope.launch {
            while (isActive) {
                delay(3500L)
                pollLiveMessages()
            }
        }
    }

    private suspend fun pollLiveMessages() {
        // 1. Poll Visitor Conversation
        val vid = _visitorSessionId.value
        val newAdminReply = repository.syncVisitorConversation(
            visitorId = vid,
            onBeforeIncomingAdminReply = { _, _ ->
                _isAdminTypingForVisitor.value = true
                delay(850L)
                _isAdminTypingForVisitor.value = false
            }
        )
        if (newAdminReply != null) {
            _incomingMessageEvents.tryEmit(
                IncomingMessageEvent(
                    visitorId = vid,
                    senderName = "Chitron Bhattacharjee",
                    messageText = newAdminReply.text,
                    isForAdmin = false
                )
            )
            if (!_isVisitorChatOpen.value) {
                val currentUnread = (_chatHeadBubble.value?.unreadCount ?: 0) + 1
                _chatHeadBubble.value = ChatHeadBubbleState(
                    visitorId = vid,
                    senderName = "Chitron Bhattacharjee",
                    lastMessage = newAdminReply.text,
                    unreadCount = currentUnread,
                    isForAdmin = false,
                    showPreviewBubble = true
                )
            }
        }

        // 2. Poll Admin Conversations (if Admin is authenticated)
        val token = _adminToken.value
        if (_isAdminAuthenticated.value && !token.isNullOrBlank()) {
            val activeOpenVid = _adminSelectedVisitorId.value
            val updatedConvs = repository.syncAdminConversations(token)

            // If a fullscreen conversation is currently open, sync its message list with typing indicator
            if (!activeOpenVid.isNullOrBlank()) {
                val newVisitorMsg = repository.syncAdminConversationDetail(
                    adminToken = token,
                    visitorId = activeOpenVid,
                    onBeforeIncomingVisitorMessage = { _, _ ->
                        _typingVisitorId.value = activeOpenVid
                        delay(850L)
                        _typingVisitorId.value = null
                    }
                )
                if (newVisitorMsg != null) {
                    val senderName = allConversations.value
                        .find { it.visitorId == activeOpenVid }?.visitorName
                        ?: "Visitor"
                    _incomingMessageEvents.tryEmit(
                        IncomingMessageEvent(
                            visitorId = activeOpenVid,
                            senderName = senderName,
                            messageText = newVisitorMsg.text,
                            isForAdmin = true
                        )
                    )
                }
            }

            // For any other conversation that received a new visitor message, trigger notification & Chat Head bubble
            for (conv in updatedConvs) {
                if (conv.visitorId != activeOpenVid) {
                    repository.syncAdminConversationDetail(token, conv.visitorId)
                    _incomingMessageEvents.tryEmit(
                        IncomingMessageEvent(
                            visitorId = conv.visitorId,
                            senderName = conv.visitorName,
                            messageText = conv.lastMessage,
                            isForAdmin = true
                        )
                    )
                    _chatHeadBubble.value = ChatHeadBubbleState(
                        visitorId = conv.visitorId,
                        senderName = conv.visitorName,
                        lastMessage = conv.lastMessage,
                        unreadCount = conv.unreadForAdmin.coerceAtLeast(1),
                        isForAdmin = true,
                        showPreviewBubble = true
                    )
                }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.syncWithProductionServer(_adminToken.value)
                repository.syncVisitorConversation(_visitorSessionId.value)
                val token = _adminToken.value
                val openVid = _adminSelectedVisitorId.value
                if (!token.isNullOrBlank() && !openVid.isNullOrBlank()) {
                    repository.syncAdminConversationDetail(token, openVid)
                }
                // Populate initial Chat Head bubble if there are unread messages for Admin
                if (_chatHeadBubble.value == null && _isAdminAuthenticated.value) {
                    val unreadConv = allConversations.value.firstOrNull { it.unreadForAdmin > 0 }
                    if (unreadConv != null && _adminSelectedVisitorId.value != unreadConv.visitorId) {
                        _chatHeadBubble.value = ChatHeadBubbleState(
                            visitorId = unreadConv.visitorId,
                            senderName = unreadConv.visitorName,
                            lastMessage = unreadConv.lastMessage,
                            unreadCount = unreadConv.unreadForAdmin,
                            isForAdmin = true,
                            showPreviewBubble = true
                        )
                    }
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun navigateTo(destination: MainDestination) {
        if (_currentDestination.value != MainDestination.POST_DETAIL) {
            _previousDestination.value = _currentDestination.value
        }
        _currentDestination.value = destination
        syncNow()
    }

    fun openPostDetail(postId: Long) {
        if (_currentDestination.value != MainDestination.POST_DETAIL) {
            _previousDestination.value = _currentDestination.value
        }
        _selectedPostId.value = postId
        _currentDestination.value = MainDestination.POST_DETAIL
        viewModelScope.launch {
            repository.refreshPostContentFromServer(postId)
        }
    }

    fun navigateBackFromPostDetail() {
        _currentDestination.value = _previousDestination.value
    }

    fun openWritingWithCategory(category: String) {
        _writingSelectedCategory.value = category
        _writingSelectedTag.value = ""
        _currentDestination.value = MainDestination.WRITING
    }

    fun openWritingWithTag(tag: String) {
        _writingSelectedTag.value = tag
        _writingSelectedCategory.value = ""
        _currentDestination.value = MainDestination.WRITING
    }

    fun setWritingSearchQuery(query: String) {
        _writingSearchQuery.value = query
        if (query.trim().equals("admin", ignoreCase = true)) {
            _showAdminShortcutDialog.value = true
        } else {
            _showAdminShortcutDialog.value = false
        }
    }

    fun dismissAdminShortcutDialog() {
        _showAdminShortcutDialog.value = false
    }

    fun confirmAdminShortcut() {
        _showAdminShortcutDialog.value = false
        _writingSearchQuery.value = ""
        navigateTo(MainDestination.ADMIN)
    }

    fun setWritingCategory(category: String) {
        _writingSelectedCategory.value = category
    }

    fun setWritingTag(tag: String) {
        _writingSelectedTag.value = tag
    }

    fun setWritingSortNewest(newest: Boolean) {
        _writingSortNewest.value = newest
    }

    // Gallery & Lightbox
    fun setGalleryCategory(category: String) {
        _gallerySelectedCategory.value = category
        _lightboxPhotoIndex.value = null
    }

    fun openLightbox(index: Int) {
        _lightboxPhotoIndex.value = index
    }

    fun closeLightbox() {
        _lightboxPhotoIndex.value = null
    }

    // Visitor Chat
    fun setVisitorChatOpen(open: Boolean) {
        _isVisitorChatOpen.value = open
        if (open) {
            if (_chatHeadBubble.value?.isForAdmin == false) {
                _chatHeadBubble.value = null
            }
            viewModelScope.launch {
                repository.syncVisitorConversation(_visitorSessionId.value)
            }
        }
    }

    fun minimizeVisitorChatToBubble() {
        val lastMsg = visitorMessages.value.lastOrNull()?.text ?: "Tap to open chat"
        _isVisitorChatOpen.value = false
        _chatHeadBubble.value = ChatHeadBubbleState(
            visitorId = _visitorSessionId.value,
            senderName = "Chitron Bhattacharjee",
            lastMessage = lastMsg,
            unreadCount = 0,
            isForAdmin = false,
            showPreviewBubble = false
        )
    }

    fun updateVisitorName(name: String) {
        _visitorName.value = name
        prefs.edit().putString("visitor_name", name).apply()
    }

    fun sendVisitorMessage(text: String) {
        viewModelScope.launch {
            repository.sendVisitorMessage(
                visitorId = _visitorSessionId.value,
                visitorName = _visitorName.value,
                text = text
            )
        }
    }

    // Strict Admin Authentication (PIN 2448766 + live server token)
    fun verifyAdminPin(pin: String) {
        val cleanPin = pin.trim()
        if (cleanPin.isEmpty()) {
            _adminLoginError.value = "Please enter your Admin PIN."
            return
        }
        viewModelScope.launch {
            _adminLoginError.value = null
            val remoteToken = ArchiveRemoteClient.loginAdmin(cleanPin)
            val expectedPin = BuildConfig.ADMIN_PIN.ifBlank { "2448766" }
            if (!remoteToken.isNullOrBlank() || cleanPin == expectedPin) {
                val tokenToSave = remoteToken ?: "local-session-token"
                _adminToken.value = tokenToSave
                prefs.edit().putString("admin_token", tokenToSave).apply()
                _isAdminAuthenticated.value = true
                _adminLoginError.value = null
                syncNow()
            } else {
                _adminLoginError.value = "Invalid PIN. Access denied."
            }
        }
    }

    fun logoutAdmin() {
        _adminToken.value = null
        prefs.edit().remove("admin_token").apply()
        _isAdminAuthenticated.value = false
        _adminSelectedVisitorId.value = null
        _adminLoginError.value = null
    }

    fun setAdminSubTab(tab: AdminSubTab) {
        _adminSubTab.value = tab
        if (tab != AdminSubTab.MESSAGES) {
            _adminSelectedVisitorId.value = null
        }
        val token = _adminToken.value
        if (!token.isNullOrBlank()) {
            viewModelScope.launch {
                if (tab == AdminSubTab.MESSAGES) {
                    repository.syncAdminConversations(token)
                } else {
                    repository.syncWithProductionServer(token)
                }
            }
        }
    }

    fun setAdminPostFilter(filter: String) {
        _adminPostFilter.value = filter
        _adminSubTab.value = AdminSubTab.POSTS
    }

    fun setEditingPostForRevisions(postId: Long?) {
        _editingPostId.value = postId
    }

    fun saveOrUpdatePost(
        id: Long,
        title: String,
        slug: String,
        excerpt: String,
        content: String,
        coverImage: String,
        category: String,
        labelsCsv: String,
        status: String,
        featured: Boolean
    ) {
        viewModelScope.launch {
            repository.saveOrUpdatePost(
                adminToken = _adminToken.value,
                id = id,
                title = title,
                slug = slug,
                excerpt = excerpt,
                content = content,
                coverImage = coverImage,
                category = category,
                labelsCsv = labelsCsv,
                status = status,
                featured = featured
            )
        }
    }

    fun duplicatePost(post: BlogPostEntity) {
        viewModelScope.launch {
            repository.duplicatePost(_adminToken.value, post)
        }
    }

    fun updatePostStatus(post: BlogPostEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updatePostStatus(_adminToken.value, post, newStatus)
        }
    }

    fun deletePostPermanently(postId: Long) {
        val post = allPosts.value.find { it.id == postId } ?: return
        viewModelScope.launch {
            repository.deletePostPermanently(_adminToken.value, post)
        }
    }

    fun restoreRevision(post: BlogPostEntity, revision: PostRevisionEntity) {
        viewModelScope.launch {
            repository.restoreRevision(_adminToken.value, post, revision)
        }
    }

    fun addGalleryPhoto(
        title: String,
        caption: String,
        url: String,
        category: String,
        location: String,
        featured: Boolean
    ) {
        viewModelScope.launch {
            repository.addGalleryPhoto(
                adminToken = _adminToken.value,
                title = title,
                caption = caption,
                url = url,
                category = category,
                location = location,
                featured = featured
            )
        }
    }

    fun deleteGalleryPhoto(id: Long) {
        val item = allGallery.value.find { it.id == id } ?: return
        viewModelScope.launch {
            repository.deleteGalleryPhoto(_adminToken.value, item)
        }
    }

    // Admin Messenger & Fullscreen Thread Navigation
    fun setAdminChatFilter(filter: String) {
        _adminChatFilter.value = filter
    }

    fun selectAdminConversation(visitorId: String) {
        _adminSelectedVisitorId.value = visitorId
        if (_chatHeadBubble.value?.visitorId == visitorId) {
            _chatHeadBubble.value = null
        }
        viewModelScope.launch {
            val token = _adminToken.value
            if (!token.isNullOrBlank()) {
                repository.syncAdminConversationDetail(token, visitorId)
            }
            repository.markConversationReadByAdmin(token, visitorId)
        }
    }

    fun closeAdminConversation() {
        _adminSelectedVisitorId.value = null
    }

    fun minimizeAdminConversationToChatHead(visitorId: String) {
        val conv = allConversations.value.find { it.visitorId == visitorId }
        _adminSelectedVisitorId.value = null
        if (conv != null) {
            _chatHeadBubble.value = ChatHeadBubbleState(
                visitorId = conv.visitorId,
                senderName = conv.visitorName,
                lastMessage = conv.lastMessage,
                unreadCount = conv.unreadForAdmin,
                isForAdmin = true,
                showPreviewBubble = false
            )
        }
    }

    fun openChatFromBubbleOrNotification(visitorId: String, isForAdmin: Boolean) {
        _chatHeadBubble.value = null
        if (isForAdmin && _isAdminAuthenticated.value) {
            _currentDestination.value = MainDestination.ADMIN
            _adminSubTab.value = AdminSubTab.MESSAGES
            selectAdminConversation(visitorId)
        } else {
            setVisitorChatOpen(true)
        }
    }

    fun dismissChatHeadBubble() {
        _chatHeadBubble.value = null
    }

    fun dismissChatHeadPreviewToast() {
        _chatHeadBubble.value = _chatHeadBubble.value?.copy(showPreviewBubble = false)
    }

    fun sendAdminReply(visitorId: String, text: String) {
        viewModelScope.launch {
            repository.sendAdminReply(_adminToken.value, visitorId, text)
        }
    }

    fun updateConversationStatus(visitorId: String, status: String) {
        viewModelScope.launch {
            repository.updateConversationStatus(_adminToken.value, visitorId, status)
        }
    }

    fun deleteConversation(visitorId: String) {
        viewModelScope.launch {
            repository.deleteConversation(_adminToken.value, visitorId)
            if (_adminSelectedVisitorId.value == visitorId) {
                _adminSelectedVisitorId.value = null
            }
            if (_chatHeadBubble.value?.visitorId == visitorId) {
                _chatHeadBubble.value = null
            }
        }
    }

    fun saveSiteConfig(config: SiteConfigEntity) {
        viewModelScope.launch {
            repository.updateSiteConfig(_adminToken.value, config)
        }
    }

    class Factory(
        private val repository: ArchiveRepository,
        private val prefs: SharedPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ArchiveViewModel(repository, prefs) as T
        }
    }
}
