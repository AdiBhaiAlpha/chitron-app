package com.example.data

import kotlinx.coroutines.flow.Flow

class ArchiveRepository(private val dao: ArchiveDao) {

    val allPostsFlow: Flow<List<BlogPostEntity>> = dao.getAllPostsFlow()
    val allGalleryFlow: Flow<List<GalleryItemEntity>> = dao.getAllGalleryItemsFlow()
    val allConversationsFlow: Flow<List<ChatConversationEntity>> = dao.getAllConversationsFlow()
    val siteConfigFlow: Flow<SiteConfigEntity?> = dao.getSiteConfigFlow()

    fun getRevisionsForPostFlow(postId: Long): Flow<List<PostRevisionEntity>> =
        dao.getRevisionsForPostFlow(postId)

    fun getMessagesForVisitorFlow(visitorId: String): Flow<List<ChatMessageEntity>> =
        dao.getMessagesForVisitorFlow(visitorId)

    suspend fun syncWithProductionServer(adminToken: String? = null) {
        val currentConfig = dao.getSiteConfigOnce() ?: SiteConfigEntity()
        val remoteConfig = ArchiveRemoteClient.fetchSiteConfig(currentConfig)
        if (remoteConfig != null) {
            dao.upsertSiteConfig(remoteConfig)
        } else if (dao.getSiteConfigOnce() == null) {
            dao.upsertSiteConfig(currentConfig)
        }

        // Sync Posts (Admin list if authenticated, otherwise public published posts)
        val remotePosts = if (!adminToken.isNullOrBlank()) {
            ArchiveRemoteClient.fetchAdminPosts(adminToken) ?: ArchiveRemoteClient.fetchPublishedPosts()
        } else {
            ArchiveRemoteClient.fetchPublishedPosts()
        }
        if (remotePosts.isNotEmpty()) {
            val existingBySlug = dao.getAllPostsOnce().associateBy { it.slug }
            val merged = remotePosts.map { post ->
                val prev = existingBySlug[post.slug]
                if (prev != null && post.content.isBlank() && prev.content.isNotBlank()) {
                    post.copy(content = prev.content)
                } else {
                    post
                }
            }
            dao.clearAllPosts()
            dao.insertPosts(merged)
        }

        // Sync Gallery
        val remoteGallery = ArchiveRemoteClient.fetchGalleryItems()
        if (remoteGallery != null) {
            dao.clearAllGalleryItems()
            dao.insertGalleryItems(remoteGallery)
        }

        // Sync Admin Conversations if authenticated
        if (!adminToken.isNullOrBlank()) {
            syncAdminConversations(adminToken)
        }
    }

    suspend fun refreshPostContentFromServer(postId: Long) {
        val local = dao.getPostById(postId) ?: return
        val remote = ArchiveRemoteClient.fetchSinglePostBySlug(local.slug)
        if (remote != null) {
            dao.updatePost(
                local.copy(
                    remoteId = remote.remoteId.ifBlank { local.remoteId },
                    title = remote.title,
                    excerpt = remote.excerpt,
                    content = remote.content.ifBlank { local.content },
                    coverImage = remote.coverImage.ifBlank { local.coverImage },
                    category = remote.category,
                    labelsCsv = remote.labelsCsv,
                    readingTime = remote.readingTime,
                    viewCount = remote.viewCount
                )
            )
        } else {
            dao.incrementPostViews(postId)
        }
    }

    suspend fun syncVisitorConversation(
        visitorId: String,
        onBeforeIncomingAdminReply: (suspend (ChatConversationEntity, ChatMessageEntity) -> Unit)? = null
    ): ChatMessageEntity? {
        val result = ArchiveRemoteClient.fetchVisitorConversation(visitorId) ?: return null
        val (conv, remoteMessages) = result
        val localMessages = dao.getMessagesForVisitorOnce(visitorId)
        var newestAdminReply: ChatMessageEntity? = null

        if (localMessages.isEmpty() && remoteMessages.isNotEmpty()) {
            dao.upsertConversation(conv)
            dao.insertChatMessages(remoteMessages)
        } else if (remoteMessages.size > localMessages.size) {
            val added = remoteMessages.drop(localMessages.size)
            newestAdminReply = added.lastOrNull { it.sender == "admin" }
            if (newestAdminReply != null && onBeforeIncomingAdminReply != null) {
                onBeforeIncomingAdminReply(conv, newestAdminReply)
            }
            dao.upsertConversation(conv)
            dao.insertChatMessages(added)
        } else if (remoteMessages.size < localMessages.size) {
            dao.upsertConversation(conv)
            dao.deleteMessagesForVisitor(visitorId)
            dao.insertChatMessages(remoteMessages)
        } else {
            dao.upsertConversation(conv)
        }
        return newestAdminReply
    }

    suspend fun syncAdminConversations(
        adminToken: String
    ): List<ChatConversationEntity> {
        val list = ArchiveRemoteClient.fetchAdminConversations(adminToken) ?: return emptyList()
        val oldById = dao.getAllConversationsOnce().associateBy { it.visitorId }
        val newlyUpdatedVisitorConvs = mutableListOf<ChatConversationEntity>()

        for (conv in list) {
            val old = oldById[conv.visitorId]
            if (old != null) {
                val hasNewVisitorMsg = (conv.lastMessageAt > old.lastMessageAt ||
                    (conv.lastMessage != old.lastMessage && conv.unreadForAdmin > old.unreadForAdmin)) &&
                    conv.lastSender == "visitor"
                if (hasNewVisitorMsg) {
                    newlyUpdatedVisitorConvs.add(conv)
                }
            } else if (conv.unreadForAdmin > 0 && conv.lastSender == "visitor" && oldById.isNotEmpty()) {
                newlyUpdatedVisitorConvs.add(conv)
            }
        }

        val remoteIds = list.map { it.visitorId }.toSet()
        for (oldId in oldById.keys) {
            if (oldId !in remoteIds) {
                dao.deleteConversation(oldId)
            }
        }
        dao.upsertConversations(list)
        return newlyUpdatedVisitorConvs
    }

    suspend fun syncAdminConversationDetail(
        adminToken: String,
        visitorId: String,
        onBeforeIncomingVisitorMessage: (suspend (ChatConversationEntity, ChatMessageEntity) -> Unit)? = null
    ): ChatMessageEntity? {
        val result = ArchiveRemoteClient.fetchAdminConversationDetail(adminToken, visitorId) ?: return null
        val (conv, remoteMessages) = result
        val localMessages = dao.getMessagesForVisitorOnce(visitorId)
        var newestVisitorMsg: ChatMessageEntity? = null

        if (localMessages.isEmpty() && remoteMessages.isNotEmpty()) {
            dao.upsertConversation(conv)
            dao.insertChatMessages(remoteMessages)
        } else if (remoteMessages.size > localMessages.size) {
            val added = remoteMessages.drop(localMessages.size)
            newestVisitorMsg = added.lastOrNull { it.sender == "visitor" }
            if (newestVisitorMsg != null && onBeforeIncomingVisitorMessage != null) {
                onBeforeIncomingVisitorMessage(conv, newestVisitorMsg)
            }
            dao.upsertConversation(conv)
            dao.insertChatMessages(added)
        } else if (remoteMessages.size < localMessages.size) {
            dao.upsertConversation(conv)
            dao.deleteMessagesForVisitor(visitorId)
            dao.insertChatMessages(remoteMessages)
        } else {
            dao.upsertConversation(conv)
        }
        return newestVisitorMsg
    }

    fun calculateReadingTime(content: String): Int {
        val plain = content.replace(Regex("<[^>]*>"), " ")
        val words = plain.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return (words / 200.0).let { kotlin.math.ceil(it).toInt() }.coerceAtLeast(1)
    }

    fun generateSlug(title: String, customSlug: String = ""): String {
        val base = customSlug.ifBlank { title }.trim().lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s-]"), "")
            .replace(Regex("[\\s_]+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
        return if (base.length >= 2) base else "post-${System.currentTimeMillis()}"
    }

    suspend fun saveOrUpdatePost(
        adminToken: String?,
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
    ): Long {
        val readingTime = calculateReadingTime(content)
        val cleanSlug = generateSlug(title, slug)
        val cleanCat = category.trim().lowercase().ifBlank { "general" }
        val existing = if (id > 0) dao.getPostById(id) else null

        if (existing != null) {
            dao.insertRevision(
                PostRevisionEntity(
                    postId = existing.id,
                    title = existing.title,
                    excerpt = existing.excerpt,
                    content = existing.content,
                    savedAt = System.currentTimeMillis()
                )
            )
        }

        if (!adminToken.isNullOrBlank()) {
            val remoteSaved = ArchiveRemoteClient.createOrUpdateRemotePost(
                adminToken = adminToken,
                remoteId = existing?.remoteId.orEmpty(),
                title = title.trim(),
                slug = cleanSlug,
                excerpt = excerpt.trim(),
                content = content,
                coverImage = coverImage.trim(),
                category = cleanCat,
                labelsCsv = labelsCsv.trim(),
                status = status,
                featured = featured
            )
            if (remoteSaved != null) {
                syncWithProductionServer(adminToken)
                return existing?.id ?: 0L
            }
        }

        if (existing != null) {
            val updated = existing.copy(
                title = title.trim(),
                slug = cleanSlug,
                excerpt = excerpt.trim(),
                content = content,
                coverImage = coverImage.trim(),
                category = cleanCat,
                labelsCsv = labelsCsv.trim(),
                status = status,
                readingTime = readingTime,
                featured = featured,
                publishedAt = if (existing.status != "published" && status == "published") {
                    System.currentTimeMillis()
                } else {
                    existing.publishedAt
                }
            )
            dao.updatePost(updated)
            return existing.id
        } else {
            val newPost = BlogPostEntity(
                title = title.trim(),
                slug = cleanSlug,
                excerpt = excerpt.trim(),
                content = content,
                coverImage = coverImage.trim(),
                category = cleanCat,
                labelsCsv = labelsCsv.trim(),
                status = status,
                readingTime = readingTime,
                featured = featured,
                publishedAt = System.currentTimeMillis()
            )
            return dao.insertPost(newPost)
        }
    }

    suspend fun duplicatePost(adminToken: String?, post: BlogPostEntity) {
        if (!adminToken.isNullOrBlank() && post.remoteId.isNotBlank()) {
            if (ArchiveRemoteClient.duplicateRemotePost(adminToken, post.remoteId)) {
                syncWithProductionServer(adminToken)
                return
            }
        }
        val copy = post.copy(
            id = 0,
            remoteId = "",
            title = "${post.title} (Copy)",
            slug = "${post.slug}-copy-${System.currentTimeMillis() % 10000}",
            status = "draft",
            viewCount = 0,
            publishedAt = System.currentTimeMillis()
        )
        dao.insertPost(copy)
    }

    suspend fun updatePostStatus(adminToken: String?, post: BlogPostEntity, newStatus: String) {
        if (!adminToken.isNullOrBlank() && post.remoteId.isNotBlank()) {
            ArchiveRemoteClient.updateRemotePostStatus(adminToken, post.remoteId, newStatus)
            syncWithProductionServer(adminToken)
            return
        }
        dao.updatePost(
            post.copy(
                status = newStatus,
                publishedAt = if (newStatus == "published") System.currentTimeMillis() else post.publishedAt
            )
        )
    }

    suspend fun deletePostPermanently(adminToken: String?, post: BlogPostEntity) {
        if (!adminToken.isNullOrBlank() && post.remoteId.isNotBlank()) {
            ArchiveRemoteClient.deleteRemotePostPermanently(adminToken, post.remoteId)
        }
        dao.deleteRevisionsForPost(post.id)
        dao.deletePostPermanently(post.id)
    }

    suspend fun restoreRevision(adminToken: String?, post: BlogPostEntity, revision: PostRevisionEntity) {
        saveOrUpdatePost(
            adminToken = adminToken,
            id = post.id,
            title = revision.title,
            slug = post.slug,
            excerpt = revision.excerpt,
            content = revision.content,
            coverImage = post.coverImage,
            category = post.category,
            labelsCsv = post.labelsCsv,
            status = post.status,
            featured = post.featured
        )
    }

    suspend fun addGalleryPhoto(
        adminToken: String?,
        title: String,
        caption: String,
        url: String,
        category: String,
        location: String,
        featured: Boolean
    ) {
        if (!adminToken.isNullOrBlank()) {
            val ok = ArchiveRemoteClient.createRemoteGalleryPhoto(
                adminToken = adminToken,
                title = title.trim(),
                caption = caption.trim(),
                url = url.trim(),
                category = category.trim().ifBlank { "Photography" },
                location = location.trim().ifBlank { "Bangladesh" },
                featured = featured
            )
            if (ok) {
                syncWithProductionServer(adminToken)
                return
            }
        }
        dao.insertGalleryItem(
            GalleryItemEntity(
                title = title.trim(),
                caption = caption.trim(),
                url = ArchiveRemoteClient.resolveMediaUrl(url.trim()),
                category = category.trim().ifBlank { "Photography" },
                location = location.trim().ifBlank { "Bangladesh" },
                alt = title.trim(),
                featured = featured,
                dateMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteGalleryPhoto(adminToken: String?, item: GalleryItemEntity) {
        if (!adminToken.isNullOrBlank() && item.remoteId.isNotBlank()) {
            ArchiveRemoteClient.deleteRemoteGalleryPhoto(adminToken, item.remoteId)
        }
        dao.deleteGalleryItem(item.id)
    }

    suspend fun sendVisitorMessage(
        visitorId: String,
        visitorName: String,
        text: String
    ) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return
        val resolvedName = visitorName.trim().ifBlank { "Anonymous Visitor" }
        val sentRemote = ArchiveRemoteClient.sendVisitorMessage(visitorId, resolvedName, cleanText)
        if (sentRemote) {
            syncVisitorConversation(visitorId)
        } else {
            val now = System.currentTimeMillis()
            dao.upsertConversation(
                ChatConversationEntity(
                    visitorId = visitorId,
                    visitorName = resolvedName,
                    lastMessage = cleanText,
                    lastMessageAt = now,
                    lastSender = "visitor",
                    unreadForAdmin = 1,
                    status = "active"
                )
            )
            dao.insertChatMessage(
                ChatMessageEntity(
                    visitorId = visitorId,
                    sender = "visitor",
                    text = cleanText,
                    read = false,
                    timestamp = now
                )
            )
        }
    }

    suspend fun sendAdminReply(
        adminToken: String?,
        visitorId: String,
        text: String
    ) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return
        if (!adminToken.isNullOrBlank()) {
            val ok = ArchiveRemoteClient.sendAdminReply(adminToken, visitorId, cleanText)
            if (ok) {
                syncAdminConversationDetail(adminToken, visitorId)
                syncAdminConversations(adminToken)
                return
            }
        }
        val now = System.currentTimeMillis()
        val existing = dao.getConversation(visitorId)
        dao.upsertConversation(
            ChatConversationEntity(
                visitorId = visitorId,
                visitorName = existing?.visitorName ?: "Visitor",
                lastMessage = cleanText,
                lastMessageAt = now,
                lastSender = "admin",
                unreadForAdmin = 0,
                status = existing?.status ?: "active"
            )
        )
        dao.insertChatMessage(
            ChatMessageEntity(
                visitorId = visitorId,
                sender = "admin",
                text = cleanText,
                read = true,
                timestamp = now
            )
        )
    }

    suspend fun markConversationReadByAdmin(adminToken: String?, visitorId: String) {
        if (!adminToken.isNullOrBlank()) {
            ArchiveRemoteClient.markAdminConversationRead(adminToken, visitorId)
            syncAdminConversationDetail(adminToken, visitorId)
        }
        val existing = dao.getConversation(visitorId) ?: return
        dao.markVisitorMessagesRead(visitorId)
        dao.upsertConversation(existing.copy(unreadForAdmin = 0))
    }

    suspend fun updateConversationStatus(adminToken: String?, visitorId: String, status: String) {
        if (!adminToken.isNullOrBlank()) {
            ArchiveRemoteClient.updateAdminConversationStatus(adminToken, visitorId, status)
        }
        val existing = dao.getConversation(visitorId) ?: return
        dao.upsertConversation(existing.copy(status = status))
    }

    suspend fun deleteConversation(adminToken: String?, visitorId: String) {
        if (!adminToken.isNullOrBlank()) {
            ArchiveRemoteClient.deleteAdminConversation(adminToken, visitorId)
        }
        dao.deleteMessagesForVisitor(visitorId)
        dao.deleteConversation(visitorId)
    }

    suspend fun updateSiteConfig(adminToken: String?, config: SiteConfigEntity) {
        dao.upsertSiteConfig(config)
        if (!adminToken.isNullOrBlank()) {
            ArchiveRemoteClient.saveRemoteSiteContent(adminToken, config)
        }
    }
}
