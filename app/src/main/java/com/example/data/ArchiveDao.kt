package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {
    // --- Blog Posts ---
    @Query("SELECT * FROM blog_posts ORDER BY publishedAt DESC")
    fun getAllPostsFlow(): Flow<List<BlogPostEntity>>

    @Query("SELECT * FROM blog_posts ORDER BY publishedAt DESC")
    suspend fun getAllPostsOnce(): List<BlogPostEntity>

    @Query("SELECT * FROM blog_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): BlogPostEntity?

    @Query("SELECT * FROM blog_posts WHERE slug = :slug LIMIT 1")
    suspend fun getPostBySlug(slug: String): BlogPostEntity?

    @Query("SELECT * FROM blog_posts WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPostByRemoteId(remoteId: String): BlogPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: BlogPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<BlogPostEntity>)

    @Update
    suspend fun updatePost(post: BlogPostEntity)

    @Query("DELETE FROM blog_posts WHERE id = :id")
    suspend fun deletePostPermanently(id: Long)

    @Query("DELETE FROM blog_posts")
    suspend fun clearAllPosts()

    @Query("UPDATE blog_posts SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementPostViews(id: Long)

    // --- Post Revisions ---
    @Query("SELECT * FROM post_revisions WHERE postId = :postId ORDER BY savedAt DESC")
    fun getRevisionsForPostFlow(postId: Long): Flow<List<PostRevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: PostRevisionEntity)

    @Query("DELETE FROM post_revisions WHERE postId = :postId")
    suspend fun deleteRevisionsForPost(postId: Long)

    // --- Gallery ---
    @Query("SELECT * FROM gallery_items ORDER BY orderIndex ASC, dateMillis DESC")
    fun getAllGalleryItemsFlow(): Flow<List<GalleryItemEntity>>

    @Query("SELECT * FROM gallery_items WHERE id = :id LIMIT 1")
    suspend fun getGalleryItemById(id: Long): GalleryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItem(item: GalleryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItems(items: List<GalleryItemEntity>)

    @Update
    suspend fun updateGalleryItem(item: GalleryItemEntity)

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun deleteGalleryItem(id: Long)

    @Query("DELETE FROM gallery_items")
    suspend fun clearAllGalleryItems()

    // --- Chat Conversations & Messages ---
    @Query("SELECT * FROM chat_conversations ORDER BY lastMessageAt DESC")
    fun getAllConversationsFlow(): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM chat_conversations ORDER BY lastMessageAt DESC")
    suspend fun getAllConversationsOnce(): List<ChatConversationEntity>

    @Query("SELECT * FROM chat_conversations WHERE visitorId = :visitorId LIMIT 1")
    suspend fun getConversation(visitorId: String): ChatConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ChatConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(conversations: List<ChatConversationEntity>)

    @Query("DELETE FROM chat_conversations")
    suspend fun clearAllConversations()

    @Query("SELECT * FROM chat_messages WHERE visitorId = :visitorId ORDER BY timestamp ASC")
    fun getMessagesForVisitorFlow(visitorId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE visitorId = :visitorId ORDER BY timestamp ASC")
    suspend fun getMessagesForVisitorOnce(visitorId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET read = 1 WHERE visitorId = :visitorId AND sender = 'visitor'")
    suspend fun markVisitorMessagesRead(visitorId: String)

    @Query("DELETE FROM chat_conversations WHERE visitorId = :visitorId")
    suspend fun deleteConversation(visitorId: String)

    @Query("DELETE FROM chat_messages WHERE visitorId = :visitorId")
    suspend fun deleteMessagesForVisitor(visitorId: String)

    // --- Site Config / CMS ---
    @Query("SELECT * FROM site_config WHERE id = 1 LIMIT 1")
    fun getSiteConfigFlow(): Flow<SiteConfigEntity?>

    @Query("SELECT * FROM site_config WHERE id = 1 LIMIT 1")
    suspend fun getSiteConfigOnce(): SiteConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSiteConfig(config: SiteConfigEntity)
}
