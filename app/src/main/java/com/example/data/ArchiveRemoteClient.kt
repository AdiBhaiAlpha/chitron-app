package com.example.data

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object ArchiveRemoteClient {
    const val BASE_URL = "https://chitron.iam.bd"

    fun resolveMediaUrl(rawUrl: String?): String {
        val clean = rawUrl?.trim().orEmpty()
        if (clean.isEmpty()) return ""
        if (clean == "local://portrait") return clean
        return if (clean.startsWith("http://") || clean.startsWith("https://")) {
            Uri.encode(clean, ":/?#[]@!$&'()*+,;=%-_~.")
        } else {
            val path = if (clean.startsWith("/")) clean else "/$clean"
            BASE_URL + Uri.encode(path, "/:@?&=%-_~.")
        }
    }

    fun htmlToReadableText(rawHtml: String?): String {
        val input = rawHtml?.trim().orEmpty()
        if (input.isEmpty()) return ""
        if (!input.contains("<") && !input.contains("&")) return input

        // Preserve stanza and line breaks from <div>, <p>, <br>, <blockquote>, <h1>-<h6>
        var processed = input
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("</div>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</blockquote>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<li>", RegexOption.IGNORE_CASE), "• ")
            .replace(Regex("</li>", RegexOption.IGNORE_CASE), "\n")

        // Strip remaining HTML tags
        processed = processed.replace(Regex("<[^>]*>"), "")

        // Decode common HTML entities
        processed = processed
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&#39;", "'")
            .replace("&ldquo;", "“")
            .replace("&rdquo;", "”")
            .replace("&lsquo;", "‘")
            .replace("&rsquo;", "’")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")

        // Normalize excessive blank lines while preserving poem line breaks
        val lines = processed.lines().map { it.trimEnd() }
        val result = StringBuilder()
        var consecutiveBlank = 0
        for (line in lines) {
            if (line.isBlank()) {
                consecutiveBlank++
                if (consecutiveBlank <= 1 && result.isNotEmpty()) {
                    result.append("\n")
                }
            } else {
                consecutiveBlank = 0
                if (result.isNotEmpty()) {
                    result.append("\n")
                }
                result.append(line)
            }
        }
        return result.toString().trim()
    }

    fun parseIsoMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = sdf.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return System.currentTimeMillis()
    }

    private suspend fun requestJson(
        path: String,
        method: String = "GET",
        bodyJson: JSONObject? = null,
        adminToken: String? = null
    ): JSONObject? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL$path")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (!adminToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $adminToken")
                    setRequestProperty("X-Admin-Token", adminToken)
                }
                if (bodyJson != null && (method == "POST" || method == "PUT")) {
                    doOutput = true
                    OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(bodyJson.toString())
                        writer.flush()
                    }
                }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            if (stream == null) return@withContext null
            val responseText = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use {
                it.readText()
            }
            if (code !in 200..299) return@withContext null
            if (responseText.trim().startsWith("[")) {
                JSONObject().put("items", JSONArray(responseText))
            } else {
                JSONObject(responseText)
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    fun parsePostJson(obj: JSONObject, existingContent: String = ""): BlogPostEntity {
        val remoteId = obj.optString("_id").ifBlank { obj.optString("id") }
        val title = obj.optString("title", "Untitled")
        val slug = obj.optString("slug").ifBlank { remoteId }
        val rawExcerpt = obj.optString("excerpt", "")
        val rawContent = obj.optString("content", "")
        val cleanContent = if (rawContent.isNotBlank()) {
            htmlToReadableText(rawContent)
        } else {
            existingContent.ifBlank { htmlToReadableText(rawExcerpt) }
        }
        val cleanExcerpt = htmlToReadableText(rawExcerpt).replace("\n", " ")
        val coverUrl = resolveMediaUrl(obj.optString("coverImage", ""))
        val labelsArr = obj.optJSONArray("labels")
        val labels = mutableListOf<String>()
        if (labelsArr != null) {
            for (i in 0 until labelsArr.length()) {
                val tag = labelsArr.optString(i).trim()
                if (tag.isNotEmpty()) labels.add(tag)
            }
        }
        val pubMillis = parseIsoMillis(
            obj.optString("publishedAt").ifBlank { obj.optString("createdAt") }
        )

        return BlogPostEntity(
            remoteId = remoteId,
            title = title,
            slug = slug,
            excerpt = cleanExcerpt,
            content = cleanContent,
            coverImage = coverUrl,
            author = obj.optString("author", "Chitron Bhattacharjee"),
            category = obj.optString("category", "general"),
            labelsCsv = labels.joinToString(","),
            status = obj.optString("status", "published"),
            publishedAt = pubMillis,
            readingTime = obj.optInt("readingTime", 1).coerceAtLeast(1),
            viewCount = obj.optInt("viewCount", 0),
            seoTitle = obj.optString("seoTitle", ""),
            seoDescription = obj.optString("seoDescription", ""),
            featured = obj.optBoolean("featured", false)
        )
    }

    suspend fun fetchPublishedPosts(): List<BlogPostEntity> {
        val res = requestJson("/api/posts?limit=100") ?: return emptyList()
        val arr = res.optJSONArray("posts") ?: return emptyList()
        val list = mutableListOf<BlogPostEntity>()
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val summary = parsePostJson(item)
            // Fetch full post content by slug so poems and articles have full text
            val detailRes = requestJson("/api/posts/${Uri.encode(summary.slug)}")
            val detailObj = detailRes?.optJSONObject("post")
            if (detailObj != null) {
                list.add(parsePostJson(detailObj, summary.content))
            } else {
                list.add(summary)
            }
        }
        return list
    }

    suspend fun fetchSinglePostBySlug(slug: String): BlogPostEntity? {
        val res = requestJson("/api/posts/${Uri.encode(slug)}") ?: return null
        val postObj = res.optJSONObject("post") ?: return null
        return parsePostJson(postObj)
    }

    suspend fun fetchAdminPosts(adminToken: String): List<BlogPostEntity>? {
        val res = requestJson("/api/admin/posts?limit=100", adminToken = adminToken) ?: return null
        val arr = res.optJSONArray("posts") ?: return null
        val list = mutableListOf<BlogPostEntity>()
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            list.add(parsePostJson(item))
        }
        return list
    }

    suspend fun fetchGalleryItems(): List<GalleryItemEntity>? {
        val res = requestJson("/api/gallery?limit=150") ?: return null
        val arr = res.optJSONArray("photos") ?: res.optJSONArray("items") ?: return null
        val list = mutableListOf<GalleryItemEntity>()
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val remoteId = item.optString("_id").ifBlank { item.optString("id") }
            val rawUrl = item.optString("imageUrl").ifBlank { item.optString("url") }
            val tagsArr = item.optJSONArray("tags")
            val tags = mutableListOf<String>()
            if (tagsArr != null) {
                for (t in 0 until tagsArr.length()) {
                    val tag = tagsArr.optString(t).trim()
                    if (tag.isNotEmpty()) tags.add(tag)
                }
            }
            list.add(
                GalleryItemEntity(
                    remoteId = remoteId,
                    title = item.optString("title", "Untitled"),
                    caption = item.optString("caption", ""),
                    url = resolveMediaUrl(rawUrl),
                    category = item.optString("category", "Photography"),
                    tagsCsv = tags.joinToString(","),
                    location = item.optString("location", "Bangladesh"),
                    alt = item.optString("alt", ""),
                    dateMillis = parseIsoMillis(
                        item.optString("date").ifBlank { item.optString("createdAt") }
                    ),
                    featured = item.optBoolean("featured", false),
                    status = item.optString("status", "published"),
                    orderIndex = item.optInt("order", i + 1)
                )
            )
        }
        return list
    }

    suspend fun fetchSiteConfig(existing: SiteConfigEntity): SiteConfigEntity? {
        val homeBundle = requestJson("/api/home")
        val aboutRes = requestJson("/api/content/about")
        if (homeBundle == null && aboutRes == null) return null

        val settings = homeBundle?.optJSONObject("settings")
        val page = homeBundle?.optJSONObject("page")
        val profile = aboutRes?.optJSONObject("profile")

        val skillsArr = profile?.optJSONArray("skills")
        val skillsList = mutableListOf<String>()
        if (skillsArr != null) {
            for (i in 0 until skillsArr.length()) {
                val s = skillsArr.optString(i).trim()
                if (s.isNotEmpty()) skillsList.add(s)
            }
        }

        return existing.copy(
            siteName = settings?.optString("siteName")?.ifBlank { existing.siteName } ?: existing.siteName,
            tagline = settings?.optString("tagline")?.ifBlank { existing.tagline } ?: existing.tagline,
            authorName = settings?.optString("authorName")?.ifBlank { existing.authorName } ?: existing.authorName,
            authorTitle = settings?.optString("authorTitle")?.ifBlank { existing.authorTitle } ?: existing.authorTitle,
            location = settings?.optString("location")?.ifBlank { existing.location } ?: existing.location,
            contactEmail = settings?.optString("contactEmail")?.ifBlank { existing.contactEmail } ?: existing.contactEmail,
            heroTitle = page?.optString("heroTitle")?.ifBlank { existing.heroTitle } ?: existing.heroTitle,
            heroDescription = page?.optString("heroDescription")?.ifBlank { existing.heroDescription } ?: existing.heroDescription,
            primaryButtonText = page?.optString("primaryButtonText")?.ifBlank { existing.primaryButtonText } ?: existing.primaryButtonText,
            secondaryButtonText = page?.optString("secondaryButtonText")?.ifBlank { existing.secondaryButtonText } ?: existing.secondaryButtonText,
            featuredSectionTitle = page?.optString("featuredSectionTitle")?.ifBlank { existing.featuredSectionTitle } ?: existing.featuredSectionTitle,
            aboutHeadline = profile?.optString("headline")?.ifBlank { existing.aboutHeadline } ?: existing.aboutHeadline,
            aboutShortBio = profile?.optString("shortBio")?.ifBlank { existing.aboutShortBio } ?: existing.aboutShortBio,
            aboutBiography = profile?.optString("biography")?.ifBlank { existing.aboutBiography } ?: existing.aboutBiography,
            aboutPhilosophy = profile?.optString("philosophy")?.ifBlank { existing.aboutPhilosophy } ?: existing.aboutPhilosophy,
            aboutCurrently = profile?.optString("currentFocus")?.ifBlank { existing.aboutCurrently } ?: existing.aboutCurrently,
            skillsCsv = if (skillsList.isNotEmpty()) skillsList.joinToString(",") else existing.skillsCsv
        )
    }

    suspend fun loginAdmin(pin: String): String? {
        val body = JSONObject().put("pin", pin.trim())
        val res = requestJson("/api/auth/login", method = "POST", bodyJson = body) ?: return null
        if (res.optBoolean("success", false)) {
            return res.optString("token").takeIf { it.isNotBlank() }
        }
        return null
    }

    // --- Visitor Live Chat ---
    suspend fun fetchVisitorConversation(visitorId: String): Pair<ChatConversationEntity, List<ChatMessageEntity>>? {
        val res = requestJson("/api/chat/conversation/${Uri.encode(visitorId)}?markRead=1") ?: return null
        val convObj = res.optJSONObject("conversation") ?: res
        return parseConversationAndMessages(convObj, visitorId)
    }

    suspend fun sendVisitorMessage(
        visitorId: String,
        visitorName: String,
        text: String
    ): Boolean {
        val body = JSONObject()
            .put("text", text)
            .put("visitorName", visitorName)
            .put("type", "text")
        val res = requestJson(
            "/api/chat/conversation/${Uri.encode(visitorId)}/message",
            method = "POST",
            bodyJson = body
        )
        return res != null
    }

    // --- Admin Live Messenger ---
    suspend fun fetchAdminConversations(adminToken: String): List<ChatConversationEntity>? {
        val res = requestJson("/api/admin/chat/conversations", adminToken = adminToken) ?: return null
        val arr = res.optJSONArray("conversations") ?: return null
        val list = mutableListOf<ChatConversationEntity>()
        for (i in 0 until arr.length()) {
            val c = arr.optJSONObject(i) ?: continue
            val vid = c.optString("visitorId")
            if (vid.isBlank()) continue
            list.add(
                ChatConversationEntity(
                    visitorId = vid,
                    visitorName = c.optString("visitorName").ifBlank { "Anonymous (${vid.takeLast(6)})" },
                    visitorEmail = c.optString("visitorEmail", ""),
                    lastMessage = c.optString("lastMessage", ""),
                    lastMessageAt = c.optLong("lastMessageAt", System.currentTimeMillis()),
                    lastSender = c.optString("lastSender", "visitor"),
                    unreadForAdmin = c.optInt("unreadForAdmin", 0),
                    status = c.optString("status", "active")
                )
            )
        }
        return list
    }

    suspend fun fetchAdminConversationDetail(
        adminToken: String,
        visitorId: String
    ): Pair<ChatConversationEntity, List<ChatMessageEntity>>? {
        val res = requestJson(
            "/api/admin/chat/conversations/${Uri.encode(visitorId)}",
            adminToken = adminToken
        ) ?: return null
        val convObj = res.optJSONObject("conversation") ?: return null
        return parseConversationAndMessages(convObj, visitorId)
    }

    private fun parseConversationAndMessages(
        convObj: JSONObject,
        fallbackVisitorId: String
    ): Pair<ChatConversationEntity, List<ChatMessageEntity>> {
        val vid = convObj.optString("visitorId").ifBlank { fallbackVisitorId }
        val conv = ChatConversationEntity(
            visitorId = vid,
            visitorName = convObj.optString("visitorName").ifBlank { "Anonymous (${vid.takeLast(6)})" },
            visitorEmail = convObj.optString("visitorEmail", ""),
            lastMessage = convObj.optString("lastMessage", ""),
            lastMessageAt = convObj.optLong("lastMessageAt", System.currentTimeMillis()),
            lastSender = convObj.optString("lastSender", "visitor"),
            unreadForAdmin = convObj.optInt("unreadForAdmin", 0),
            status = convObj.optString("status", "active")
        )
        val msgsArr = convObj.optJSONArray("messages")
        val messages = mutableListOf<ChatMessageEntity>()
        if (msgsArr != null) {
            for (i in 0 until msgsArr.length()) {
                val m = msgsArr.optJSONObject(i) ?: continue
                val msgText = m.optString("text", "")
                if (msgText.isBlank()) continue
                messages.add(
                    ChatMessageEntity(
                        remoteMessageId = m.optString("messageId", ""),
                        visitorId = vid,
                        sender = m.optString("sender", "visitor"),
                        text = msgText,
                        read = m.optBoolean("read", false),
                        timestamp = m.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }
        return conv to messages
    }

    suspend fun sendAdminReply(
        adminToken: String,
        visitorId: String,
        text: String
    ): Boolean {
        val body = JSONObject().put("text", text).put("type", "text")
        val res = requestJson(
            "/api/admin/chat/conversations/${Uri.encode(visitorId)}/reply",
            method = "POST",
            bodyJson = body,
            adminToken = adminToken
        )
        return res != null
    }

    suspend fun markAdminConversationRead(adminToken: String, visitorId: String) {
        requestJson(
            "/api/admin/chat/conversations/${Uri.encode(visitorId)}/read",
            method = "POST",
            bodyJson = JSONObject(),
            adminToken = adminToken
        )
    }

    suspend fun updateAdminConversationStatus(
        adminToken: String,
        visitorId: String,
        status: String
    ) {
        requestJson(
            "/api/admin/chat/conversations/${Uri.encode(visitorId)}/status",
            method = "PUT",
            bodyJson = JSONObject().put("status", status),
            adminToken = adminToken
        )
    }

    suspend fun deleteAdminConversation(adminToken: String, visitorId: String) {
        requestJson(
            "/api/admin/chat/conversations/${Uri.encode(visitorId)}",
            method = "DELETE",
            adminToken = adminToken
        )
    }

    // --- Admin Posts & Gallery Mutations ---
    suspend fun createOrUpdateRemotePost(
        adminToken: String,
        remoteId: String,
        title: String,
        slug: String,
        excerpt: String,
        content: String,
        coverImage: String,
        category: String,
        labelsCsv: String,
        status: String,
        featured: Boolean
    ): BlogPostEntity? {
        val labelsArray = JSONArray()
        labelsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
            labelsArray.put(it)
        }
        val payload = JSONObject()
            .put("title", title)
            .put("slug", slug)
            .put("excerpt", excerpt)
            .put("content", content)
            .put("coverImage", coverImage)
            .put("category", category)
            .put("labels", labelsArray)
            .put("status", status)
            .put("featured", featured)

        val res = if (remoteId.isNotBlank()) {
            requestJson(
                "/api/admin/posts/${Uri.encode(remoteId)}",
                method = "PUT",
                bodyJson = payload,
                adminToken = adminToken
            )
        } else {
            requestJson(
                "/api/admin/posts",
                method = "POST",
                bodyJson = payload,
                adminToken = adminToken
            )
        }
        val postObj = res?.optJSONObject("post") ?: return null
        return parsePostJson(postObj, content)
    }

    suspend fun duplicateRemotePost(adminToken: String, remoteId: String): Boolean {
        if (remoteId.isBlank()) return false
        return requestJson(
            "/api/admin/posts/${Uri.encode(remoteId)}/duplicate",
            method = "POST",
            bodyJson = JSONObject(),
            adminToken = adminToken
        ) != null
    }

    suspend fun updateRemotePostStatus(adminToken: String, remoteId: String, newStatus: String): Boolean {
        if (remoteId.isBlank()) return false
        val path = when (newStatus) {
            "published" -> "/api/admin/posts/${Uri.encode(remoteId)}/publish"
            "draft" -> "/api/admin/posts/${Uri.encode(remoteId)}/unpublish"
            "trashed" -> "/api/admin/posts/${Uri.encode(remoteId)}"
            else -> "/api/admin/posts/${Uri.encode(remoteId)}/restore"
        }
        val method = if (newStatus == "trashed") "DELETE" else "POST"
        return requestJson(path, method = method, bodyJson = JSONObject(), adminToken = adminToken) != null
    }

    suspend fun deleteRemotePostPermanently(adminToken: String, remoteId: String): Boolean {
        if (remoteId.isBlank()) return false
        return requestJson(
            "/api/admin/posts/${Uri.encode(remoteId)}/permanent",
            method = "DELETE",
            adminToken = adminToken
        ) != null
    }

    suspend fun createRemoteGalleryPhoto(
        adminToken: String,
        title: String,
        caption: String,
        url: String,
        category: String,
        location: String,
        featured: Boolean
    ): Boolean {
        val payload = JSONObject()
            .put("title", title)
            .put("caption", caption)
            .put("url", url)
            .put("imageUrl", url)
            .put("category", category)
            .put("location", location)
            .put("featured", featured)
            .put("status", "published")
        return requestJson(
            "/api/admin/gallery",
            method = "POST",
            bodyJson = payload,
            adminToken = adminToken
        ) != null
    }

    suspend fun deleteRemoteGalleryPhoto(adminToken: String, remoteId: String): Boolean {
        if (remoteId.isBlank()) return false
        return requestJson(
            "/api/admin/gallery/${Uri.encode(remoteId)}",
            method = "DELETE",
            adminToken = adminToken
        ) != null
    }

    suspend fun saveRemoteSiteContent(adminToken: String, config: SiteConfigEntity) {
        val settingsBody = JSONObject()
            .put("siteName", config.siteName)
            .put("tagline", config.tagline)
            .put("authorName", config.authorName)
            .put("authorTitle", config.authorTitle)
            .put("location", config.location)
            .put("contactEmail", config.contactEmail)
        requestJson("/api/admin/content/settings", method = "PUT", bodyJson = settingsBody, adminToken = adminToken)

        val homeBody = JSONObject()
            .put("heroTitle", config.heroTitle)
            .put("heroDescription", config.heroDescription)
            .put("primaryButtonText", config.primaryButtonText)
            .put("secondaryButtonText", config.secondaryButtonText)
            .put("featuredSectionTitle", config.featuredSectionTitle)
        requestJson("/api/admin/content/homepage", method = "PUT", bodyJson = homeBody, adminToken = adminToken)

        val skillsArray = JSONArray()
        config.skillsList.forEach { skillsArray.put(it) }
        val aboutBody = JSONObject()
            .put("headline", config.aboutHeadline)
            .put("shortBio", config.aboutShortBio)
            .put("biography", config.aboutBiography)
            .put("philosophy", config.aboutPhilosophy)
            .put("currentFocus", config.aboutCurrently)
            .put("skills", skillsArray)
        requestJson("/api/admin/content/about", method = "PUT", bodyJson = aboutBody, adminToken = adminToken)
    }
}
