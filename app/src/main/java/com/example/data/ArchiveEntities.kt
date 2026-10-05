package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blog_posts")
data class BlogPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = "",
    val title: String,
    val slug: String,
    val excerpt: String,
    val content: String,
    val coverImage: String = "",
    val author: String = "Chitron Bhattacharjee",
    val category: String = "general",
    val labelsCsv: String = "",
    val status: String = "published", // draft, published, scheduled, trashed
    val publishedAt: Long = System.currentTimeMillis(),
    val scheduledAt: Long? = null,
    val readingTime: Int = 1,
    val viewCount: Int = 0,
    val seoTitle: String = "",
    val seoDescription: String = "",
    val featured: Boolean = false
) {
    val labelsList: List<String>
        get() = labelsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "post_revisions")
data class PostRevisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = "",
    val postId: Long,
    val title: String,
    val excerpt: String,
    val content: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gallery_items")
data class GalleryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = "",
    val title: String,
    val caption: String,
    val url: String,
    val category: String = "Photography",
    val tagsCsv: String = "",
    val location: String = "Bangladesh",
    val alt: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val featured: Boolean = false,
    val status: String = "published",
    val orderIndex: Int = 0
) {
    val tagsList: List<String>
        get() = tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey val visitorId: String,
    val visitorName: String = "Anonymous Visitor",
    val visitorEmail: String = "",
    val lastMessage: String = "",
    val lastMessageAt: Long = System.currentTimeMillis(),
    val lastSender: String = "visitor", // visitor or admin
    val unreadForAdmin: Int = 0,
    val status: String = "active" // active, archived, blocked
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteMessageId: String = "",
    val visitorId: String,
    val sender: String, // visitor or admin
    val text: String,
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "site_config")
data class SiteConfigEntity(
    @PrimaryKey val id: Int = 1,
    val siteName: String = "Chitron's Archive",
    val tagline: String = "Notes, ideas, experiments and things worth remembering.",
    val authorName: String = "Chitron Bhattacharjee",
    val authorTitle: String = "AI Developer, Programmer & Writer",
    val location: String = "Mymensingh, Bangladesh",
    val contactEmail: String = "chitronbhattacharjee@gmail.com",
    val heroTitle: String = "Chitron Bhattacharjee",
    val heroDescription: String = "AI developer, programmer, designer and writer from Bangladesh. This is my personal archive — notes, ideas, experiments and things worth remembering.",
    val primaryButtonText: String = "Read Writing",
    val secondaryButtonText: String = "About Me",
    val featuredSectionTitle: String = "Latest Writing",
    val aboutHeadline: String = "Hi, I’m Chitron Bhattacharjee.",
    val aboutShortBio: String = "I’m an AI developer, programmer, and writer from Bangladesh. I enjoy building things with technology, especially AI-powered systems, web applications, and tools that solve real problems in a simple way.\n\nI’m always interested in learning how things work behind the scenes and turning ideas into something people can actually use.",
    val aboutBiography: String = "I work with modern web technologies and enjoy experimenting with AI, automation, and conversational systems. Most of my time goes into building projects, improving my skills, and exploring new ideas in technology.\n\nI also enjoy writing. Sometimes I write about technology, sometimes about ideas and experiences, and sometimes simply to put thoughts into words.",
    val aboutPhilosophy: String = "I believe good software does not need to be unnecessarily complicated. I prefer things that are simple, fast, practical, and easy to understand.\n\nWhether I’m building a small tool or working on a larger project, I try to focus on making it useful first. Technology should solve problems, not create more of them.",
    val aboutCurrently: String = "Right now, I’m working on AI-related projects, conversational systems, and personal web platforms. I’m also continuing to learn and experiment with new technologies as I build.",
    val skillsCsv: String = "JavaScript,Node.js,Express,MongoDB,PHP,HTML & CSS"
) {
    val skillsList: List<String>
        get() = skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
