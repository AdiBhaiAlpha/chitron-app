package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import com.example.ui.theme.AppIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BlogPostEntity
import com.example.data.ChatConversationEntity
import com.example.data.ChatMessageEntity
import com.example.data.GalleryItemEntity
import com.example.data.PostRevisionEntity
import com.example.data.SiteConfigEntity
import com.example.i18n.AppLanguage
import com.example.ui.AdminSubTab

@Composable
fun AdminMainScreen(
    isAuthenticated: Boolean,
    loginError: String?,
    activeSubTab: AdminSubTab,
    postFilter: String,
    chatFilter: String,
    selectedVisitorId: String?,
    allPosts: List<BlogPostEntity>,
    allGallery: List<GalleryItemEntity>,
    allConversations: List<ChatConversationEntity>,
    activeMessages: List<ChatMessageEntity>,
    editingRevisions: List<PostRevisionEntity>,
    siteConfig: SiteConfigEntity,
    lang: AppLanguage,
    onVerifyPin: (String) -> Unit,
    onLogout: () -> Unit,
    onSelectSubTab: (AdminSubTab) -> Unit,
    onSelectPostFilter: (String) -> Unit,
    onSavePost: (Long, String, String, String, String, String, String, String, String, Boolean) -> Unit,
    onDuplicatePost: (BlogPostEntity) -> Unit,
    onUpdatePostStatus: (BlogPostEntity, String) -> Unit,
    onDeletePostPermanently: (Long) -> Unit,
    onLoadRevisionsForPost: (Long?) -> Unit,
    onRestoreRevision: (BlogPostEntity, PostRevisionEntity) -> Unit,
    onSelectChatFilter: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onSendAdminReply: (String, String) -> Unit,
    onUpdateConversationStatus: (String, String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onAddGalleryPhoto: (String, String, String, String, String, Boolean) -> Unit,
    onDeleteGalleryPhoto: (Long) -> Unit,
    onSaveSiteConfig: (SiteConfigEntity) -> Unit
) {
    if (!isAuthenticated) {
        var pinInput by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Chitron's Archive",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Admin CMS & Live Messenger",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 8) pinInput = it },
                        label = { Text("Enter Admin PIN") },
                        placeholder = { Text("•••••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )
                    if (loginError != null) {
                        Text(
                            text = loginError,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = { onVerifyPin(pinInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_sign_in_btn")
                    ) {
                        Text("Sign In")
                    }
                }
            }
        }
        return
    }

    var editingPost by remember { mutableStateOf<BlogPostEntity?>(null) }
    var isCreatingNewPost by remember { mutableStateOf(false) }
    var revisionModalPost by remember { mutableStateOf<BlogPostEntity?>(null) }

    // Post Editor Dialog
    if (isCreatingNewPost || editingPost != null) {
        val current = editingPost
        var title by remember(current) { mutableStateOf(current?.title ?: "") }
        var slug by remember(current) { mutableStateOf(current?.slug ?: "") }
        var category by remember(current) { mutableStateOf(current?.category ?: "ai") }
        var labelsCsv by remember(current) { mutableStateOf(current?.labelsCsv ?: "") }
        var excerpt by remember(current) { mutableStateOf(current?.excerpt ?: "") }
        var content by remember(current) { mutableStateOf(current?.content ?: "") }
        var coverImage by remember(current) { mutableStateOf(current?.coverImage ?: "") }
        var status by remember(current) { mutableStateOf(current?.status ?: "published") }
        var featured by remember(current) { mutableStateOf(current?.featured ?: false) }

        AlertDialog(
            onDismissRequest = {
                editingPost = null
                isCreatingNewPost = false
            },
            title = { Text(if (current == null) "New Article" else "Edit Article") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(400.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = status,
                                onValueChange = { status = it },
                                label = { Text("Status (published/draft)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = labelsCsv,
                            onValueChange = { labelsCsv = it },
                            label = { Text("Labels (comma-separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = coverImage,
                            onValueChange = { coverImage = it },
                            label = { Text("Cover Image URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = excerpt,
                            onValueChange = { excerpt = it },
                            label = { Text("Excerpt") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Article Content (Markdown / Paragraphs)") },
                            minLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = featured, onCheckedChange = { featured = it })
                            Text("Featured Post")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSavePost(
                                current?.id ?: 0L,
                                title,
                                slug,
                                excerpt,
                                content,
                                coverImage,
                                category,
                                labelsCsv,
                                status.lowercase().ifBlank { "published" },
                                featured
                            )
                            editingPost = null
                            isCreatingNewPost = false
                        }
                    }
                ) {
                    Text("Save Article")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingPost = null
                        isCreatingNewPost = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Revision History Dialog
    if (revisionModalPost != null) {
        val targetPost = revisionModalPost!!
        AlertDialog(
            onDismissRequest = {
                revisionModalPost = null
                onLoadRevisionsForPost(null)
            },
            title = { Text("Revisions: ${targetPost.title}") },
            text = {
                if (editingRevisions.isEmpty()) {
                    Text("No previous revisions saved yet. Edit and save this post to create a revision snapshot.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(260.dp)
                    ) {
                        items(editingRevisions, key = { it.id }) { rev ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rev.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatEditorialDate(rev.savedAt, lang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            onRestoreRevision(targetPost, rev)
                                            revisionModalPost = null
                                            onLoadRevisionsForPost(null)
                                        }
                                    ) {
                                        Text("Restore")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        revisionModalPost = null
                        onLoadRevisionsForPost(null)
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    val unreadTotal = remember(allConversations) {
        allConversations.sumOf { it.unreadForAdmin }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Admin Sub-navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = activeSubTab == AdminSubTab.DASHBOARD,
                onClick = { onSelectSubTab(AdminSubTab.DASHBOARD) },
                label = { Text("Dashboard") }
            )
            FilterChip(
                selected = activeSubTab == AdminSubTab.POSTS,
                onClick = { onSelectSubTab(AdminSubTab.POSTS) },
                label = { Text("Posts (${allPosts.size})") }
            )
            FilterChip(
                selected = activeSubTab == AdminSubTab.MESSAGES,
                onClick = { onSelectSubTab(AdminSubTab.MESSAGES) },
                label = {
                    Text(if (unreadTotal > 0) "Messages ($unreadTotal)" else "Messages")
                }
            )
            FilterChip(
                selected = activeSubTab == AdminSubTab.GALLERY,
                onClick = { onSelectSubTab(AdminSubTab.GALLERY) },
                label = { Text("Gallery (${allGallery.size})") }
            )
            FilterChip(
                selected = activeSubTab == AdminSubTab.SITE_CONTENT,
                onClick = { onSelectSubTab(AdminSubTab.SITE_CONTENT) },
                label = { Text("Site Content") }
            )
            IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_btn")) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        when (activeSubTab) {
            AdminSubTab.DASHBOARD -> {
                val publishedCount = allPosts.count { it.status == "published" }
                val draftCount = allPosts.count { it.status == "draft" }
                val scheduledCount = allPosts.count { it.status == "scheduled" }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CMS Overview",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Button(
                                onClick = { isCreatingNewPost = true },
                                modifier = Modifier.testTag("admin_new_post_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Post")
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard("Total", allPosts.size.toString(), Modifier.weight(1f)) {
                                onSelectPostFilter("")
                            }
                            StatCard("Published", publishedCount.toString(), Modifier.weight(1f)) {
                                onSelectPostFilter("published")
                            }
                            StatCard("Drafts", draftCount.toString(), Modifier.weight(1f)) {
                                onSelectPostFilter("draft")
                            }
                            StatCard("Unread Msg", unreadTotal.toString(), Modifier.weight(1f)) {
                                onSelectSubTab(AdminSubTab.MESSAGES)
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Recent Posts (${scheduledCount} scheduled)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(allPosts.take(6), key = { it.id }) { post ->
                        AdminPostRow(
                            post = post,
                            onEdit = { editingPost = post },
                            onDuplicate = { onDuplicatePost(post) },
                            onToggleStatus = {
                                val next = if (post.status == "published") "draft" else "published"
                                onUpdatePostStatus(post, next)
                            },
                            onTrashOrDelete = {
                                if (post.status == "trashed") {
                                    onDeletePostPermanently(post.id)
                                } else {
                                    onUpdatePostStatus(post, "trashed")
                                }
                            },
                            onOpenRevisions = {
                                onLoadRevisionsForPost(post.id)
                                revisionModalPost = post
                            }
                        )
                    }
                }
            }

            AdminSubTab.POSTS -> {
                val filtered = remember(allPosts, postFilter) {
                    if (postFilter.isBlank()) allPosts
                    else allPosts.filter { it.status.equals(postFilter, ignoreCase = true) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                "" to "All",
                                "published" to "Published",
                                "draft" to "Drafts",
                                "scheduled" to "Scheduled",
                                "trashed" to "Trash"
                            ).forEach { (filterKey, label) ->
                                FilterChip(
                                    selected = postFilter == filterKey,
                                    onClick = { onSelectPostFilter(filterKey) },
                                    label = { Text(label) }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { isCreatingNewPost = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("New")
                            }
                        }
                    }

                    items(filtered, key = { it.id }) { post ->
                        AdminPostRow(
                            post = post,
                            onEdit = { editingPost = post },
                            onDuplicate = { onDuplicatePost(post) },
                            onToggleStatus = {
                                val next = if (post.status == "published") "draft" else "published"
                                onUpdatePostStatus(post, next)
                            },
                            onTrashOrDelete = {
                                if (post.status == "trashed") {
                                    onDeletePostPermanently(post.id)
                                } else {
                                    onUpdatePostStatus(post, "trashed")
                                }
                            },
                            onOpenRevisions = {
                                onLoadRevisionsForPost(post.id)
                                revisionModalPost = post
                            }
                        )
                    }
                }
            }

            AdminSubTab.MESSAGES -> {
                val filteredConversations = remember(allConversations, chatFilter) {
                    when (chatFilter) {
                        "unread" -> allConversations.filter { it.unreadForAdmin > 0 }
                        "active" -> allConversations.filter { it.status == "active" }
                        "archived" -> allConversations.filter { it.status == "archived" }
                        "blocked" -> allConversations.filter { it.status == "blocked" }
                        else -> allConversations
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Live Visitor Messenger",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap any conversation to open in Fullscreen Chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("all", "unread", "active", "archived", "blocked").forEach { f ->
                                FilterChip(
                                    selected = chatFilter == f,
                                    onClick = { onSelectChatFilter(f) },
                                    label = { Text(f.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }

                    if (filteredConversations.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "No conversations in this filter yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    }

                    // Messenger-style Conversation List (tap opens Fullscreen Chat)
                    items(filteredConversations, key = { it.visitorId }) { conv ->
                        val hasUnread = conv.unreadForAdmin > 0
                        val initial = conv.visitorName.trim().firstOrNull()?.uppercase() ?: "V"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectConversation(conv.visitorId) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (hasUnread) 1.5.dp else 1.dp,
                                color = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Circle
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = initial,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = conv.visitorName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (conv.status == "active") MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = conv.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (conv.lastSender == "admin") "You: ${conv.lastMessage}" else conv.lastMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (hasUnread) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (hasUnread) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = conv.unreadForAdmin.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = AppIcons.ChatBubble,
                                        contentDescription = "Open fullscreen chat",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AdminSubTab.GALLERY -> {
                var newTitle by remember { mutableStateOf("") }
                var newCaption by remember { mutableStateOf("") }
                var newUrl by remember { mutableStateOf("") }
                var newCategory by remember { mutableStateOf("Photography") }
                var newLocation by remember { mutableStateOf("Bangladesh") }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Add Photo to Gallery", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(
                                    value = newTitle,
                                    onValueChange = { newTitle = it },
                                    label = { Text("Photo Title *") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newCategory,
                                        onValueChange = { newCategory = it },
                                        label = { Text("Category") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = newLocation,
                                        onValueChange = { newLocation = it },
                                        label = { Text("Location") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                OutlinedTextField(
                                    value = newUrl,
                                    onValueChange = { newUrl = it },
                                    label = { Text("Image URL") },
                                    placeholder = { Text("https://images.unsplash.com/...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newCaption,
                                    onValueChange = { newCaption = it },
                                    label = { Text("Caption") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = {
                                        if (newTitle.isNotBlank()) {
                                            onAddGalleryPhoto(
                                                newTitle,
                                                newCaption,
                                                newUrl.ifBlank { "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?q=80&w=1200&auto=format&fit=crop" },
                                                newCategory,
                                                newLocation,
                                                false
                                            )
                                            newTitle = ""
                                            newCaption = ""
                                            newUrl = ""
                                        }
                                    }
                                ) {
                                    Text("Publish Photo")
                                }
                            }
                        }
                    }

                    items(allGallery, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${item.category} • ${item.location}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteGalleryPhoto(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete photo")
                                }
                            }
                        }
                    }
                }
            }

            AdminSubTab.SITE_CONTENT -> {
                var siteName by remember(siteConfig) { mutableStateOf(siteConfig.siteName) }
                var heroTitle by remember(siteConfig) { mutableStateOf(siteConfig.heroTitle) }
                var heroDesc by remember(siteConfig) { mutableStateOf(siteConfig.heroDescription) }
                var aboutHeadline by remember(siteConfig) { mutableStateOf(siteConfig.aboutHeadline) }
                var aboutCurrently by remember(siteConfig) { mutableStateOf(siteConfig.aboutCurrently) }
                var skillsCsv by remember(siteConfig) { mutableStateOf(siteConfig.skillsCsv) }
                var savedBanner by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Site Content & Settings", style = MaterialTheme.typography.headlineMedium)
                    }
                    item {
                        OutlinedTextField(
                            value = siteName,
                            onValueChange = { siteName = it },
                            label = { Text("Site Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = heroTitle,
                            onValueChange = { heroTitle = it },
                            label = { Text("Homepage Hero Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = heroDesc,
                            onValueChange = { heroDesc = it },
                            label = { Text("Homepage Hero Description") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = aboutHeadline,
                            onValueChange = { aboutHeadline = it },
                            label = { Text("About Page Headline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = aboutCurrently,
                            onValueChange = { aboutCurrently = it },
                            label = { Text("Currently Working On") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = skillsCsv,
                            onValueChange = { skillsCsv = it },
                            label = { Text("Skills (comma-separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSaveSiteConfig(
                                        siteConfig.copy(
                                            siteName = siteName,
                                            heroTitle = heroTitle,
                                            heroDescription = heroDesc,
                                            aboutHeadline = aboutHeadline,
                                            aboutCurrently = aboutCurrently,
                                            skillsCsv = skillsCsv
                                        )
                                    )
                                    savedBanner = true
                                }
                            ) {
                                Text("Save Site Content")
                            }
                            if (savedBanner) {
                                Text(
                                    "Saved!",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminPostRow(
    post: BlogPostEntity,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleStatus: () -> Unit,
    onTrashOrDelete: () -> Unit,
    onOpenRevisions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${post.status.uppercase()} • ${post.category.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${post.readingTime} min • ${post.viewCount} views",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit post", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onOpenRevisions) {
                    Icon(AppIcons.History, contentDescription = "Revisions", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDuplicate) {
                    Icon(AppIcons.ContentCopy, contentDescription = "Duplicate post", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onToggleStatus) {
                    Icon(AppIcons.Restore, contentDescription = "Toggle publish/draft", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onTrashOrDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Trash or delete", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
